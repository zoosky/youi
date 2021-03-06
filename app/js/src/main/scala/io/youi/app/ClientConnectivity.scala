package io.youi.app

import io.youi.{AnimationFrame, History}
import io.youi.http.{Connection, WebSocketUtil}
import io.youi.net.URL
import io.youi.stream.StreamURL
import org.scalajs.dom.{WebSocket, window}
import org.scalajs.dom.ext.AjaxException
import reactify.{ChangeListener, Var}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ClientConnectivity(connectivity: ApplicationConnectivity, application: ClientApplication) {
  val connection: Connection = new Connection
  val webSocket: Var[Option[WebSocket]] = Var(None)

  connection.receive.text.attach {
    case "PING" => connection.send.text := "PONG"
    case _ => // Ignore
  }

  if (connectivity.autoConnect) {
    connect()
  }

  connection.connected.changes(new ChangeListener[Boolean] {
    override def change(oldValue: Boolean, newValue: Boolean): Unit = if (oldValue && !newValue && application.autoReload) {
      attemptReload()
    }
  })

  def connect(): Unit = {
    connectivity.activeConnections := Set(connection)

    disconnect()
    val protocol = if (window.location.protocol == "https:") {
      "wss"
    } else {
      "ws"
    }
    val url = URL(s"$protocol://${window.location.host}${connectivity.path}")
    webSocket := Some(WebSocketUtil.connect(url, connection))
  }

  def disconnect(): Unit = synchronized {
    webSocket().foreach { ws =>
      if (ws.readyState == WebSocket.OPEN) {
        ws.close()
      }
      webSocket := None
    }
  }

  def close(): Unit = {
    connection.close()
    disconnect()
  }

  private def attemptReload(attempt: Int = 0): Unit = {
    StreamURL.stream(History.url()).onComplete {
      case Success(html) => History.reload(force = true)
      case Failure(exception) => {
        exception match {
          case exc: AjaxException if exc.xhr.status > 0 => History.reload(force = true)
          case _ => {
            val timeout = if (attempt < 10) {
              2500
            } else if (attempt < 25) {
              5000
            } else if (attempt < 100) {
              10000
            } else {
              30000
            }

            window.setTimeout(() => {
              attemptReload(attempt + 1)
            }, timeout)
          }
        }
      }
    }
  }
}
