/* Copyright 2010, University of Colorado */

package edu.colorado.phet.capacitorlab;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import edu.colorado.phet.capacitorlab.view.DielectricNode.DielectricChargeView;
import edu.colorado.phet.common.phetcommon.math.Point3D;
import edu.colorado.phet.common.phetcommon.util.DoubleRange;
import edu.colorado.phet.common.phetcommon.util.IntegerRange;

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
    
    public static final double WORLD_DRAG_MARGIN = 0.001; // meters
    
    public static final Point3D BATTERY_LOCATION = new Point3D.Double( 0.005, 0.034, 0 ); // meters
    public static final DoubleRange BATTERY_VOLTAGE_RANGE = new DoubleRange( -1.5, 1.5, 0 ); // Volts
    public static final double BATTERY_VOLTAGE_SNAP_TO_ZERO_THRESHOLD = 0.1; // Volts
    public static final boolean BATTERY_CONNECTED = true;
    
    public static final Point3D CAPACITOR_LOCATION = new Point3D.Double( BATTERY_LOCATION.getX() + 0.025, BATTERY_LOCATION.getY(), 0 ); // meters
    public static final DoubleRange PLATE_WIDTH_RANGE = new DoubleRange( 0.01, 0.02, 0.01 ); // meters
    public static final double PLATE_HEIGHT = 0.0005; // meters
    public static final DoubleRange PLATE_SEPARATION_RANGE = new DoubleRange( 0.005, 0.01, 0.01 ); // meters
    
    public static final DoubleRange DIELECTRIC_CONSTANT_RANGE = new DoubleRange( 1, 5, 5 ); // dimensionless
    public static final DoubleRange DIELECTRIC_OFFSET_RANGE = new DoubleRange( 0, PLATE_WIDTH_RANGE.getMax(), PLATE_WIDTH_RANGE.getDefault() ); // meters
    
    public static final double WIRE_THICKNESS = 0.0005; // meters
    public static final double WIRE_EXTENT = 0.016; // how far a wire extends beyond a capactor's origin in meters

    public static final double EPSILON_0 = 8.854E-12; // vacuum permittivity, aka electric constant (Farads/meter)
    
    // dielectric constants (dimensionless)
    public static final double EPSILON_VACUUM = 1;
    public static final double EPSILON_AIR = 1.0005896;
    public static final double EPSILON_GLASS = 4.7;
    public static final double EPSILON_PAPER = 3.5;
    public static final double EPSILON_TEFLON = 2.1;
    
    public static final Point3D PLATE_CHARGE_CONTROL_LOCATION = new Point3D.Double( CAPACITOR_LOCATION.getX() - 0.004, 0.001, 0 );
    public static final double PLATE_CHARGE_SNAP_TO_ZERO_THRESHOLD = 1.5E-13;
    
    public static final Point3D EFIELD_DETECTOR_PROBE_LOCATION = CAPACITOR_LOCATION; //XXX should probably start somewhere outside the capacitor
    
    public static final Point3D VOLTMETER_POSITIVE_PROBE_LOCATION = new Point3D.Double( BATTERY_LOCATION.getX() + 0.015, BATTERY_LOCATION.getY(), BATTERY_LOCATION.getZ() );
    public static final Point3D VOLTMETER_NEGATIVE_PROBE_LOCATION = new Point3D.Double( VOLTMETER_POSITIVE_PROBE_LOCATION.getX() + 0.005, VOLTMETER_POSITIVE_PROBE_LOCATION.getY(), VOLTMETER_POSITIVE_PROBE_LOCATION.getZ() );
    
    //----------------------------------------------------------------------------
    // View
    //----------------------------------------------------------------------------
    
    // reference coordinate frame size for world nodes
    public static final Dimension CANVAS_RENDERING_SIZE = new Dimension( 1024, 768 );
    
    // model-view transform
    public static final double MVT_SCALE = 15000; // scale factor when going from model to view
    public static final double MVT_YAW = Math.toRadians( -45 ); // rotation about the vertical axis, right-hand rule determines sign
    public static final double MVT_PITCH = Math.toRadians( 30 ); // rotation about the horizontal axis, right-hand rule determines sign
    
    public static final double PSWING_SCALE = 1.5;
    
    public static final double DRAG_HANDLE_ARROW_LENGTH = 35; // pixels
    
    // default exponents for the meters
    public static final int CAPACITANCE_METER_VALUE_EXPONENT = -13;
    public static final int PLATE_CHARGE_METER_VALUE_EXPONENT = -10;
    public static final int STORED_ENERGY_METER_VALUE_EXPONENT = -10;
    
    // visibility of meters
    public static final boolean CAPACITANCE_METER_VISIBLE = false;
    public static final boolean CHARGE_METER_VISIBLE = false;
    public static final boolean ENERGY_METER_VISIBLE = false;
    public static final boolean VOLTMETER_VISIBLE = false;//XXX should be false by default
    public static final boolean EFIELD_DETECTOR_VISIBLE = false;
    
    // default meter locations, view coordinates
    public static final Point2D CAPACITANCE_METER_LOCATION = new Point2D.Double( 600, 25 );
    public static final Point2D CHARGE_METER_LOCATION = new Point2D.Double( 750, 25 );
    public static final Point2D ENERGY_METER_LOCATION = new Point2D.Double( 900, 25 );
    public static final Point2D VOLTMETER_BODY_LOCATION = new Point2D.Double( 850, 350 );
    public static final Point2D EFIELD_DETECTOR_BODY_LOCATION = new Point2D.Double( 650, 615 );
    
    // plate charges
    public static final boolean PLATE_CHARGES_VISIBLE = true; 
    public static final IntegerRange NUMBER_OF_PLATE_CHARGES = new IntegerRange( 1, 625 );
    public static final Dimension MINUS_CHARGE_SIZE = new Dimension( 7, 2 );
    
    // dielectric charges
    public static final DielectricChargeView DIELECTRIC_CHARGE_VIEW = DielectricChargeView.TOTAL;
    
    // E-field
    public static final boolean EFIELD_VISIBLE = false;
    public static final DoubleRange EFIELD_SPACING_RANGE = new DoubleRange( PLATE_WIDTH_RANGE.getMin() / 15, PLATE_WIDTH_RANGE.getMin() / 2 ); // meters
    public static final IntegerRange NUMBER_OF_EFIELD_LINES = new IntegerRange( 4, 900 ); // number of lines on smallest plate
    
    // E-field detector
    public static final boolean EFIELD_PLATE_VECTOR_VISIBLE = true;
    public static final boolean EFIELD_DIELECTRIC_VECTOR_VISIBLE = true;
    public static final boolean EFIELD_SUM_VECTOR_VISIBLE = true;
    public static final boolean EFIELD_VALUES_VISIBLE = true;
}
