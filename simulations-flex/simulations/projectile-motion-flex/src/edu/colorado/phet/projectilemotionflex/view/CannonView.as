/**
 * Created with IntelliJ IDEA.
 * User: Duso
 * Date: 6/17/12
 * Time: 9:09 AM
 * To change this template use File | Settings | File Templates.
 */
package edu.colorado.phet.projectilemotionflex.view {
import edu.colorado.phet.projectilemotionflex.model.TrajectoryModel;

import flash.display.Graphics;

import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.geom.Point;

public class CannonView extends Sprite {
    private var mainView: MainView;
    private var trajectoryModel: TrajectoryModel;
    private var container: BackgroundView;
    private var stageW: Number;
    private var stageH: Number;
    private var barrel: Sprite;
    private var carriage: Sprite;
    private var platform: Sprite;
    private var flames: Sprite;
    private var pixPerMeter: Number;



    public function CannonView( mainView:MainView,  trajectoryModel: TrajectoryModel, container:BackgroundView ) {
        this.mainView = mainView;
        this.trajectoryModel = trajectoryModel;
        this.container = container;
        this.stageW = mainView.stageW;
        this.stageH = mainView.stageH;
        this.barrel = new Sprite();
        this.carriage = new Sprite();
        this.platform = new Sprite();
        this.flames = new Sprite();
        this.initialize();
    }//end constructor

    private function initialize():void{
        trajectoryModel.registerView( this );
        this.pixPerMeter = mainView.pixPerMeter;
        this.drawCannon();
        this.drawCarriage();
        this.makeBarrelTiltable();
        this.makeCarriageMovable();
        this.addChild( barrel );
        this.addChild( carriage );
        this.addChild( platform );
        this.update();
    }//end initialize()

    private function drawCannon():void{
        //var pixPerM = mainView.pixPerMeter
        var L:Number = 2*pixPerMeter;
        var W:Number = 0.4*pixPerMeter;
        var gB: Graphics = barrel.graphics;
        with( gB ){
            clear();
            lineStyle( 1, 0x000000, 1 );
            beginFill( 0xbbbbbb, 1 );
            moveTo( 0 , -W/2 );
            lineTo( 0,  W/2 );
            lineTo( L,  W/2 );
            lineTo( L, -W/2 );
            lineTo( 0, -W/2 );
            endFill();
        }
    }//end drawCannon()

    private function drawCarriage():void{
        //var pixPerM = mainView.pixPerMeter
        var B:Number = 2*pixPerMeter;   //base width
        var H:Number = 1*pixPerMeter;   //height
        var W:Number = 0.4*pixPerMeter; //width of support
        var gC: Graphics = this.carriage.graphics;
        with( gC ){
            clear();
            lineStyle( 1, 0x000000, 1 );
            beginFill( 0x996633 );
            moveTo( 0, 0 );
            lineTo( -W/2, 0 );
            lineTo( -W/2, H );
            lineTo( - B/2, H );
            lineTo( - B/2, H + W );
            lineTo( B/2, H + W );
            lineTo( B/2, H );
            lineTo( W/2, H );
            lineTo( W/2, 0 );
            lineTo( 0, 0 );
            endFill();
        }
    }

    private function makeBarrelTiltable():void{
        var thisObject:Object = this;
        var thisBarrel:Object = this.barrel;
        this.barrel.buttonMode = true;
        this.barrel.addEventListener( MouseEvent.MOUSE_DOWN, startTargetTilt );

        function startTargetTilt( evt: MouseEvent ): void {
            //clickOffset = new Point( evt.localX, evt.localY );
            //trace("ChargeVeiw: mouseX = "+evt.stageX+"      mouseY = "+evt.stageY );
            stage.addEventListener( MouseEvent.MOUSE_UP, stopTargetTilt );
            stage.addEventListener( MouseEvent.MOUSE_MOVE, tiltTarget );
        }

        function stopTargetTilt( evt: MouseEvent ): void {
            var xInPix:Number = thisObject.mouseX - thisBarrel.x;   //screen coordinates, origin on left edge of stage
            var yInPix:Number = thisObject.mouseY - thisBarrel.y;   //screen coordinates, origin on top edge of stage
            var angleInDeg:Number = -( 180/Math.PI )*Math.atan2( yInPix,  xInPix );
            thisObject.trajectoryModel.angleInDeg = angleInDeg;
            evt.updateAfterEvent();
            stage.removeEventListener( MouseEvent.MOUSE_UP, stopTargetTilt );
            stage.removeEventListener( MouseEvent.MOUSE_MOVE, tiltTarget );
        }

        function tiltTarget( evt: MouseEvent ): void {
            var xInPix:Number = thisObject.mouseX - thisBarrel.x;   //screen coordinates, origin on left edge of stage
            var yInPix:Number = thisObject.mouseY - thisBarrel.y;   //screen coordinates, origin on top edge of stage
            var angleInDeg:Number = -( 180/Math.PI )*Math.atan2( yInPix,  xInPix );
            thisObject.trajectoryModel.angleInDeg = angleInDeg;
            evt.updateAfterEvent();
        }//end of dragTarget()
    }

    private function makeCarriageMovable():void{
        var thisObject: Object = this;
        var thisCarriage: Sprite = this.carriage;
        this.carriage.buttonMode = true;
        this.carriage.addEventListener( MouseEvent.MOUSE_DOWN, startTargetDrag );
        var clickOffset: Point;

        function startTargetDrag( evt: MouseEvent ): void {
            clickOffset = new Point( evt.localX, evt.localY );
            stage.addEventListener( MouseEvent.MOUSE_UP, stopTargetDrag );
            stage.addEventListener( MouseEvent.MOUSE_MOVE, dragTarget );
        }

        function stopTargetDrag( evt: MouseEvent ): void {

            var xInPix:Number = thisObject.container.container.mouseX - clickOffset.x;    //screen coordinates, origin on left edge of stage
            var yInPix:Number = thisObject.container.container.mouseY - clickOffset.y;    //screen coordinates, origin on left edge of stage
            thisObject.trajectoryModel.xP0 = xInPix/(thisObject.mainView.pixPerMeter);
            thisObject.trajectoryModel.yP0 = -yInPix/(thisObject.mainView.pixPerMeter);
            evt.updateAfterEvent();
            clickOffset = null;
            stage.removeEventListener( MouseEvent.MOUSE_UP, stopTargetDrag );
            stage.removeEventListener( MouseEvent.MOUSE_MOVE, dragTarget );
        }

        function dragTarget( evt: MouseEvent ): void {
            var xInPix:Number = thisObject.container.container.mouseX - clickOffset.x;    //screen coordinates, origin on left edge of stage
            var yInPix:Number = thisObject.container.container.mouseY - clickOffset.y;
            thisObject.trajectoryModel.xP0 = xInPix/(thisObject.mainView.pixPerMeter);
            thisObject.trajectoryModel.yP0 = -yInPix/(thisObject.mainView.pixPerMeter);
            evt.updateAfterEvent();
        }//end of dragTarget()


    } //end makeCarriageMovable

    public function update():void{
        this.barrel.rotation = -trajectoryModel.angleInDeg;
        //trace("cannonView.update called, angle = " + trajectoryModel.angleInDeg );
        this.x = this.trajectoryModel.xP0*mainView.pixPerMeter;
        this.y = -this.trajectoryModel.yP0*mainView.pixPerMeter;
        trace("cannonView.update called, x = " + this.trajectoryModel.xP0 );
    }


} //end class
} //end package
