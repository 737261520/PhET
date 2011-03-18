// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.bendinglight.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.colorado.phet.bendinglight.modules.intro.ResetModel;
import edu.colorado.phet.bendinglight.view.LaserColor;
import edu.colorado.phet.bendinglight.view.LaserView;
import edu.colorado.phet.common.phetcommon.math.Function;
import edu.colorado.phet.common.phetcommon.model.clock.ClockAdapter;
import edu.colorado.phet.common.phetcommon.model.clock.ClockEvent;
import edu.colorado.phet.common.phetcommon.model.clock.ConstantDtClock;
import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.util.RichSimpleObserver;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.phetcommon.util.function.Function1;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction0;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.common.phetcommon.view.util.VisibleColor;

import static java.lang.Math.pow;

public class BendingLightModel implements ResetModel {
    protected static final double DEFAULT_DIST_FROM_PIVOT = 8.125E-6;
    protected final List<LightRay> rays = new LinkedList<LightRay>();
    private ConstantDtClock clock;
    public final Property<LaserView> laserView = new Property<LaserView>( LaserView.RAY );

    public static final MediumState VACUUM = new MediumState( "Vacuum", 1.0 );
    public static final MediumState AIR = new MediumState( "Air", 1.000293 );
    public static final MediumState WATER = new MediumState( "Water", 1.333 );
    public static final MediumState GLASS = new MediumState( "Glass", 1.5 );
    public static final MediumState DIAMOND = new MediumState( "Diamond", 2.419 );
    public static final MediumState MYSTERY_A = new MediumState( "Mystery A", DIAMOND.index, true );
    public static final MediumState MYSTERY_B = new MediumState( "Mystery B", 1.4, true );

    public final DispersionFunction environmentDispersion; //(wavelength,mediumBaseIndexOfRefraction) => true index of refraction
    public final DispersionFunction prismDispersion;

    public Property<Function1<Double, Color>> colorMappingFunction = new Property<Function1<Double, Color>>( new Function1<Double, Color>() {
        public Color apply( Double value ) {
            if ( value < WATER.index ) {
                double ratio = new Function.LinearFunction( 1.0, WATER.index, 0, 1 ).evaluate( value );
                return colorBlend( AIR_COLOR, WATER_COLOR, ratio );
            }
            else if ( value < GLASS.index ) {
                double ratio = new Function.LinearFunction( WATER.index, GLASS.index, 0, 1 ).evaluate( value );
                return colorBlend( WATER_COLOR, GLASS_COLOR, ratio );
            }
            else if ( value < DIAMOND.index ) {
                double ratio = new Function.LinearFunction( GLASS.index, DIAMOND.index, 0, 1 ).evaluate( value );
                return colorBlend( GLASS_COLOR, DIAMOND_COLOR, ratio );
            }
            else {
                return DIAMOND_COLOR;
            }
        }

        public Color colorBlend( Color a, Color b, double ratio ) {
            return new Color(
                    (int) ( ( (float) a.getRed() ) * ( 1 - ratio ) + ( (float) b.getRed() ) * ratio ),
                    (int) ( ( (float) a.getGreen() ) * ( 1 - ratio ) + ( (float) b.getGreen() ) * ratio ),
                    (int) ( ( (float) a.getBlue() ) * ( 1 - ratio ) + ( (float) b.getBlue() ) * ratio ),
                    (int) ( ( (float) a.getAlpha() ) * ( 1 - ratio ) + ( (float) b.getAlpha() ) * ratio )
            );
        }
    } );

    public static final double SPEED_OF_LIGHT = 2.99792458e8;
    public static final double WAVELENGTH_RED = 650E-9;
    //A good size for the units being used in the sim; used to determine the dimensions of various model objects
    public static final double CHARACTERISTIC_LENGTH = WAVELENGTH_RED;

    final double modelWidth = CHARACTERISTIC_LENGTH * 62;
    final double modelHeight = modelWidth * 0.7;

    private ArrayList<VoidFunction1<LightRay>> rayAddedListeners = new ArrayList<VoidFunction1<LightRay>>();
    protected final Laser laser;
    protected final IntensityMeter intensityMeter = new IntensityMeter( modelWidth * 0.3, -modelHeight * 0.3, modelWidth * 0.4, -modelHeight * 0.3 );
    //Alphas may be ignored, see MediumNode
    public static final Color AIR_COLOR = Color.white;
    public static final Color WATER_COLOR = new Color( 198, 226, 246 );
    public static final Color GLASS_COLOR = new Color( 171, 169, 212 );
    public static final Color DIAMOND_COLOR = new Color( 78, 79, 164 );
    private final ArrayList<VoidFunction0> modelUpdateListeners = new ArrayList<VoidFunction0>();
    public Property<Double> wavelengthProperty;
    private ArrayList<VoidFunction0> resetListeners = new ArrayList<VoidFunction0>();

    public BendingLightModel( double laserAngle, boolean topLeftQuadrant, final double laserDistanceFromPivot ) {
        laser = new Laser( laserDistanceFromPivot, laserAngle, topLeftQuadrant );
        this.clock = new ConstantDtClock( 30.0 ) {{
            addClockListener( new ClockAdapter() {
                public void simulationTimeChanged( ClockEvent clockEvent ) {
                    for ( LightRay ray : rays ) {
                        ray.step( clockEvent.getSimulationTimeChange() );
                    }
                }
            } );
        }};
        new RichSimpleObserver() {
            public void update() {
                updateModel();
            }
        }.observe( laser.on, laser.pivot, laser.emissionPoint, intensityMeter.sensorPosition, intensityMeter.enabled, laser.color, laserView );

        wavelengthProperty = new Property<Double>( BendingLightModel.WAVELENGTH_RED ) {{
            addObserver( new VoidFunction1<Double>() {
                public void apply( Double value ) {
                    laser.color.setValue( new LaserColor.OneColor( value ) );
                }
            } );
        }};
        laserView.addObserver( new SimpleObserver() {
            public void update() {
                updateModel();//TODO: Maybe it would be better just to regenerate view, but now we just do this by telling the model to recompute and repopulate
                getLaser().wave.setValue( laserView.getValue() == LaserView.WAVE );// synchronize view and model representations of whether it is wave or not
            }
        } );
        final Function.LinearFunction dispersionFunction = new Function.LinearFunction( VisibleColor.MIN_WAVELENGTH / 1E9, VisibleColor.MAX_WAVELENGTH / 1E9, 0, 0.04 * 1.5 ); // A function that uses the default value for RED, and changes the index of refraction by +/- 0.04
        environmentDispersion = new DispersionFunction() {
            public double getIndexOfRefraction( double wavelength, double baseIndexOfRefraction ) {
                return baseIndexOfRefraction + dispersionFunction.evaluate( wavelength );
            }
        };
        prismDispersion = new DispersionFunction() {
            public double getIndexOfRefraction( double wavelength, double baseIndexOfRefraction ) {
                return baseIndexOfRefraction + dispersionFunction.evaluate( wavelength );
            }
        };
    }

    protected void addRay( LightRay ray ) {
        rays.add( ray );
        for ( VoidFunction1<LightRay> rayAddedListener : rayAddedListeners ) {
            rayAddedListener.apply( ray );
        }
    }

    public double getWidth() {
        return modelWidth;
    }

    public double getHeight() {
        return modelHeight;
    }

    public void addRayAddedListener( VoidFunction1<LightRay> listener ) {
        rayAddedListeners.add( listener );
    }

    public Laser getLaser() {
        return laser;
    }

    public ConstantDtClock getClock() {
        return clock;
    }

    public Iterable<? extends LightRay> getRays() {
        return rays;
    }

    public IntensityMeter getIntensityMeter() {
        return intensityMeter;
    }

    protected void clearModel() {
        for ( LightRay ray : rays ) {
            ray.remove();
        }
        rays.clear();
        intensityMeter.clearRayReadings();
    }

    public void updateModel() {
        clearModel();
        propagateRays();
        for ( VoidFunction0 modelUpdateListener : modelUpdateListeners ) {
            modelUpdateListener.apply();
        }
    }

    protected void propagateRays() {
    }

    public static double getTransmittedPower( double n1, double n2, double cosTheta1, double cosTheta2 ) {
        return 4 * n1 * n2 * cosTheta1 * cosTheta2 / ( pow( n1 * cosTheta1 + n2 * cosTheta2, 2 ) );
    }

    public static double getReflectedPower( double n1, double n2, double cosTheta1, double cosTheta2 ) {
        return pow( ( n1 * cosTheta1 - n2 * cosTheta2 ) / ( n1 * cosTheta1 + n2 * cosTheta2 ), 2 );
    }

    public void addModelUpdateListener( VoidFunction0 listener ) {
        modelUpdateListeners.add( listener );
    }

    public void addResetListener( VoidFunction0 listener ) {
        resetListeners.add( listener );
    }

    public void resetAll() {
        laser.resetAll();
        intensityMeter.resetAll();
        laserView.reset();
        for ( VoidFunction0 listener : resetListeners ) {
            listener.apply();
        }
    }
}
