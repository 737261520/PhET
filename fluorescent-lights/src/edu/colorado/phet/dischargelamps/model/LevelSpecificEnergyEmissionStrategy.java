/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.dischargelamps.model;

import edu.colorado.phet.common.math.ProbabilisticChooser;
import edu.colorado.phet.quantum.model.Atom;
import edu.colorado.phet.quantum.model.AtomicState;
import edu.colorado.phet.quantum.model.EnergyEmissionStrategy;

import java.util.*;

/**
 * LevelSpecificEnergyEmissionStrategy
 * <p/>
 * An energy emission strategy in which the probability of the atom going from
 * one state to another is different for the transition from each level to
 * every lower level.
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class LevelSpecificEnergyEmissionStrategy implements EnergyEmissionStrategy {
    private static final Random random = new Random();

    private Map originStateToTargetStates = new HashMap();
    private DischargeLampElementProperties.TransitionEntry[] teA;


    public LevelSpecificEnergyEmissionStrategy( DischargeLampElementProperties.TransitionEntry[] teA ) {
        this.teA = teA;
    }

    public AtomicState emitEnergy( Atom atom ) {
        AtomicState newState = (AtomicState)getTargetState( atom.getCurrState() );
        return newState;
    }

    public Object getTargetState( Object originState ) {
        return getTargetState( originState, random.nextDouble() );
    }

    public Object getTargetState( Object originState, double p ) {
        ProbabilisticChooser targetMap = (ProbabilisticChooser)originStateToTargetStates.get( originState );
        Object targetState = targetMap.get( p );
        return targetState;
    }

    /**
     * Sets the states for the strategy. Builds the map that holds the ProbabilityMaps for each
     * state.
     *
     * @param states
     */
    public void setStates( AtomicState[] states ) {
        for( int i = 0; i < states.length; i++ ) {
            AtomicState state = states[i];
            List mapEntries = new ArrayList();
            for( int j = 0; j < teA.length; j++ ) {
                DischargeLampElementProperties.TransitionEntry te = teA[j];
                if( i == te.getSourceStateIdx() ) {
                    ProbabilisticChooser.Entry pmEntry = new ProbabilisticChooser.Entry( states[teA[j].getTargetStateIdx()],
                                                                             te.getTxStrength() );
                    mapEntries.add( pmEntry );
                };
            }
            originStateToTargetStates.put( state, new ProbabilisticChooser( (ProbabilisticChooser.Entry[])mapEntries.toArray(new ProbabilisticChooser.Entry[mapEntries.size()]) ) );
        }
    }
}
