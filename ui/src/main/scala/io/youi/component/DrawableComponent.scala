package io.youi.component
import io.youi.component.shape.Drawable
import io.youi.style.{Paint, Theme}
import org.scalajs.dom.raw.CanvasRenderingContext2D
import reactify.Var

class DrawableComponent extends CanvasComponent {
  val drawable: Var[Drawable] = Var(Drawable.empty)

  drawable.on(reDraw.flag())

  val fill: Var[Paint] = Var(theme.fill)
  val stroke: Var[Paint] = Var(theme.stroke)

  override protected def defaultTheme: Theme = DrawableComponent

  override protected def draw(context: CanvasRenderingContext2D): Unit = {
//    if (fill().nonEmpty) {
//      context.fillStyle = fill().apply(this).asInstanceOf[js.Any]
//    }
//    if (stroke().nonEmpty) {
//      context.strokeStyle = stroke().apply(this).asInstanceOf[js.Any]
//    }
    drawable().draw(this, context)
//    if (fill().nonEmpty) {
//      context.fill()
//    }
//    if (stroke().nonEmpty) {
//      context.fill()
//    }
  }
}

object DrawableComponent extends Theme(CanvasComponent)