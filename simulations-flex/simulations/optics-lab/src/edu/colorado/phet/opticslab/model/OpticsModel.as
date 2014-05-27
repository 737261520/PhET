
/**
 * Created with IntelliJ IDEA.
 * User: Duso
 * Date: 5/21/2014
 * Time: 8:37 AM
 * To change this template use File | Settings | File Templates.
 */
package edu.colorado.phet.opticslab.model {
import edu.colorado.phet.opticslab.view.LightSourceView;
import edu.colorado.phet.opticslab.view.MainView;

import flash.geom.Point;


//model of Optics Lab sim: sources, lenses, mirrors, masks, etc
public class OpticsModel {

    public var views_arr:Array;     //views associated with this model
    public var myMainView:MainView; //communications hub for model-view-controller
    public var source_arr: Array; //light sources
    private var _nbrSources: int;    //number of light sources on stage
    private var _nbrComponents: int;    //number of components (masks, lenses, mirrors) on stage
    public var component_arr: Array;   //lenses, mirrors, masks
    public var newSourceCreated: Boolean;       //true if new source added to play area, sends update to layout view if true
    public var newMaskCreated: Boolean;         //true if new mask added to play area, sends update to layout view if true
    public var testSource: LightSource;         //for testing only



    public function OpticsModel( myMainView: MainView ) {
        this.myMainView = myMainView;
        this.views_arr = new Array();
        this.source_arr = new Array();
        this._nbrSources = 0;
        this._nbrComponents = 0;
        this.newSourceCreated = false;
        this.newMaskCreated = false;
        this.component_arr = new Array();
        //this.testSource = new LightSource( this );

        this.initialize();
    }//end constructor


    private function initialize():void{
        //trace("OpticsModel.initialize called.")
        this.updateViews();
    }  //end initialize()

    public function testRayTracing():void{
        this.testSource = new LightSource( this, _nbrSources );
        _nbrSources += 1;
    }

    public function createNewLightSource():void{
        var index: uint = _nbrSources;
        var newSource: LightSource = new LightSource( this, index );
        newSource.setLocation( Math.random()*0.5, Math.random()*0.5 );  //for testing only
        source_arr[ index ] = newSource;
        this.newSourceCreated = true;
        //myMainView.myLayoutView.createNewLightSourceView( index );   //breaks encapsulation of model, must be a better way
        this.updateViews();
        this.newSourceCreated = false;
        _nbrSources += 1;
    }

    public function createNewMask():void{
        var index: uint = _nbrComponents;
        var newMask: Mask = new Mask( this, index );
        newMask.setLocation( Math.random()*0.5, Math.random()*0.5 );  //for testing only
        component_arr[ index ] = newMask;
        this.newMaskCreated = true;
        //myMainView.myLayoutView.createNewLightSourceView( index );   //breaks encapsulation of model, must be a better way
        this.updateViews();
        this.newMaskCreated = false;
        _nbrComponents += 1;
    }

    public function destroyLightSource( index: uint ):void{

    }

    public function getXYOfSource( idx: uint ):Point{
        var sourceLocation:Point = new Point();
        sourceLocation.x = source_arr[ idx ].x;
        sourceLocation.y = source_arr[ idx ].y;
        return sourceLocation;
    }


    public function unregisterLightSource( lightSource: LightSource ): void{
        var indexLocation:int = -1;
        indexLocation = this.source_arr.indexOf( lightSource );
        if( indexLocation != -1 ){
            this.source_arr.splice( indexLocation, 1 );
        }
        this._nbrSources -= 1;
        if( _nbrSources < 0 ){trace("ERROR: OpticsModel.unregisterLightSource.  Nbr of light souces is negative.")}
    }

    public function registerView( view: Object ): void {
        this.views_arr.push( view );
        //if( ){};
    }

    public function unregisterView( view: Object ):void{
        var indexLocation:int = -1;
        indexLocation = this.views_arr.indexOf( view );
        if( indexLocation != -1 ){
            this.views_arr.splice( indexLocation, 1 )
        }
    }

    public function get nbrSources(): int {
        return _nbrSources;
    }

    public function get nbrComponents(): int {
        return _nbrComponents;
    }

    public function updateViews(): void {
        for(var i:int = 0; i < this.views_arr.length; i++){
            this.views_arr[ i ].update();
        }
    }//end updateView()



} //end of class
} //end of package
