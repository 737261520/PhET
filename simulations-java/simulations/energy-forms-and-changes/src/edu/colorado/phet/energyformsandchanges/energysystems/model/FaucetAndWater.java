// Copyright 2002-2012, University of Colorado
package edu.colorado.phet.energyformsandchanges.energysystems.model;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.colorado.phet.common.phetcommon.math.vector.Vector2D;
import edu.colorado.phet.common.phetcommon.model.property.BooleanProperty;
import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.simsharing.messages.IUserComponent;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.phetcommon.view.util.DoubleGeneralPath;
import edu.colorado.phet.energyformsandchanges.EnergyFormsAndChangesResources;
import edu.colorado.phet.energyformsandchanges.EnergyFormsAndChangesSimSharing;
import edu.colorado.phet.energyformsandchanges.common.model.EnergyChunk;
import edu.colorado.phet.energyformsandchanges.common.model.EnergyType;

/**
 * Class that represents a faucet that can be turned on to provide mechanical
 * energy to other energy system elements.
 *
 * @author John Blanco
 */
public class FaucetAndWater extends EnergySource {

    //-------------------------------------------------------------------------
    // Class Data
    //-------------------------------------------------------------------------

    public static final Vector2D OFFSET_FROM_CENTER_TO_WATER_ORIGIN = new Vector2D( 0.065, 0.08 );
    private static final double MAX_ENERGY_PRODUCTION_RATE = 200; // In joules/second.
    private static final double WATER_FALLING_VELOCITY = 0.07; // In meters/second.
    private static final double FLOW_PER_CHUNK = 0.4;  // Empirically determined to get desired energy chunk emission rate.
    private static final double MAX_WATER_WIDTH = 0.015; // In meters.
    private static final double MAX_DISTANCE_FROM_FAUCET_TO_BOTTOM_OF_WATER = 0.5; // In meters.

    //-------------------------------------------------------------------------
    // Instance Data
    //-------------------------------------------------------------------------

    public final Property<Double> flowProportion = new Property<Double>( 0.0 );
    public final BooleanProperty enabled = new BooleanProperty( true );

    // Shape of the water coming from the faucet.  The shape is specified
    // relative to the water origin.
    public final Property<Shape> waterShape = new Property<Shape>( null );

    // Points, or actually distance-width pairs, that define the shape of the
    // water.
    private final List<DistanceWidthPair> waterShapeDefiningPoints = new ArrayList<DistanceWidthPair>();

    private double flowSinceLastChunk = 0;
    private final BooleanProperty energyChunksVisible;

    //-------------------------------------------------------------------------
    // Constructor(s)
    //-------------------------------------------------------------------------

    protected FaucetAndWater( BooleanProperty energyChunksVisible ) {
        super( EnergyFormsAndChangesResources.Images.FAUCET_ICON );
        this.energyChunksVisible = energyChunksVisible;

        waterShape.set( createWaterShapeFromPoints( waterShapeDefiningPoints ) );

        // TODO: Probably won't need this when updates occurring in stepInTime.
        flowProportion.addObserver( new SimpleObserver() {
            public void update() {
//                updateWaterShape();
            }
        } );
    }

    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------

    @Override public Energy stepInTime( double dt ) {

        if ( isActive() ) {

            // Update the shape of the water.  This is done here - in the time
            // step method - so that the water appears to fall at the start
            // and end of the flow.
            updateWaterShape( dt );

            // Check if time to emit an energy chunk and, if so, do it.
            flowSinceLastChunk += flowProportion.get() * dt;
            if ( flowSinceLastChunk > FLOW_PER_CHUNK ) {
                energyChunkList.add( new EnergyChunk( EnergyType.MECHANICAL,
                                                      getPosition().plus( OFFSET_FROM_CENTER_TO_WATER_ORIGIN ),
                                                      new Vector2D( 0, -WATER_FALLING_VELOCITY ),
                                                      energyChunksVisible ) );
                flowSinceLastChunk = 0;
            }

            // Update energy chunk positions.
            for ( EnergyChunk energyChunk : new ArrayList<EnergyChunk>( energyChunkList ) ) {

                // Make the chunk fall.
                energyChunk.translateBasedOnVelocity( dt );

                // Remove it if it is out of visible range.
                if ( getPosition().plus( OFFSET_FROM_CENTER_TO_WATER_ORIGIN ).distance( energyChunk.position.get() ) > MAX_DISTANCE_FROM_FAUCET_TO_BOTTOM_OF_WATER ) {
                    energyChunkList.remove( energyChunk );
                }
            }
        }

        // Generate the appropriate amount of energy.
        return new Energy( EnergyType.MECHANICAL, MAX_ENERGY_PRODUCTION_RATE * flowProportion.get() * dt, -Math.PI / 2 );
    }

    private void updateWaterShape( double dt ) {

        // Make the existing shape-defining points fall.
        for ( DistanceWidthPair waterShapeDefiningPoint : waterShapeDefiningPoints ) {
            waterShapeDefiningPoint.setDistance( Math.min( waterShapeDefiningPoint.getDistance() + WATER_FALLING_VELOCITY * dt,
                                                           MAX_DISTANCE_FROM_FAUCET_TO_BOTTOM_OF_WATER ) );
        }

        double waterWidth = flowProportion.get() * MAX_WATER_WIDTH;
        double previousWaterWidth = waterShapeDefiningPoints.size() > 0 ? waterShapeDefiningPoints.get( waterShapeDefiningPoints.size() - 1 ).getWidth() : 0;

        // Update the points that define the top of the water shape.
        if ( waterWidth > 0 && previousWaterWidth == 0 ) {
            // This is the start of a new water blob.  Each water blob must
            // have a zero-width point at its bottom edge.
            waterShapeDefiningPoints.add( new DistanceWidthPair( dt * WATER_FALLING_VELOCITY, 0 ) );
            // Now add point for actual water width.
            waterShapeDefiningPoints.add( new DistanceWidthPair( 0, waterWidth ) );
        }
        else if ( waterWidth == 0 && previousWaterWidth > 0 ) {
            // This is the end of a water blob.
            waterShapeDefiningPoints.add( new DistanceWidthPair( 0, waterWidth ) );
        }
        else if ( waterWidth > 0 && previousWaterWidth > 0 ) {
            // This is somewhere in the middle of a blob, so just add a point.
            waterShapeDefiningPoints.add( new DistanceWidthPair( 0, waterWidth ) );
        }

        // Update points that are reaching the bottom of the "fall distance".
        List<DistanceWidthPair> copyOfShapeDefiningPoints = new ArrayList<DistanceWidthPair>( waterShapeDefiningPoints );
        for ( int i = 0; i < copyOfShapeDefiningPoints.size() - 1; i++ ) {
            if ( copyOfShapeDefiningPoints.get( i ).distance >= MAX_DISTANCE_FROM_FAUCET_TO_BOTTOM_OF_WATER &&
                 copyOfShapeDefiningPoints.get( i + 1 ).distance >= MAX_DISTANCE_FROM_FAUCET_TO_BOTTOM_OF_WATER ) {
                // This point is no longer needed.
                waterShapeDefiningPoints.remove( copyOfShapeDefiningPoints.get( i ) );
                // Set the point at the bottom to have zero width.
                copyOfShapeDefiningPoints.get( i + 1 ).setWidth( 0 );
            }
        }

        // TODO: Optimize to update only when changes occur.
        waterShape.set( createWaterShapeFromPoints( waterShapeDefiningPoints ) );
    }

    @Override public void deactivate() {
        super.deactivate();
        enabled.set( false );
    }

    @Override public void activate() {
        super.activate();
        enabled.set( true );
    }

    @Override public IUserComponent getUserComponent() {
        return EnergyFormsAndChangesSimSharing.UserComponents.selectFaucetButton;
    }

    private static Shape createWaterShapeFromPoints( List<DistanceWidthPair> distanceWidthPairs ) {

        if ( distanceWidthPairs.size() < 2 ) {
            // Not enough pairs to create a shape, so return a shape that is
            // basically invisible.
            return new Rectangle2D.Double( 0, 0, 1E-7, 1E-7 );
        }

        DoubleGeneralPath path = new DoubleGeneralPath();

        // Identify individual blobs and add them to the path.
        int blobCount = 0;
        List<DistanceWidthPair> blobDefiningPairs = new ArrayList<DistanceWidthPair>();
        for ( int i = 0; i < distanceWidthPairs.size(); i++ ) {
            System.out.println( "blob i = " + i );
            blobDefiningPairs.add( distanceWidthPairs.get( i ) );
            if ( ( blobDefiningPairs.size() > 1 && distanceWidthPairs.get( i ).getWidth() == 0 ) || i == distanceWidthPairs.size() - 1 ) {
                // End of blob detected, so add this to the path.
                System.out.println( "blobCount = " + blobCount );
                blobCount++;
                addBlobToPath( blobDefiningPairs, path );
                blobDefiningPairs.clear();
            }
        }

        return path.getGeneralPath();
    }

    private static void addBlobToPath( List<DistanceWidthPair> distanceWidthPairs, DoubleGeneralPath path ) {

        // Check that the blob is properly formed.
        if ( distanceWidthPairs.get( 0 ).getWidth() != 0 ) {
            System.out.println( "Error case bubka" );
        }
        assert distanceWidthPairs.size() >= 2;
        assert distanceWidthPairs.get( 0 ).width == 0;

        List<DistanceWidthPair> copy = new ArrayList<DistanceWidthPair>( distanceWidthPairs );
        Vector2D startPoint = new Vector2D( -copy.get( 0 ).getWidth() / 2, -copy.get( 0 ).distance );
        copy.remove( 0 );
        List<DistanceWidthPair> reverseCopy = new ArrayList<DistanceWidthPair>( copy ) {{
            Collections.reverse( this );
        }};

        path.moveTo( startPoint );

        // Add one side of the path.
        for ( DistanceWidthPair distanceWidthPair : copy ) {
            path.lineTo( -distanceWidthPair.getWidth() / 2, -distanceWidthPair.getDistance() );
        }

        // Add the other side of the path.
        for ( DistanceWidthPair distanceWidthPair : reverseCopy ) {
            path.lineTo( distanceWidthPair.getWidth() / 2, -distanceWidthPair.getDistance() );
        }

        // Effective close the blob.
        path.lineTo( startPoint );
    }

    private void updateWaterShape() {
        if ( flowProportion.get() == 0 ) {
            waterShape.set( new Rectangle2D.Double( 0, 0, 1E-7, 1E-7 ) );
        }
        else {
            waterShapeDefiningPoints.clear();
            double waterWidth = flowProportion.get() * MAX_WATER_WIDTH;
            waterShapeDefiningPoints.add( new DistanceWidthPair( MAX_DISTANCE_FROM_FAUCET_TO_BOTTOM_OF_WATER, waterWidth ) );
            waterShapeDefiningPoints.add( new DistanceWidthPair( 0, waterWidth ) );
            waterShape.set( createWaterShapeFromPoints( waterShapeDefiningPoints ) );
        }
    }

    private static class DistanceWidthPair {
        private double distance;
        private double width;

        private DistanceWidthPair( double distance, double width ) {
            this.distance = distance;
            this.width = width;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance( double distance ) {
            this.distance = distance;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth( double width ) {
            this.width = width;
        }
    }
}
