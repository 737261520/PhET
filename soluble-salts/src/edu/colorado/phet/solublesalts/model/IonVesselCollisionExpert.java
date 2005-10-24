/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.solublesalts.model;

import edu.colorado.phet.collision.Collidable;
import edu.colorado.phet.collision.CollisionExpert;
import edu.colorado.phet.collision.ContactDetector;
import edu.colorado.phet.collision.SphereBoxExpert;

import java.util.List;

/**
 * IonVesselCollisionExpert
 * <p/>
 * Has the same behavior as a SphereBoxExpert, with the additional behavior that
 * on some collisions, the ion may stick to the vessel, based on an "affinity" between
 * the two. The affinity is determined by a strategy plugged into the collision expert.
 * <p/>
 * Will not let two ions of the same type bind within one diameter of each other
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class IonVesselCollisionExpert implements CollisionExpert, ContactDetector {

    private SphereBoxExpert sphereBoxExpert = new SphereBoxExpert();
    private SolubleSaltsModel model;

    public IonVesselCollisionExpert( SolubleSaltsModel model ) {
        this.model = model;
    }

    public boolean detectAndDoCollision( Collidable bodyA, Collidable bodyB ) {
        Ion ion = null;
        Vessel vessel = null;
        if( applies( bodyA, bodyB ) ) {
            ion = (Ion)( bodyA instanceof Ion ? bodyA : bodyB );
            vessel = (Vessel)( bodyA instanceof Vessel ? bodyA : bodyB );
            if( !ion.isBound() && areInContact( ion, vessel.getWater() ) ) {

                // Make sure the ion isn't too close to another ion of the
                // same polarity
                boolean canBind = true;
                double minDist = ion.getRadius() * 4;
                List otherIons = model.getIonsOfType( ion.getClass() );
                for( int i = 0; i < otherIons.size(); i++ ) {
                    Ion testIon = (Ion)otherIons.get( i );
                    if( testIon.isBound() && testIon.getBindingLattice().getSeed() == testIon
                        && testIon.getPosition().distance( ion.getPosition() ) < minDist ) {

                        canBind = false;
                    }
                }


                if( canBind && vessel.getIonStickAffinity().stick( ion, vessel ) ) {
                    vessel.bind( ion );
                }
                // Perform the collision, even if we bind, so when the ion is released
                // it moves properly
                sphereBoxExpert.detectAndDoCollision( ion, vessel.getWater() );
            }
        }
        return false;
    }

    public boolean areInContact( Collidable bodyA, Collidable bodyB ) {
        return sphereBoxExpert.areInContact( bodyA, bodyB );
    }

    public boolean applies( Collidable bodyA, Collidable bodyB ) {
        return ( bodyA instanceof Ion && bodyB instanceof Vessel
                 || bodyB instanceof Ion && bodyA instanceof Vessel );
    }
}
