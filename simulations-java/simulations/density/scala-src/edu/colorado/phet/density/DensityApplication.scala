package edu.colorado.phet.density


import com.jme.bounding.BoundingBox
import com.jme.image.Texture
import com.jme.input.action.{InputAction, InputActionEvent}

import com.jme.input.{InputHandler, KeyInput}
import com.jme.math.{FastMath, Quaternion, Vector3f}
import com.jme.scene.shape.Box

import com.jme.renderer.Renderer
import com.jme.scene.state.TextureState
import com.jme.system.canvas.SimpleCanvasImpl
import com.jme.system.DisplaySystem
import com.jme.system.lwjgl.LWJGLSystemProvider
import com.jme.util.TextureManager
import com.jmex.awt.input.AWTMouseInput
import com.jmex.awt.lwjgl.{LWJGLAWTCanvasConstructor, LWJGLCanvas}
import common.phetcommon.application.{PhetApplicationConfig, Module, PhetApplicationLauncher}
import common.piccolophet.nodes.mediabuttons.PiccoloClockControlPanel
import common.piccolophet.PiccoloPhetApplication
import java.awt.{Canvas, BorderLayout, Component}
import javax.swing.{JComponent, JPanel}
import jmetest.util.JMESwingTest
import scalacommon.ScalaClock

class DensityModule extends Module("Density", new ScalaClock(30, 30 / 1000.0)) {
  setSimulationPanel(new JMEPanel)
  setClockControlPanel(new PiccoloClockControlPanel(getClock))
}

//See JMESwingTest
class JMEPanel extends JPanel {
  setLayout(new BorderLayout)
  val display = DisplaySystem.getDisplaySystem(LWJGLSystemProvider.LWJGL_SYSTEM_IDENTIFIER)
  display.registerCanvasConstructor("AWT", classOf[LWJGLAWTCanvasConstructor])
  val canvas = display.createCanvas(500, 500)
  canvas.setUpdateInput(true)
  canvas.setTargetRate(60)
  canvas.setImplementor(new MyImplementor)

  AWTMouseInput.setup(canvas.asInstanceOf[Canvas], false)
  KeyInput.setProvider(KeyInput.INPUT_AWT)

  add(canvas.asInstanceOf[Component], BorderLayout.CENTER)
}

class MyImplementor extends SimpleCanvasImpl(499, 499) {
  var angle = 0.0f
  val rotQuat = new Quaternion()
  val axis = new Vector3f(1, 1, 0.5f)
  var fps = 0.0
  val input = new InputHandler()
  val max = new Vector3f(5, 5, 5)
  val min = new Vector3f(-5, -5, -5)

  val box = new Box("Box", min, max)

  override def simpleSetup() = {

    axis.normalizeLocal()

    box.setModelBound(new BoundingBox())
    box.updateModelBound()
    box.setLocalTranslation(new Vector3f(0, 0, -10))
    box.setRenderQueueMode(Renderer.QUEUE_SKIP)
    rootNode.attachChild(box)

    val ts = renderer.createTextureState()
    ts.setEnabled(true)
    //    ts.setTexture(TextureManager.loadTexture(classOf[JMESwingTest].getClassLoader().getResource("jmetest/data/images/Monkey.jpg"),
    ts.setTexture(TextureManager.loadTexture(classOf[JMESwingTest].getClassLoader().getResource("phetcommon/images/logos/phet-logo-120x50.jpg"),
      Texture.MinificationFilter.Trilinear,
      Texture.MagnificationFilter.Bilinear))

    rootNode.setRenderState(ts)
    val startTime = System.currentTimeMillis() + 5000

    input.addAction(new InputAction() {
      override def performAction(evt: InputActionEvent) {
        println(evt)
        //        logger.info(evt.getTriggerName())
      }
    }, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_ALL,
      InputHandler.AXIS_NONE, false)

    input.addAction(new InputAction() {
      override def performAction(evt: InputActionEvent) {
        //        logger.info(evt.getTriggerName())
      }
    }, InputHandler.DEVICE_KEYBOARD, InputHandler.BUTTON_ALL,
      InputHandler.AXIS_NONE, false)
  }

  override def simpleUpdate() {
    input.update(tpf)

    // Code for rotating the box... no surprises here.
    if (tpf < 1) {
      angle = angle + (tpf * 25)
      if (angle > 360) {
        angle = 0
      }
    }
    rotQuat.fromAngleNormalAxis(angle * FastMath.DEG_TO_RAD, axis)
    box.setLocalRotation(rotQuat)
  }
}

class DensityApplication(config: PhetApplicationConfig) extends PiccoloPhetApplication(config: PhetApplicationConfig) {
  addModule(new DensityModule())
}

object DensityApplicationMain {
  def main(args: Array[String]) = new PhetApplicationLauncher().launchSim(args, "density", classOf[DensityApplication])
}