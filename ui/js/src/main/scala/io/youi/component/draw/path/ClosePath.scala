package io.youi.component.draw.path

import org.scalajs.dom.raw.CanvasRenderingContext2D

object ClosePath extends PathAction {
  override def invoke(context: CanvasRenderingContext2D): Unit = context.closePath()
}
