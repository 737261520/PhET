// Copyright 2002-2012, University of Colorado

/**
 * Stage, sets up the scenegraph.
 * Uses composition of Easel.Stage, inheritance proved to be problematic.
 *
 * @author Chris Malley (PixelZoom, Inc.)
 */
define( [
            'easel',
            'common/Property',
            'view/BarMagnetDisplay',
            'view/CompassDisplay',
            'view/FieldDisplay',
            'view/FieldInsideDisplay',
            'view/FieldMeterDisplay'
        ],
        function ( Easel, Property, BarMagnetDisplay, CompassDisplay, FieldDisplay, FieldInsideDisplay, FieldMeterDisplay ) {

            function FaradayStage( canvas, model ) {

                // view-specific properties (have no model counterpart.)
                this.magnetTransparent = new Property( false );
                this.magnetTransparent.addObserver( function () {
                    //TODO make "inside field" display object visible
                } );

                // stage
                this.stage = new Easel.Stage( canvas );
                this.stage.enableMouseOver();

                // black background
                var background = new Easel.Shape();
                background.graphics.beginFill( 'black' );
                background.graphics.rect( 0, 0, canvas.width, canvas.height );

                // field
                this.field = new FieldDisplay( model.field, model.mvt );

                // bar magnet
                this.barMagnet = new BarMagnetDisplay( model.barMagnet, model.mvt );

                // field inside magnet
                var fieldInside = new FieldInsideDisplay( model.barMagnet, model.mvt );

                // compass
                this.compass = new CompassDisplay( model.compass, model.mvt );

                // field meter
                this.meter = new FieldMeterDisplay( model.fieldMeter, model.mvt );

                // rendering order
                this.stage.addChild( background );
                this.stage.addChild( this.field );
                this.stage.addChild( this.barMagnet );
                this.stage.addChild( fieldInside );
                this.stage.addChild( this.compass );
                this.stage.addChild( this.meter );
            }

            // Resets all view-specific properties
            FaradayStage.prototype.reset = function () {
                this.magnetTransparent.reset();
            };

            return FaradayStage;
        } );
