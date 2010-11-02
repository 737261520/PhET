/* Copyright 2007-2008, University of Colorado */

package edu.colorado.phet.gravityandorbits.model;

import java.awt.*;
import java.util.ArrayList;

import edu.colorado.phet.common.phetcommon.math.ImmutableVector2D;
import edu.colorado.phet.common.phetcommon.model.clock.ClockAdapter;
import edu.colorado.phet.common.phetcommon.model.clock.ClockEvent;


/**
 * Model template.
 */
public class GravityAndOrbitsModel {
    private static final double SUN_RADIUS = 6.955E8;
    private static final double EARTH_RADIUS = 6.371E6;
    private static final double G = 6.67428E-11;

    private final GravityAndOrbitsClock clock;
    private final Body sun = new Body( "Sun", 0, 0, SUN_RADIUS * 2, 0, 0, 1.989E30, Color.yellow, Color.white );
    private final Body planet = new Body( "Planet", 149668992000.0, 0, EARTH_RADIUS * 2, 0, -29.78E3, 5.9742E24, Color.blue, Color.white );//semi-major axis, see http://en.wikipedia.org/wiki/Earth, http://en.wikipedia.org/wiki/Sun

    public GravityAndOrbitsModel( GravityAndOrbitsClock clock ) {
        super();
        this.clock = clock;
        clock.addClockListener( new ClockAdapter() {
            @Override
            public void simulationTimeChanged( ClockEvent clockEvent ) {
                super.simulationTimeChanged( clockEvent );
                final VelocityVerlet.BodyState sunState = sun.toBodyState();
                final VelocityVerlet.BodyState planetState = planet.toBodyState();
                final ArrayList<VelocityVerlet.BodyState> verletState = getVerletState();
                ArrayList<VelocityVerlet.BodyState> state = new VelocityVerlet().getNextState( verletState, clockEvent.getSimulationTimeChange(), new VelocityVerlet.PotentialField() {
                    public ImmutableVector2D getGradient( VelocityVerlet.BodyState body, ImmutableVector2D position ) {
                        if ( body == sunState ) {//sun
                            return getForce( planetState, sunState );
                        }
                        else {
                            return getForce( sunState, planetState );
                        }
                    }
                } );
                sun.setBodyState( state.get( 0 ) );
                planet.setBodyState( state.get( 1 ) );
            }
        } );
    }

    private ImmutableVector2D getForce( VelocityVerlet.BodyState source, VelocityVerlet.BodyState target ) {
        return target.getUnitDirectionVector( source ).getScaledInstance( G * source.mass * target.mass / source.distanceSquared( target ) );
    }

    private ArrayList<VelocityVerlet.BodyState> getVerletState() {
        ArrayList<VelocityVerlet.BodyState> state = new ArrayList<VelocityVerlet.BodyState>();
        state.add( sun.toBodyState() );
        state.add( planet.toBodyState() );
        return state;
    }

    public GravityAndOrbitsClock getClock() {
        return clock;
    }

    public Body getSun() {
        return sun;
    }

    public Body getPlanet() {
        return planet;
    }
}
