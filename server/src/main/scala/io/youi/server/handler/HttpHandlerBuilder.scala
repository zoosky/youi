package io.youi.server.handler

import java.io.File

import io.youi.Priority
import io.youi.http._
import io.youi.net.{ContentType, URL, URLMatcher}
import io.youi.server.Server
import io.youi.server.validation.{ValidationResult, Validator}
import io.youi.stream.{Delta, HTMLParser, Selector}

case class HttpHandlerBuilder(server: Server,
                              urlMatcher: Option[URLMatcher] = None,
                              cachingManager: CachingManager = CachingManager.Default,
                              priority: Priority = Priority.Normal,
                              validators: List[Validator] = Nil) {
  def priority(priority: Priority): HttpHandlerBuilder = copy(priority = priority)

  def matcher(urlMatcher: URLMatcher): HttpHandlerBuilder = copy(urlMatcher = Some(urlMatcher))

  def caching(cachingManager: CachingManager): HttpHandlerBuilder = copy(cachingManager = cachingManager)

  def withValidation(validators: Validator*): HttpHandlerBuilder = copy(validators = validators.toList ::: this.validators)

  def resource(f: => Content): HttpHandler = resource((_: URL) => Some(f))

  def resource(f: URL => Option[Content]): HttpHandler = {
    handle { connection =>
      f(connection.request.url).foreach { content =>
        SenderHandler(content, caching = cachingManager).handle(connection)
      }
    }
  }

  def file(directory: File, pathTransform: String => String = (s: String) => s): HttpHandler = {
    handle { connection =>
      val path = pathTransform(connection.request.url.path.encoded)
      val file = new File(directory, path)
      if (file.isFile) {
        SenderHandler(Content.file(file), caching = cachingManager).handle(connection)
      }
    }
  }

  def classLoader(directory: String = "", pathTransform: String => String = (s: String) => s): HttpHandler = {
    val dir = if (directory.endsWith("/")) {
      directory.substring(directory.length - 1)
    } else {
      directory
    }
    handle { connection =>
      val path = pathTransform(connection.request.url.path.encoded)
      val resourcePath = s"$dir$path" match {
        case s if s.startsWith("/") => s.substring(1)
        case s => s
      }
      Option(getClass.getClassLoader.getResource(resourcePath)).foreach { url =>
        val file = new File(url.getFile)
        if (!file.isDirectory) {
          SenderHandler(Content.classPath(url), caching = cachingManager).handle(connection)
        }
      }
    }
  }

  def proxy(handler: ProxyHandler): HttpHandler = handle { connection =>
    connection.proxySupport = handler
  }

  def stream(baseDirectory: File, basePath: String, deltas: HttpConnection => List[Delta] = _ => Nil): HttpHandler = handle { connection =>
    val url = connection.request.url
    val path = url.path.decoded
    if (path.startsWith(basePath)) {
      val clippedPath = path.substring(basePath.length)
      val file = new File(baseDirectory, clippedPath)
      if (file.exists()) {
        val parser = HTMLParser.cache(file)
        val selector = url.param("selector").map(Selector.parse)
        val mods = deltas(connection)
        val html = parser.stream(mods, selector)
        val content = StringContent(html, ContentType.`text/html`, file.lastModified())
        val handler = SenderHandler(content, caching = cachingManager)
        handler.handle(connection)
      }
    }
  }

  def handle(f: HttpConnection => Unit): HttpHandler = wrap(new HttpHandler {
    override def handle(connection: HttpConnection): Unit = f(connection)
  })

  def validation(validator: HttpConnection => ValidationResult): HttpHandler = validation(new Validator {
    override def validate(connection: HttpConnection): ValidationResult = validator(connection)
  })

  def validation(validators: Validator*): HttpHandler = wrap(new ValidatorHttpHandler(validators.toList))

  def wrap(handler: HttpHandler): HttpHandler = {
    val p = priority
    val wrapper = new HttpHandler {
      override def priority: Priority = p

      override def handle(connection: HttpConnection): Unit = {
        if (urlMatcher.forall(_.matches(connection.request.url))) {
          ValidatorHttpHandler.validate(connection, validators) match {
            case ValidationResult.Continue => handler.handle(connection)
            case _ => // Validation failed, handled by ValidatorHttpHandler
          }
        }
      }
    }
    server.handlers += wrapper
    handler
  }
}