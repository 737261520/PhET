// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.bendinglight.model;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import edu.colorado.phet.common.phetcommon.math.ImmutableVector2D;
import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction0;

import static edu.colorado.phet.bendinglight.model.BendingLightModel.SPEED_OF_LIGHT;

/**
 * @author Sam Reid
 */
public class LightRay {
    public final Property<ImmutableVector2D> tip;
    public final Property<ImmutableVector2D> tail;
    //    public final Property<Double> phase;
    public final double indexOfRefraction;
    public final double wavelength; // wavelength in meters
    private final double powerFraction;
    private ArrayList<VoidFunction0> removalListeners = new ArrayList<VoidFunction0>();
    private ArrayList<VoidFunction0> moveToFrontListeners = new ArrayList<VoidFunction0>();
    private Color color;
    private double waveWidth;
    private double numWavelengthsPhaseOffset;
    private final Shape oppositeMedium;
    public final boolean extendBackwards;
    private boolean extend;
    private double time;

    public LightRay( ImmutableVector2D tail, ImmutableVector2D tip, double indexOfRefraction, double wavelength,
                     double powerFraction, Color color, double waveWidth,
                     double numWavelengthsPhaseOffset,//This number indicates how many wavelengths have passed before this light ray begins; it is zero for the light coming out of the laser.
                     Shape oppositeMedium,//for clipping
                     boolean extend,//has to be an integral number of wavelength so that the phases work out correctly, turing this up too high past 1E6 causes things not to render properly
                     boolean extendBackwards ) {
        this.oppositeMedium = oppositeMedium;
        this.extendBackwards = extendBackwards;
//        this.phase = new Property<Double>( initialPhase );
        this.color = color;
        this.waveWidth = waveWidth;
        this.tip = new Property<ImmutableVector2D>( tip );
        this.tail = new Property<ImmutableVector2D>( tail );
        this.indexOfRefraction = indexOfRefraction;
        this.wavelength = wavelength;
        this.powerFraction = powerFraction;
        this.numWavelengthsPhaseOffset = numWavelengthsPhaseOffset;
        this.extend = extend;
    }

    public void addRemovalListener( VoidFunction0 listener ) {
        removalListeners.add( listener );
    }

    public void addMoveToFrontListener( VoidFunction0 listener ) {
        moveToFrontListeners.add( listener );
    }

    public void addObserver( SimpleObserver simpleObserver ) {
        tip.addObserver( simpleObserver );
        tail.addObserver( simpleObserver );
    }

    public double getSpeed() {
        return SPEED_OF_LIGHT / indexOfRefraction;
    }

    public void remove() {
        for ( VoidFunction0 removalListener : removalListeners ) {
            removalListener.apply();
        }
        tip.removeAllObservers();
        tail.removeAllObservers();
        removalListeners.clear();
    }

    public double getPowerFraction() {
        return powerFraction;
    }

    public boolean intersects( Shape sensorShape ) {
        return !new Area( sensorShape ) {{
            intersect( new Area( new BasicStroke( 1E-10f ).createStrokedShape( toLine2D() ) ) );
        }}.isEmpty();
    }

    public Line2D toLine2D() {
        return new Line2D.Double( tail.getValue().toPoint2D(), tip.getValue().toPoint2D() );
    }

    public double getLength() {
        return tip.getValue().minus( tail.getValue() ).getMagnitude();
    }

    public ImmutableVector2D toVector2D() {
        return new ImmutableVector2D( tail.getValue().toPoint2D(), tip.getValue().toPoint2D() );
    }

    public Color getColor() {
        return color;
    }

    public double getWavelength() {
        return wavelength;
    }

    public void moveToFront() {
        for ( VoidFunction0 moveToFrontListener : moveToFrontListeners ) {
            moveToFrontListener.apply();
        }
    }

    @Override
    public String toString() {
        return "tail = " + tail + ", tip = " + tip;
    }

    public double getExtensionFactor() {
        if ( extendBackwards ||//fill in the triangular chip near y=0 even for truncated beams, if it is the transmitted beam
             extend ) {
            return wavelength * 1E6;
        }
        else {
            return 0;
        }
    }

    public Shape getWaveShape() {
        final BasicStroke stroke = new BasicStroke( (float) ( waveWidth ), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER );
        final Shape strokedShape = stroke.createStrokedShape( extendBackwards ? getExtendedLineBackwards() : getExtendedLine() );
        Area area = new Area( strokedShape ) {{
            if ( oppositeMedium != null ) { subtract( new Area( oppositeMedium ) ); }
        }};
        return area;
    }

    //Have to extend the line so that it can be clipped against the opposite medium, so it will won't show any missing triangular chips.
    private Line2D.Double getExtendedLine() {
        return new Line2D.Double( tail.getValue().toPoint2D(), tip.getValue().plus( getUnitVector().times( getExtensionFactor() ) ).toPoint2D() );
    }

    //Use this one for the transmitted beam
    private Line2D.Double getExtendedLineBackwards() {
        return new Line2D.Double( tail.getValue().plus( getUnitVector().times( -getExtensionFactor() ) ).toPoint2D(), tip.getValue().toPoint2D() );
    }

    public ImmutableVector2D getUnitVector() {
        return new ImmutableVector2D( tail.getValue().toPoint2D(), tip.getValue().toPoint2D() ).getNormalizedInstance();
    }

    public double getAngle() {
        return toVector2D().getAngle();
    }

    private ArrayList<VoidFunction0> stepListeners = new ArrayList<VoidFunction0>();

    public void addStepListener( VoidFunction0 stepListener ) {
        stepListeners.add( stepListener );
    }

    public void setTime( double time ) {
        this.time = time;
        for ( VoidFunction0 stepListener : stepListeners ) {
            stepListener.apply();
        }
//        System.out.println( "getFrequency() = " + getFrequency() );
    }

//    public void step( double dt ) {
//        //it moved v*dt meters
//        double distanceMoved = getSpeed() * dt;
//        double numberWavelengthsMoved = distanceMoved / getWavelength();
//        phase.setValue( phase.getValue() + numberWavelengthsMoved );
//    }

    public double getWaveWidth() {
        return waveWidth;
    }

    public double getNumberOfWavelengths() {
        return getLength() / wavelength;
    }

    public double getNumWavelengthsPhaseOffset() {
        return numWavelengthsPhaseOffset;
    }

    public Shape getOppositeMedium() {
        return oppositeMedium;
    }

    public boolean contains( ImmutableVector2D position, boolean waveMode ) {
        if ( waveMode ) {
            return getWaveShape().contains( position.getX(), position.getY() );
        }
        else {
            return new BasicStroke( (float) getRayWidth() ).createStrokedShape( toLine2D() ).contains( position.toPoint2D() );
        }
    }

    public double getRayWidth() {
        return 1.5992063492063494E-7;//At the default transform, this yields a 4 pixel wide stroke
    }

    public ImmutableVector2D getVelocity() {
        return tip.getValue().minus( tail.getValue() ).getNormalizedInstance().times( getSpeed() );
    }

    public double getFrequency() {
        return getSpeed() / getWavelength();
    }

    public double getAngularFrequency() {
        return getFrequency() * Math.PI * 2;
    }

    public double getPhaseOffset() {
//        System.out.println( "time = " + time );
        return getAngularFrequency() * time - 2 * Math.PI * numWavelengthsPhaseOffset;
    }

    public double getCosArg( double distanceAlongRay ) {
        double w = getAngularFrequency();
        double k = 2 * Math.PI / getWavelength();
        double x = distanceAlongRay;
        double t = time;

        return k * x - w * t - 2 * Math.PI * numWavelengthsPhaseOffset;
//        return getAngularFrequency() * time - numWavelengthsPhaseOffset * 2 * Math.PI;
//        double radians = getFrequency() * time * 2 * Math.PI;
//        return ( numWavelengthsPhaseOffset + distanceAlongRay / getWavelength() * time ) * 2 * Math.PI;
    }

    public double getTime() {
        return time;
    }
}
