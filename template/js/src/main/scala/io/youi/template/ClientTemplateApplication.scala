package io.youi.template

import io.youi.app.ClientApplication
import io.youi.{History, dom}
import org.scalajs.dom._

import scala.scalajs.js.annotation.JSExportTopLevel

object ClientTemplateApplication extends TemplateApplication with ClientApplication {
  private lazy val pages = dom.byId[html.Input]("template_pages").value.split(';').toSet

  @JSExportTopLevel("application")
  def main(): Unit = {
    val paths = pages.map { page =>
      s"/${page.substring(0, page.indexOf('.'))}"
    }
    val screens = paths.map { p =>
      new TemplateScreen(p)
    }

    println("Template initialized!")
  }

  private def fixAnchors(pages: Set[String]): Unit = {
    dom.byTag[html.Anchor]("a").foreach { anchor =>
      val host = window.location.href.substring(0, window.location.href.lastIndexOf('/') + 1)
      val hrefOption = anchor.href match {
        case h if h.startsWith(host) && !h.endsWith("#") => Some(h.substring(host.length))
        case h => None
      }
      hrefOption match {
        case Some(href) => {
          if (pages.contains(href)) {
            val path = s"/${href.substring(0, href.length - 5)}"
            anchor.href = path
            anchor.addEventListener("click", (evt: Event) => {
              evt.preventDefault()
              evt.stopPropagation()

              History.pushPath(path)
            })
          }
        }
        case None => // Ignore
      }
    }
  }

  def screenFixes(): Unit = fixAnchors(pages)
}