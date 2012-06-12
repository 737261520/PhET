/**
 * Created with IntelliJ IDEA.
 * User: Duso
 * Date: 6/6/12
 * Time: 8:37 AM
 * To change this template use File | Settings | File Templates.
 */
package edu.colorado.phet.radiatingcharge.model {
import edu.colorado.phet.radiatingcharge.view.MainView;

import flash.events.TimerEvent;

import flash.utils.Timer;

//model of radiating E-field from a moving point charge
public class FieldModel {

    public var views_arr:Array;     //views associated with this model
    public var myMainView:MainView;

    private var c:Number;           //speed of light in pixels per second, charge cannot moving faster than c
    private var _xC:Number;         //x-position of charge in pixels
    private var _yC:Number;         //y-position of charge in pixels
    private var vX:Number;          //x-component of charge velocity
    private var vY:Number;          //y-component of charge velocity
    private var v:Number;           //speed of charge
    private var gamma:Number;       //gamma factor
    private var fX:Number;          //x-component of force on charge
    private var fY:Number;          //y-component of force on charge
    private var k:Number;           //spring constant of spring between charge and mouse
    private var m:Number;           //mass of charge
    private var _nbrLines:int;      //number of field lines coming from charge
    private var _nbrPhotonsPerLine:int //number of photons in a given field line
    private var cos_arr:Array;      //cosines of angles of the rays, CCW from horizontal, angle must be in radians
    private var theta_arr:Array;    //angles of rest frame rays (in rads)
    private var sin_arr:Array;      //sines of angles of the rays,
    private var _fieldLine_arr:Array;  //array of field lines

    private var _paused:Boolean;    //true if sim is paused

    private var _t:Number;          //time in arbitrary units
    private var _tLastPhoton: Number;	    //time of previous Photon emission
    private var tRate: Number;	    //1 = real time; 0.25 = 1/4 of real time, etc.
    private var dt: Number;  	    //default time step in seconds
    private var f:Number;           //frequency (nbr per second) of photons on a given field line emitted by charge
    private var delTPhoton:Number;  //time in sec between photon emission events
    private var msTimer: Timer;	    //millisecond timer
    private var stageW:int;
    private var stageH:int;


    public function FieldModel( myMainView ) {
        this.myMainView = myMainView;
        this.views_arr = new Array();
        this.stageW = this.myMainView.stageW;
        this.stageH = this.myMainView.stageH;
        this.c = this.stageW/2;    //8 seconds to cross height of stage
        this.k = 3;
        this.m = 1;
        this.vX = 0;
        this.vY = 0;
        this.fX = 0;
        this.fY = 0;
        this.v = Math.sqrt( vX*vX + vY*vY );
        this.gamma = 1/Math.sqrt( 1 - this.v/this.c );
        this._nbrLines = 10;
        this._nbrPhotonsPerLine = 20;
        this.theta_arr = new Array( this._nbrLines );
        this.cos_arr = new Array( this._nbrLines );
        this.sin_arr = new Array( this._nbrLines );
        this._fieldLine_arr = new Array ( this._nbrLines );


        this.initialize();
    }//end constructor

    private function initialize():void{
        for(var i:int = 0; i < this._nbrLines; i++ ){
           this.theta_arr[i] = i*2*Math.PI/this._nbrLines  //Math.PI/4;
//           if( theta_arr[i] > Math.PI ){
//               theta_arr[i] -= 2*Math.PI;
//           }
           this.cos_arr[i] = Math.cos( theta_arr[i] ) ;
           this.sin_arr[i] = Math.sin( theta_arr[i] ) ;
        }
        this._xC = 0; //this.stageW/2;
        this._yC = 0; //this.stageH/2;
        //populate rays with photons
        for( var i:int = 0; i < this._nbrLines ; i++ ) {
            this._fieldLine_arr[i] = new Array( this._nbrPhotonsPerLine );
            for (var j:int = 0; j < this._nbrPhotonsPerLine; j++ ){
                this._fieldLine_arr[i][j]= new Photon( _xC,  _yC );     //each photon has a position (x,y) and direction vector (cos, sin)
            }
        } //end for loop
        this._paused = false;
        this._t = 0;
        this._tLastPhoton = 0;
        this.dt = 0.03;
        this.f = 10;//0.2*1/dt ;           //f should be less than 1/dt
        this.delTPhoton = 0.1; //this.f;
        this.tRate = 1;
        this.msTimer = new Timer( this.dt * 1000 );   //argument of Timer constructor is time step in ms
        this.msTimer.addEventListener( TimerEvent.TIMER, stepForward );
        //this.initializeFieldLines();
        this.updateViews();
        this.startRadiation();
    }

    public function get nbrLines():int{
        return this._nbrLines
    }
    
    public function get nbrPhotonsPerLine():int{
        return this._nbrPhotonsPerLine
    }

    public function getDT():Number{
        return this.dt;
    }

    public function get fieldLine_arr():Array{
        return this._fieldLine_arr;
    }
    
    public function get t():Number{
        return this._t;
    }

    public function get xC():Number{
        return this._xC;
    }

    public function get yC():Number{
        return this._yC;
    }

    public function get paused():Boolean{
        return this._paused;
    }

    public function set paused( tOrF:Boolean ):void{
        this._paused = tOrF;
        if( tOrF ){
            this.stopRadiation();
        }else{
            this.startRadiation();
        }
    } //end set Paused


//
//    public function set _xC(xPos:Number):void{
//        this._xC = xPos;
//    }
//
//    public function set _yC(yPos:Number):void{
//        this._yC = yPos;
//    }
//
//    public function setXY( xPos:Number, yPos:Number ):void{
//        this._xC = xPos;
//        this._yC = yPos;
//        this.updateViews();
//    }
    
    public function setForce( delX:Number, delY:Number ):void{
        this.fX = this.k*delX;
        this.fY = this.k*delY;
        //this.moveCharge();
    }

    public function registerView( view: Object ): void {
        this.views_arr.push( view );
    }

    public function unregisterView( view: Object ):void{
        var indexLocation:int = -1;
        indexLocation = this.views_arr.indexOf( view );
        if( indexLocation != -1 ){
            this.views_arr.splice( indexLocation, 1 )
        }
    }

    private function initializeFieldLines():void{
        for( var i:int = 0; i < this._nbrLines; i++ ){
            for( var j:int = 0; j < this._nbrPhotonsPerLine; j++ ){
                this._fieldLine_arr[i][j].xP = this._xC + this.cos_arr[i]*j*this.stageW/(2*this._nbrPhotonsPerLine );
                this._fieldLine_arr[i][j].yP = this._yC + this.sin_arr[i]*j*this.stageW/(2*this._nbrPhotonsPerLine );
                //this._fieldLine_arr[i][j][0] = this._xC + this.cos_arr[i]*j*this.stageW/(2*this._nbrPhotonsPerLine );
                //this._fieldLine_arr[i][j][1] = this._yC + this.sin_arr[i]*j*this.stageW/(2*this._nbrPhotonsPerLine );
            }
        }
    }

    private function moveCharge():void{
        var beta:Number = this.v/this.c;
        this.gamma = 1/Math.sqrt( 1 - beta*beta );
        _xC += vX*dt + 0.5*(fX/m)*dt*dt;
        _yC += vY*dt + 0.5*(fY/m)*dt*dt;
        var g3:Number = Math.pow( gamma, 3 );
        vX += fX*dt/( m*g3 );
        vY += fY*dt/( m*g3 );
        v = Math.sqrt( vX*vX + vY*vY );
        if( v > c ){
            while( v >= c ){
                trace( "error: v > c, beta = " + this.v/this.c );
                vX = 0.99*vX*c/v;
                vY = 0.99*vY*c/v;
                v = Math.sqrt( vX*vX + vY*vY );
            }
        }
    }//end moveCharge()
    
    //this algorithm is incorrect.
    private function stepForward( evt: TimerEvent ):void{
        this._t += this.dt;
        if( this._t > this._tLastPhoton + this.delTPhoton ){
            this._tLastPhoton = this._t;
            this.emitPhotons();
        }
        for( var i:int = 0; i < this._nbrLines; i++ ){  //for each ray
            for( var j:int = 0; j < this._nbrPhotonsPerLine; j++ ){    //for each photon in a ray.
                this._fieldLine_arr[i][j].xP += _fieldLine_arr[i][j].cos*this.c*this.dt; //this.cos_arr[i]*this.c*this.dt;
                this._fieldLine_arr[i][j].yP += _fieldLine_arr[i][j].sin*this.c*this.dt; //this.sin_arr[i]*this.c*this.dt;
            }
        }
        this.moveCharge();
        this.updateViews();
        //trace("FieldModel._xC: "+this._xC) ;
        //evt.updateAfterEvent();
    }//end stepForward()

    private function emitPhotons():void{
        //add new photon to 1st element of photon array
        for( var i:int = 0; i < this._nbrLines; i++ ){
            //recycle photon at end of ray and place at front end of ray
            this._fieldLine_arr[i].unshift( _fieldLine_arr[i][nbrPhotonsPerLine - 1])
            // remove last element of ray
            this._fieldLine_arr[i].splice( this.nbrPhotonsPerLine );
        }
        //initialize photons at front end of rays
        this.initializeEmittedPhotons();
        //
        //trace("FieldModel.emitPhotons._fieldLine_arr[2].length = "+this._fieldLine_arr[2].length);
        //trace("FieldModel.emitPhotons:  beta = " + this.v/this.c + "    gamma = " + this.gamma);

    }//end emitPhotons()

    private function initializeEmittedPhotons():void{
        //TESTing code

//        for( var i:int = 0; i < this._nbrLines; i++ ){
//            this._fieldLine_arr[i][0].xP = this._xC;
//            this._fieldLine_arr[i][0].yP = this._yC;
//            this._fieldLine_arr[i][0].cos = this.cos_arr[i];
//            this._fieldLine_arr[i][0].sin = this.sin_arr[i];
//        }
        //end of TESTing code

        var beta:Number = this.v/this.c;
        var inverseGamma = Math.sqrt( 1 - beta*beta );
        var thetaC:Number = Math.atan2( this.vY, this.vX );   //angle of velocity vector of charge
        //keep all angles between 0 and +2*pi radians
        if( thetaC < 0 ){
            thetaC += 2*Math.PI;
        }
        //trace( "FieldModel: vX = " + this.vX + "     vY = " + this.vY + "    theta = " + 180*thetaC/Math.PI );
        if( beta < 0.001 ){
            thetaC = 0;
        }
        var cosThetaC:Number = Math.cos( thetaC );
        var sinThetaC:Number = Math.sin( thetaC );
        //trace("cos = "+cosThetaC+"    sin = " + sinThetaC )
        for( var i:int = 0; i < this._nbrLines; i++ ){
            //reset positions
            this._fieldLine_arr[i][0].xP = this._xC;
            this._fieldLine_arr[i][0].yP = this._yC;
            var thetaU:Number = this.theta_arr[i] - thetaC;
            if(thetaU < - Math.PI ){
                thetaU += 2*Math.PI;
            }else if( thetaU > Math.PI ){
                thetaU -= 2*Math.PI;
            }
            var cosThetaU:Number = Math.cos( thetaU );
            var sinThetaU:Number = Math.sin( thetaU );
            var cosThetaFinal:Number = ( (cosThetaU + beta)*cosThetaC - (inverseGamma)*sinThetaU*sinThetaC ) /( 1 + beta*cosThetaU );
            var sinThetaFinal:Number = ( (cosThetaU + beta)*sinThetaC - (inverseGamma)*sinThetaU*cosThetaC ) /( 1 + beta*cosThetaU );
            this._fieldLine_arr[i][0].cos = cosThetaFinal;
            this._fieldLine_arr[i][0].sin = sinThetaFinal;
//            this._fieldLine_arr[i][0].cos = this.cos_arr[i];
//            this._fieldLine_arr[i][0].sin = this.sin_arr[i];
            //trace("FieldModel.initializeEmittedPhotons. theta of charge = " + thetaCharge )
            // remove last element of ray
            //trace("FieldModel, ray = "+i+"   thetaC = "+thetaC+ "  theta_arr[0] = "+theta_arr[i]+ "   thetaU = "+ thetaU);
        }
    }//end initializeEmittedPhotons
    


    public function startRadiation():void{
        this.msTimer.start();
    }

    public function stopRadiation():void{
        this.msTimer.stop();
    }

    public function updateViews(): void {
        for(var i:int = 0; i < this.views_arr.length; i++){
            this.views_arr[ i ].update();
        }
        //this.view.update();
    }//end updateView()

} //end of class
} //end of package
