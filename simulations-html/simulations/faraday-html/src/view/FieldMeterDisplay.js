// Copyright 2002-2012, University of Colorado

/**
 * Display object for the field meter.
 *
 * @author Chris Malley (PixelZoom, Inc.)
 */
define( [ 'easel', 'view/DragHandler' ], function( Easel, DragHandler ) {

    /**
     * @param {FieldMeter} fieldMeter
     * @param {ModelViewTransform} mvt
     * @constructor
     */
    function FieldMeterDisplay( fieldMeter, mvt ) {
        // constructor stealing
        Easel.Text.call( this, "meter", "bold 36px Arial", 'white' );

        //XXX center
        this.textAlign = 'center';
        this.textBaseline = 'middle';

        // Dragging.
        DragHandler.register( this, function( point ) {
            fieldMeter.location.set( mvt.viewToModel( point ) );
        });

        // Register for synchronization with model.
        var thisDisplayObject = this;
        function updateLocation( location ) {
            var point = mvt.modelToView( location );
            thisDisplayObject.x = point.x;
            thisDisplayObject.y = point.y;
        }
        fieldMeter.location.addObserver( updateLocation );

        // sync now
        updateLocation( fieldMeter.location.get() );
    }

    // prototype chaining
    FieldMeterDisplay.prototype = new Easel.Text();

    return FieldMeterDisplay;
} );
