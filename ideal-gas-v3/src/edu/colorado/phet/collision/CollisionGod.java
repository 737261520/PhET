/**
 * Class: CollisionGod
 * Package: edu.colorado.phet.idealgas.physics.collision
 * Author: Another Guy
 * Date: Jan 20, 2004
 */
package edu.colorado.phet.collision;

import edu.colorado.phet.idealgas.IdealGasConfig;
import edu.colorado.phet.idealgas.model.GasMolecule;
import edu.colorado.phet.idealgas.model.IdealGasModel;
import edu.colorado.phet.mechanics.Body;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * This class takes care of detecting and computing all the collisions in
 * the system. To avoid combinatorial explosions in collision detection,
 * the area in which collisions are to be handled is divided up into regions,
 * and particle-particle collisions are only searched for within each
 * region and those adjacent to it.
 */
public class CollisionGod {
    private int numRegionsX;
    private int numRegionsY;
    private Region[][] regions;
    private HashMap elementToRegionMap = new HashMap();
    // List to track bodies that are to be removed from the system at the
    // end of the apply() method, to avoid concurrentModificationExceptions
    private ArrayList removalList = new ArrayList();
    private double regionWidth;
    private double regionHeight;
    public static boolean overlappingRegions = true;//false;
    private IdealGasModel model;
    private double dt;
    private Rectangle2D.Double bounds;
    private double regionOverlap;
    private List collisionExperts;


    public CollisionGod( IdealGasModel model, double dt,
                         Rectangle2D.Double bounds, int numRegionsX, int numRegionsY ) {
        this.model = model;
        this.dt = dt;
        this.bounds = bounds;
        this.numRegionsX = numRegionsX;
        this.numRegionsY = numRegionsY;
        regions = new Region[numRegionsX][numRegionsY];
        regionWidth = bounds.getWidth() / numRegionsX;
        regionHeight = bounds.getHeight() / numRegionsY;
        regionOverlap = 2 * GasMolecule.s_defaultRadius;

        for( int i = 0; i < numRegionsX; i++ ) {
            for( int j = 0; j < numRegionsY; j++ ) {
                if( overlappingRegions ) {
                    regions[i][j] = new Region( bounds.getX() + i * regionWidth, // - regionOverlap,
                                                bounds.getX() + ( ( i + 1 ) * regionWidth ) + regionOverlap,
                                                bounds.getY() + j * regionHeight, // - regionOverlap,
                                                bounds.getY() + ( ( j + 1 ) * regionHeight ) + regionOverlap );
                }
                else {
                    regions[i][j] = new Region( bounds.getX() + (double)i * regionWidth, ( bounds.getX() + (double)( i + 1 ) * regionWidth ) - -Double.MIN_VALUE,
                                                bounds.getY() + (double)j * regionHeight, ( bounds.getY() + (double)( j + 1 ) * regionHeight ) - Double.MIN_VALUE );
                }
            }
        }
    }

    public void doYourThing( double dt, List collisionExperts ) {
        this.collisionExperts = collisionExperts;
        List bodies = model.getBodies();
        adjustRegionMembership( bodies );

        // Do the miscellaneous collisions after the gas to gas collisions. This
        // seems to help keep things more graphically accurate
        doGasToGasCollisions();
        doMiscCollisions( bodies );
        for( int i = 0; i < regions.length; i++ ) {
            Region[] region = regions[i];
            for( int j = 0; j < region.length; j++ ) {
                Region region1 = region[j];
                region1.clear();
            }
        }
    }

    /**
     * Makes sure all gas molecules are in the correct regions.
     *
     * @param bodies
     */
    private void adjustRegionMembership( List bodies ) {

        // Put all the gas molecules in the model in the right regions
        for( int i = 0; i < bodies.size(); i++ ) {
            Body body = (Body)bodies.get( i );
            if( body instanceof GasMolecule ) {
                if( overlappingRegions ) {
                    assignRegions( body );
                }
                else {
                    if( elementToRegionMap.containsKey( body ) ) {
                        this.placeBody( body );
                    }
                    else {
                        this.addBody( body );
                    }
                }
            }
        }

        // Remove any gas molecules from our internal structures that
        // are no longer in the physical system
        removalList.clear();
        Set placedBodies = elementToRegionMap.keySet();
        for( Iterator iterator = placedBodies.iterator(); iterator.hasNext(); ) {
            Object o = iterator.next();
            if( o instanceof GasMolecule ) {
                if( !bodies.contains( o ) ) {
                    removalList.add( o );
                }
            }
        }
        while( !removalList.isEmpty() ) {
            Body body = (Body)removalList.remove( 0 );
            removeBody( body );
        }
    }

    /**
     * Detects and performs collisions in which at least one of
     * the bodies is not a gas molecule
     *
     * @param bodies
     */
    private void doMiscCollisions( List bodies ) {
        ArrayList nonGasBodies = new ArrayList();
        // Find all the bodies that aren't gas molecules
        for( int i = 0; i < bodies.size(); i++ ) {
            Object o = bodies.get( i );
            if( !( o instanceof GasMolecule ) ) {
                nonGasBodies.add( o );
            }
        }
        // Find all collisions between non-gas molecules and other
        // bodies
        for( int i = 0; i < nonGasBodies.size(); i++ ) {
            CollidableBody body1 = (CollidableBody)nonGasBodies.get( i );
            for( int j = 0; j < bodies.size(); j++ ) {
                CollidableBody body2 = (CollidableBody)bodies.get( j );
                if( body1 != body2 ) {
                    detectAndDoCollision( body1, body2, dt );
                }
            }
        }
    }

    private void doGasToGasCollisions() {

        // Do particle-particle collisions. Each region collides with
        // itself and the regions to the right and below.
        for( int i = 0; i < numRegionsX; i++ ) {
            for( int j = 0; j < numRegionsY; j++ ) {
                doRegionToRegionCollision( regions[i][j], regions[i][j] );
                if( !overlappingRegions ) {
                    if( i < numRegionsX - 1 ) {
                        doRegionToRegionCollision( regions[i][j], regions[i + 1][j] );
                    }
                    if( j < numRegionsY - 1 ) {
                        doRegionToRegionCollision( regions[i][j], regions[i][j + 1] );
                    }
                    if( i < numRegionsX - 1 && j < numRegionsY - 1 ) {
                        doRegionToRegionCollision( regions[i][j], regions[i + 1][j + 1] );
                    }
                }
            }
        }
    }

    private void doRegionToRegionCollision( Region region1, Region region2 ) {
        for( int i = 0; i < region1.size(); i++ ) {
            CollidableBody body1 = (CollidableBody)region1.get( i );
            int jStart = ( region1 == region2 ) ? i + 1 : 0;
            for( int j = jStart; j < region2.size(); j++ ) {
                CollidableBody body2 = (CollidableBody)region2.get( j );
                if( body1 != body2 ) {
                    detectAndDoCollision( body1, body2, dt );
                }
            }
        }
    }

    private void detectAndDoCollision( CollidableBody body1, CollidableBody body2, double dt ) {
        for( int i = 0; i < collisionExperts.size(); i++ ) {
            CollisionExpert collisionExpert = (CollisionExpert)collisionExperts.get( i );
            collisionExpert.detectAndDoCollision( body1, body2 );
        }

    }

    private void addBody( Body body ) {
        if( overlappingRegions ) {
            assignRegions( body );
        }
        else {
            Region region = findRegionFor( body );
            elementToRegionMap.put( body, region );
            if( region == null ) {
                System.out.println( "halt" );
            }
            region.add( body );
        }
    }

    private void removeBody( Body body ) {
        if( overlappingRegions ) {
            int iLimit = numRegionsX;
            int jLimit = numRegionsY;
            for( int i = 0; i < iLimit; i++ ) {
                for( int j = 0; j < jLimit; j++ ) {
                    if( regions[i][j].contains( body ) ) {
                        // If we have found a region in which the particle belongs, we only have to
                        // test for membership in the next region, and no farther
                        iLimit = Math.min( ( i + 2 ), numRegionsX );
                        jLimit = Math.min( ( j + 2 ), numRegionsY );
                        regions[i][j].remove( body );
                    }
                }
            }
        }
        else {
            ( (Region)elementToRegionMap.get( body ) ).remove( body );
            elementToRegionMap.remove( body );
        }
    }

    private void assignRegions( Body body ) {
        double x = body.getPosition().getX();
        double y = body.getPosition().getY();
        try {
            int i = (int)( ( x - bounds.x ) / regionWidth );
            int iPrime = (int)( ( x - regionOverlap - bounds.x ) / ( regionWidth ) );
            int j = (int)( ( y - bounds.y ) / regionHeight );
            int jPrime = (int)( ( y - regionOverlap - bounds.y ) / ( regionHeight ) );
            regions[i][j].add( body );
            if( i != iPrime ) {
                regions[iPrime][j].add( body );
            }
            if( j != jPrime ) {
                regions[i][jPrime].add( body );
            }
            if( i != iPrime && j != jPrime ) {
                regions[iPrime][jPrime].add( body );
            }
        }
        catch( ArrayIndexOutOfBoundsException aiobe ) {
            System.out.println( "ArrayIndexOutOfBoundsException in CollisionGod.assignRegions()" );
        }
    }

    private Region findRegionFor( Body body ) {
        Region region = null;
        if( IdealGasConfig.regionTest ) {
            int i = (int)( (double)body.getPosition().getX() / regionWidth );
            int j = (int)( (double)body.getPosition().getY() / regionHeight );
            region = regions[i][j];
        }
        else {
            for( int i = 0; region == null && i < numRegionsX; i++ ) {
                for( int j = 0; region == null && j < numRegionsY; j++ ) {
                    if( regions[i][j].belongsIn( body ) ) {
                        region = regions[i][j];
                    }
                }
            }
        }
        return region;
    }

    private void placeBody( Body body ) {
        Region currRegion = (Region)elementToRegionMap.get( body );
        if( currRegion == null ) {
            addBody( body );
        }
        else if( !currRegion.belongsIn( body ) ) {
            currRegion.remove( body );
            addBody( body );
        }
    }

    /**
     * A region within the physical system
     */
    // Try making it a linked list instead of a HashSet. That way, we can avoid using
    // iterators when going through them
    public class Region extends LinkedList {
        double xMin;
        double xMax;
        double yMin;
        double yMax;

        Region( double xMin, double xMax, double yMin, double yMax ) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
        }

        boolean belongsIn( Body body ) {
            boolean result = body.getPosition().getX() >= xMin
                             && body.getPosition().getX() <= xMax
                             && body.getPosition().getY() >= yMin
                             && body.getPosition().getY() <= yMax;
            return result;
        }
    }
}
