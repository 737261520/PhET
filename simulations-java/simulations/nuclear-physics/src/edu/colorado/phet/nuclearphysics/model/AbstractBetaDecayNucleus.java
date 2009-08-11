/* Copyright 2009, University of Colorado */

package edu.colorado.phet.nuclearphysics.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import edu.colorado.phet.common.phetcommon.model.clock.ClockEvent;
import edu.colorado.phet.nuclearphysics.common.NuclearPhysicsClock;
import edu.colorado.phet.nuclearphysics.common.model.Antineutrino;
import edu.colorado.phet.nuclearphysics.common.model.AtomicNucleus;
import edu.colorado.phet.nuclearphysics.common.model.Electron;

/**
 * This class contains much of the behavior that is common to all nuclei that
 * exhibit beta decay.
 * 
 * @author John Blanco
 */
public abstract class AbstractBetaDecayNucleus extends AtomicNucleus {

	private static final Random RAND = new Random();
	private static final double EMISSION_SPEED = 10;  // Femtometers per clock tick.  Weird, I know.
	
	public AbstractBetaDecayNucleus(NuclearPhysicsClock clock, Point2D position,
			int numProtons, int numNeutrons, double decayTimeScalingFactor) {
		super(clock, position, numProtons, numNeutrons, decayTimeScalingFactor);
	}
	
	/**
	 * Take the actions that simulate alpha decay.
	 */
	protected void decay( ClockEvent clockEvent ){
		
		// Update the nucleus configuration.
		_numNeutrons -= 1;
		_numProtons += 1;
		
		// Create the emitted particles, which are an electron and an
		// antineutrino.
		double angle = RAND.nextDouble() * Math.PI * 2;
		double xVel = Math.cos(angle) * EMISSION_SPEED; 
		double yVel = Math.sin(angle) * EMISSION_SPEED; 
        ArrayList byProducts = new ArrayList();
        Electron electron = new Electron(getPositionReference().getX(), getPositionReference().getY());
        electron.setVelocity(xVel, yVel);
        byProducts.add(electron);
        Antineutrino antineutrino = new Antineutrino(getPositionReference().getX(), getPositionReference().getY());
        antineutrino.setVelocity(xVel, yVel);
        byProducts.add(antineutrino);

        // Set the final value of the time that this nucleus existed prior to
        // decaying.
        _activatedLifetime += clockEvent.getSimulationTimeChange();
        
        // Send out the decay event to all listeners.
        notifyNucleusChangeEvent(byProducts);
        
        // Set the decay time to 0 to indicate that decay has occurred and
        // should not occur again.
        _decayTime = 0;
	}
}
