// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.lightreflectionandrefraction.model;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.colorado.phet.common.phetcommon.math.Function;
import edu.colorado.phet.common.phetcommon.math.ImmutableVector2D;
import edu.colorado.phet.common.phetcommon.math.MathUtil;
import edu.colorado.phet.common.phetcommon.model.Property;
import edu.colorado.phet.common.phetcommon.model.clock.ClockAdapter;
import edu.colorado.phet.common.phetcommon.model.clock.ClockEvent;
import edu.colorado.phet.common.phetcommon.model.clock.ConstantDtClock;
import edu.colorado.phet.common.phetcommon.model.clock.IClock;
import edu.colorado.phet.common.phetcommon.util.Function1;
import edu.colorado.phet.common.phetcommon.util.VoidFunction0;
import edu.colorado.phet.common.phetcommon.util.VoidFunction1;
import edu.colorado.phet.lightreflectionandrefraction.util.RichSimpleObserver;

import static java.lang.Math.pow;

public class LRRModel {
    protected final List<LightRay> rays = new LinkedList<LightRay>();
    private ConstantDtClock clock;

    public static final MediumState VACUUM = new MediumState( "Vacuum", 1.0 );
    public static final MediumState AIR = new MediumState( "Air", 1.000293 );
    public static final MediumState WATER = new MediumState( "Water", 1.333 );
    public static final MediumState GLASS = new MediumState( "Glass", 1.5 );
    public static final MediumState DIAMOND = new MediumState( "Diamond", 2.419 );
    public static final MediumState MYSTERY_A = new MediumState( "Mystery A", DIAMOND.index ) {
        public boolean isMystery() {
            return true;
        }
    };
    public static final MediumState MYSTERY_B = new MediumState( "Mystery B", 1.4 ) {
        public boolean isMystery() {
            return true;
        }
    };

    //TODO: some of this code is duplicated with the other instantiations of color mapping function
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

    final double modelWidth = WAVELENGTH_RED * 62;
    final double modelHeight = modelWidth * 0.7;

    private ArrayList<VoidFunction1<LightRay>> rayAddedListeners = new ArrayList<VoidFunction1<LightRay>>();
    protected final Laser laser = new Laser( 8.125E-6 );
    protected final IntensityMeter intensityMeter = new IntensityMeter( modelWidth * 0.3, -modelHeight * 0.3, modelWidth * 0.4, -modelHeight * 0.3 );
    //Alphas may be ignored, see MediumNode
    public static final Color AIR_COLOR = Color.white;
    public static final Color WATER_COLOR = new Color( 198, 226, 246 );
    public static final Color GLASS_COLOR = new Color( 171, 169, 212 );
    public static final Color DIAMOND_COLOR = new Color( 78, 79, 164 );
    private final ArrayList<VoidFunction0> modelUpdateListeners = new ArrayList<VoidFunction0>();

    public LRRModel() {
        this.clock = new ConstantDtClock( 30.0 );

        new RichSimpleObserver() {
            public void update() {
                updateModel();
            }
        }.observe( laser.on, laser.angle, intensityMeter.sensorPosition, intensityMeter.enabled, laser.color );

        clock.addClockListener( new ClockAdapter() {
            public void simulationTimeChanged( ClockEvent clockEvent ) {
                for ( LightRay ray : rays ) {
                    ray.step( clockEvent.getSimulationTimeChange() );
                }
            }
        } );
    }

    /*
     Checks whether the intensity meter should absorb the ray, and if so adds a truncated ray.
     If the intensity meter misses the ray, the original ray is added.
     */
    protected boolean addAndAbsorb( LightRay ray ) {
        boolean rayAbsorbed = ray.intersects( intensityMeter.getSensorShape() ) && intensityMeter.enabled.getValue();
        if ( rayAbsorbed ) {
            Point2D[] intersects = MathUtil.getLineCircleIntersection( intensityMeter.getSensorShape(), ray.toLine2D() );
            if ( intersects != null && intersects[0] != null && intersects[1] != null ) {
                double x = intersects[0].getX() + intersects[1].getX();
                double y = intersects[0].getY() + intersects[1].getY();
                LightRay interrupted = new LightRay( ray.tail.getValue(), new ImmutableVector2D( x / 2, y / 2 ), 1.0, ray.getWavelength(), ray.getPowerFraction(), laser.color.getValue().getColor(),
                                                     ray.getWaveWidth(), ray.getNumWavelengthsPhaseOffset(), ray.getOppositeMedium(), ray.phase.getValue(), false, ray.extendBackwards );
                boolean isForward = ray.toVector2D().dot( interrupted.toVector2D() ) > 0;
                if ( interrupted.getLength() < ray.getLength() && isForward ) {
                    addRay( interrupted );
                }
                else {
                    addRay( ray );
                    rayAbsorbed = false;
                }
            }
        }
        else {
            addRay( ray );
        }
        if ( rayAbsorbed ) {
            intensityMeter.addRayReading( new Reading( ray.getPowerFraction() ) );
        }
        else {
            intensityMeter.addRayReading( Reading.MISS );
        }
        return rayAbsorbed;
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

    public IClock getClock() {
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
}
