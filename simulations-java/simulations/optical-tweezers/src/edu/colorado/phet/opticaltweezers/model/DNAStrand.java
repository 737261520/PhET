/* Copyright 2007, University of Colorado */

package edu.colorado.phet.opticaltweezers.model;

import java.awt.geom.Point2D;
import java.util.*;

import edu.colorado.phet.common.phetcommon.model.ModelElement;
import edu.colorado.phet.common.phetcommon.util.DoubleRange;
import edu.colorado.phet.common.phetcommon.util.IntegerRange;
import edu.colorado.phet.opticaltweezers.util.Vector2D;

/**
 * DNAStrand is the model of a double-stranded DNA immersed in a viscous fluid.
 * The head is attached to a bead, while the tail is pinned in place.
 * <p>
 * This model is unlikely to be useful in any other simulations.
 * The force model is based on physics. But the model of strand motion
 * is pure "Hollywood"; that is, it is intended to give the correct appearance 
 * but has no basis in reality. The strand is modeled as a set of pivot points,
 * connected by line segements.  Each line segment behaves like a spring.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class DNAStrand extends OTObservable implements ModelElement, Observer {

    //----------------------------------------------------------------------------
    // Public class data
    //----------------------------------------------------------------------------

    public static final String PROPERTY_FORCE = "force"; // force applied to the bead that is attached to the head
    public static final String PROPERTY_SHAPE = "shape"; // shape of the strand
    public static final String PROPERTY_SPRING_CONSTANT = "springConstant";
    public static final String PROPERTY_DAMPING_CONSTANT = "dampingConstant";
    public static final String PROPERTY_KICK_CONSTANT = "kickConstant";
    public static final String PROPERTY_NUMBER_OF_EVOLUTIONS_PER_CLOCK_TICK = "numberOfEvolutionsPerClockTick";

    // persistence length is a measure of the strand's bending stiffness
    public static final double DOUBLE_STRANDED_PERSISTENCE_LENGTH = 50; // nm
    public static final double SINGLE_STRANDED_PERSISTENCE_LENGTH = 1; // nm
    
    //----------------------------------------------------------------------------
    // Private class data
    //----------------------------------------------------------------------------

    private static final double DRAG_COEFFICIENT = 0.2; //XXX drag coefficient, should be based on fluid variables!
    
    private static final double INITIAL_STRETCHINESS = 0.95; // how much the strand is stretched initially, % of contour length

    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private Bead _bead;
    private Fluid _fluid;

    private final double _contourLength; // length of the DNA strand, nm
    private final double _persistenceLength; // nm
    private final int _numberOfSegments; // number of discrete segments used to model the strand

    private DNAPivot[] _pivots;
    private DoubleRange _springConstantRange;
    private DoubleRange _dampingConstantRange;
    private DoubleRange _kickConstantRange;
    private IntegerRange _numberOfEvolutionsPerClockTickRange;
    private double _springConstant;
    private double _dampingConstant;
    private double _kickConstant;
    private int _numberOfEvolutionsPerClockTick;
    private Random _kickRandom;

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    /**
     * Constructor.
     * 
     * @param contourLength
     * @param persistenceLength
     * @param numberOfSegments
     * @param tailPosition
     * @param springConstantRange
     * @param dampingConstantRange
     * @param kickConstantRange
     * @param numberOfEvolutionsPerClockTickRange
     * @param bead
     * @param fluid
     */
    public DNAStrand( double contourLength, double persistenceLength, int numberOfSegments, DoubleRange springConstantRange, DoubleRange dampingConstantRange, DoubleRange kickConstantRange, IntegerRange numberOfEvolutionsPerClockTickRange, Bead bead, Fluid fluid ) {

        _contourLength = contourLength;
        _persistenceLength = persistenceLength;
        _numberOfSegments = numberOfSegments;

        _bead = bead;
        _bead.addObserver( this );

        _fluid = fluid;
        _fluid.addObserver( this );

        _springConstantRange = springConstantRange;
        _dampingConstantRange = dampingConstantRange;
        _kickConstantRange = kickConstantRange;
        _numberOfEvolutionsPerClockTickRange = numberOfEvolutionsPerClockTickRange;

        _springConstant = _springConstantRange.getDefault();
        _dampingConstant = _dampingConstantRange.getDefault();
        _kickConstant = _kickConstantRange.getDefault();
        _numberOfEvolutionsPerClockTick = _numberOfEvolutionsPerClockTickRange.getDefault();
        
        _kickRandom = new Random();
        
        initializeStrand();
    }

    /**
     * Call this before releasing all references to this object.
     */
    public void cleanup() {
        _bead.deleteObserver( this );
        _fluid.deleteObserver( this );
    }

    //----------------------------------------------------------------------------
    // Setters and getters
    //----------------------------------------------------------------------------

    public double getContourLength() {
        return _contourLength;
    }

    public int getNumberOfSegments() {
        return _numberOfSegments;
    }
    
    public Point2D getHeadPosition() {
        return new Point2D.Double( getHeadX(), getHeadY() );
    }

    public double getHeadX() {
        return getHeadPivot().getX();
    }

    public double getHeadY() {
        return getHeadPivot().getY();
    }

    public Point2D getTailPosition() {
        return new Point2D.Double( getTailX(), getTailY() );
    }

    public double getTailX() {
        return getTailPivot().getX();
    }

    public double getTailY() {
        return getTailPivot().getY();
    }

    public void setSpringConstant( double springConstant ) {
        if ( !_springConstantRange.contains( springConstant ) ) {
            new IllegalArgumentException( "springConstant out of range: " + springConstant );
        }
        if ( springConstant != _springConstant ) {
            _springConstant = springConstant;
            notifyObservers( PROPERTY_SPRING_CONSTANT );
        }
    }

    public double getSpringConstant() {
        return _springConstant;
    }

    public DoubleRange getSpringConstantRange() {
        return _springConstantRange;
    }

    public void setDampingConstant( double dampingConstant ) {
        if ( !_dampingConstantRange.contains( dampingConstant ) ) {
            new IllegalArgumentException( "dampingConstant out of range: " + dampingConstant );
        }
        if ( dampingConstant != _dampingConstant ) {
            _dampingConstant = dampingConstant;
            notifyObservers( PROPERTY_DAMPING_CONSTANT );
        }
    }

    public double getDampingConstant() {
        return _dampingConstant;
    }

    public DoubleRange getDampingConstantRange() {
        return _dampingConstantRange;
    }

    public void setKickConstant( double kickConstant ) {
        if ( !_kickConstantRange.contains( kickConstant ) ) {
            new IllegalArgumentException( "kickConstant out of range: " + kickConstant );
        }
        if ( kickConstant != _kickConstant ) {
            _kickConstant = kickConstant;
            notifyObservers( PROPERTY_KICK_CONSTANT );
        }
    }

    public double getKickConstant() {
        return _kickConstant;
    }

    public DoubleRange getKickConstantRange() {
        return _kickConstantRange;
    }

    public void setNumberOfEvolutionsPerClockTick( int numberOfEvolutionsPerClockTick ) {
        if ( !_numberOfEvolutionsPerClockTickRange.contains( numberOfEvolutionsPerClockTick ) ) {
            new IllegalArgumentException( "numberOfEvolutionsPerClockTick out of range: " + numberOfEvolutionsPerClockTick );
        }
        if ( numberOfEvolutionsPerClockTick != _numberOfEvolutionsPerClockTick ) {
            _numberOfEvolutionsPerClockTick = numberOfEvolutionsPerClockTick;
            notifyObservers( PROPERTY_NUMBER_OF_EVOLUTIONS_PER_CLOCK_TICK );
        }
    }

    public int getNumberOfEvolutionsPerClockTick() {
        return _numberOfEvolutionsPerClockTick;
    }

    public IntegerRange getNumberOfEvolutionsPerClockTickRange() {
        return _numberOfEvolutionsPerClockTickRange;
    }

    //----------------------------------------------------------------------------
    // Force model
    //----------------------------------------------------------------------------

    /**
     * Gets the force acting on the DNA head.
     * 
     * @return force (pN)
     */
    public Vector2D getForce() {
        double magnitude = getForceMagnitude();
        double direction = getForceDirection();
        return new Vector2D.Polar( magnitude, direction );
    }

    /*
     * Gets the direction of the force acting on the DNA head (radians).
     */
    private double getForceDirection() {
        final double xOffset = getTailPivot().getX() - getHeadPivot().getX();
        final double yOffset = getTailPivot().getY() - getHeadPivot().getY();
        return Math.atan2( yOffset, xOffset );
    }

    /*
     * Gets the magnitude of the force acting on the DNA head (pN).
     */
    private double getForceMagnitude() {
        final double extension = getExtension();
        final double kbT = 4.1 * _fluid.getTemperature() / 293; // kbT is 4.1 pN-nm at temperature=293K
        final double Lp = _persistenceLength;
        final double x = extension / _contourLength;
        return ( kbT / Lp ) * ( ( 1 / ( 4 * ( 1 - x ) * ( 1 - x ) ) ) - ( 0.24 ) + x );
    }

    /*
     * Gets the extension, the straight-line distance between the head and tail.
     */
    private double getExtension() {
        Point2D tailPosition = getTailPosition();
        Point2D headPosition = getHeadPosition();
        return tailPosition.distance( headPosition );
    }

    //----------------------------------------------------------------------------
    // Strand shape model
    //----------------------------------------------------------------------------
    
    public DNAPivot[] getPivots() {
        return _pivots;
    }
    
    public int getNumberOfPivots() {
        return _pivots.length;
    }
    
    public DNAPivot getHeadPivot() {
        return _pivots[ _pivots.length - 1 ];
    }
    
    public DNAPivot getTailPivot() {
        return _pivots[ 0 ];
    }
    
    /**
     * Initializes the strand.
     * The head is attached to the bead.
     * The tail is located some distance to the left of the head.
     */
    public void initializeStrand() {

        final double segmentLength = INITIAL_STRETCHINESS * ( _contourLength / _numberOfSegments );
        final int numberOfPivots = _numberOfSegments + 1;
        _pivots = new DNAPivot[numberOfPivots];

        // head is attached to the bead
        DNAPivot headPivot = new DNAPivot( _bead.getX(), _bead.getY() );
        _pivots[numberOfPivots - 1] = headPivot;

        // work backwards from head to tail
        for ( int i = numberOfPivots - 2; i >= 0; i-- ) {
            _pivots[i] = new DNAPivot( _pivots[i + 1].getX() - segmentLength, _pivots[i + 1].getY() );
        }

        notifyObservers( PROPERTY_SHAPE );
    }
    
    /*
     * Evolves the strand using a "Hollywood" spring model.
     * This model was provided by Mike Dubson.
     */
    private void evolveStrand( double clockStep ) {
        
        final double dt = clockStep / _numberOfEvolutionsPerClockTick;
        final double segmentLength = _contourLength / _numberOfSegments;
        
        for ( int i = 0; i < _numberOfEvolutionsPerClockTick; i++ ) {

            final int numberOfPivots = _pivots.length;
            DNAPivot currentPivot, previousPivot, nextPivot; // previous is closer to tail, next is closer to head
            
            // traverse all pivots from tail to head, skipping tail and head
            for ( int j = 1; j < numberOfPivots - 1; j++ ) {
                
                currentPivot = _pivots[ j ];
                previousPivot = _pivots[ j - 1 ];
                nextPivot = _pivots[ j + 1 ];
                
                // offset
                final double x = currentPivot.getX() + ( currentPivot.getXVelocity() * dt ) + ( 0.5 * currentPivot.getXAcceleration() * dt * dt );
                final double y = currentPivot.getY() + ( currentPivot.getYVelocity() * dt ) + ( 0.5 * currentPivot.getYAcceleration() * dt * dt );
                currentPivot.setPosition( x, y );
                
                // distance to previous and next pivots
                final double dxPrevious = currentPivot.getX() - previousPivot.getX();
                final double dyPrevious = currentPivot.getY() - previousPivot.getY();
                final double dxNext = currentPivot.getX() - nextPivot.getX();
                final double dyNext = currentPivot.getY() - nextPivot.getY();
                final double distanceToPrevious = Math.sqrt( ( dxPrevious * dxPrevious ) + ( dyPrevious * dyPrevious ) );
                final double distanceToNext = Math.sqrt( ( dxNext * dxNext ) + ( dyNext * dyNext ) );
                
                // common terms
                final double termPrevious = 1 - ( segmentLength / distanceToPrevious );
                final double termNext = 1 - ( segmentLength / distanceToNext );
                
                // acceleration
                final double xAcceleration = ( ( dxNext * termNext ) - ( dxPrevious * termPrevious ) ) - ( DRAG_COEFFICIENT * currentPivot.getXVelocity() );
                final double yAcceleration = ( ( dyNext * termNext ) - ( dyPrevious * termPrevious ) ) - ( DRAG_COEFFICIENT * currentPivot.getYVelocity() );
                currentPivot.setAcceleration( xAcceleration, yAcceleration );
                
                // velocity
                final double xVelocity = currentPivot.getXVelocity() + ( xAcceleration * dt ) + ( _kickConstant * ( _kickRandom.nextDouble() - 0.5 ) );
                final double yVelocity = currentPivot.getYVelocity() + ( yAcceleration * dt ) + ( _kickConstant * ( _kickRandom.nextDouble() - 0.5 ) );
                currentPivot.setVelocity( xVelocity, yVelocity );
            }
        }
        
        notifyObservers( PROPERTY_SHAPE );
    }

    //----------------------------------------------------------------------------
    // Observer implementation
    //----------------------------------------------------------------------------

    /*
     * Updates the model when something it's observing changes.
     * 
     * @param o
     * @param arg
     */
    public void update( Observable o, Object arg ) {
        if ( o == _bead ) {
            if ( arg == Bead.PROPERTY_POSITION ) {
                DNAPivot headPivot = getHeadPivot();
                headPivot.setPosition( _bead.getX(), _bead.getY() );
                notifyObservers( PROPERTY_FORCE );
            }
        }
        else if ( o == _fluid ) {
            if ( arg == Fluid.PROPERTY_TEMPERATURE ) {
                notifyObservers( PROPERTY_FORCE );
            }
        }
    }

    //----------------------------------------------------------------------------
    // ModelElement implementation
    //----------------------------------------------------------------------------

    public void stepInTime( double dt ) {
        evolveStrand( dt );
    }
}
