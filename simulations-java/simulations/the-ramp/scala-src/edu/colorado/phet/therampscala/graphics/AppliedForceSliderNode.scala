package edu.colorado.phet.therampscala.graphics


import common.phetcommon.view.graphics.transforms.ModelViewTransform2D
import java.awt.geom.Point2D
import model.{IBead, Bead}
import umd.cs.piccolo.PNode
import swing.ScalaValueControl
import umd.cs.piccolox.pswing.PSwing
import scalacommon.math.Vector2D
import edu.colorado.phet.scalacommon.Predef._

class AppliedForceSliderNode(bead: IBead, transform: ModelViewTransform2D) extends PNode {
  val max = 500
  val control = new ScalaValueControl(-max, max, "Applied Force X", "0.0", "N",
    bead.parallelAppliedForce, value => bead.parallelAppliedForce = value, bead.addListener)

  val pswing = new PSwing(control)
  addChild(pswing)
  def updatePosition() = {
    val viewLoc = transform.modelToView(new Point2D.Double(0, -1))
    val scale = 1.2f
    pswing.setOffset(viewLoc - new Vector2D(pswing.getFullBounds.getWidth * scale, 0))
    pswing.setScale(scale)
  }
  updatePosition()
}