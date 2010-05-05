/* Copyright 2010, University of Colorado */

package edu.colorado.phet.capacitorlab;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import edu.colorado.phet.common.phetcommon.util.DoubleRange;

/**
 * A collection of constants that configure global properties.
 * If you change something here, it will change *everywhere* in this simulation.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class CLConstants {

    /* Not intended for instantiation. */
    private CLConstants() {}
    
    //----------------------------------------------------------------------------
    // Debugging
    //----------------------------------------------------------------------------
    
    // enable debug output for canvas layout updates
    public static final boolean DEBUG_CANVAS_UPDATE_LAYOUT = false;
    
    //----------------------------------------------------------------------------
    // Application
    //----------------------------------------------------------------------------
    
    public static final String PROJECT_NAME = "capacitor-lab";
    
    //----------------------------------------------------------------------------
    // Model
    //----------------------------------------------------------------------------
    
    public static final Point2D BATTERY_LOCATION = new Point2D.Double( -130, 0 ); // mm
    public static final DoubleRange BATTERY_VOLTAGE_RANGE = new DoubleRange( -10, 10, 0 ); // volts
    public static final boolean BATTERY_CONNECTED = true;
    
    public static final Point2D CAPACITOR_LOCATION = new Point2D.Double( 0, 0 ); // mm
    public static final DoubleRange PLATE_SIZE_RANGE = new DoubleRange( 50, 100, 75 ); // mm
    public static final DoubleRange PLATE_SEPARATION_RANGE = new DoubleRange( 10, 50, 20 ); // mm
    public static final double PLATE_THICKNESS = 1.5; // mm
    
    public static final DoubleRange DIELECTRIC_CONSTANT_RANGE = new DoubleRange( 1, 5, 1 ); // dimensionless
    public static final DoubleRange DIELECTRIC_OFFSET_RANGE = new DoubleRange( 0, PLATE_SIZE_RANGE.getMax(), 0 ); // mm
    public static final double DIELECTRIC_GAP = 1.0; // mm
    
    public static final double WIRE_THICKNESS = 3; // mm
    public static final double TOP_WIRE_EXTENT = Math.abs( 85 ); // how far the top wire extends above the capactor's origin, absolute value (mm)
    public static final double BOTTOM_WIRE_EXTENT = TOP_WIRE_EXTENT; // how far the bottom wire extends below the capactor's origin, absolute value (mm)
    
    // model-view transform
    public static final double MVT_SCALE = 3;
    public static final Point2D MVT_OFFSET = new Point2D.Double( Math.abs( BATTERY_LOCATION.getX() ) + 60, 125 ); // mm
    
    //----------------------------------------------------------------------------
    // View
    //----------------------------------------------------------------------------
    
    // reference coordinate frame size for world nodes
    public static final Dimension CANVAS_RENDERING_SIZE = new Dimension( 1024, 768 );
    
    public static final double VIEWING_ANGLE = Math.toRadians( 45 ); // angle of the bottom left corner in the top plate's parallelogram (radians)
    public static final double FORESHORTENING_FACTOR = 0.5; // how much lines going away from the viewer should be shortened (dimensionless)
    
    public static final double PSWING_SCALE = 1.5;
    
    public static final double DRAG_HANDLE_ARROW_LENGTH = 35; // pixels
    
    //----------------------------------------------------------------------------
    // Control
    //----------------------------------------------------------------------------
    
}
