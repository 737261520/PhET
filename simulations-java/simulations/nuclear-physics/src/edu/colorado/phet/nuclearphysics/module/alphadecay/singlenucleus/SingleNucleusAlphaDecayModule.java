/* Copyright 2008, University of Colorado */

package edu.colorado.phet.nuclearphysics.module.alphadecay.singlenucleus;

import java.awt.Frame;

import edu.colorado.phet.common.piccolophet.PiccoloModule;
import edu.colorado.phet.nuclearphysics.NuclearPhysicsStrings;
import edu.colorado.phet.nuclearphysics.defaults.SingleNucleusAlphaDecayDefaults;
import edu.colorado.phet.nuclearphysics.model.NuclearPhysicsClock;
import edu.colorado.phet.nuclearphysics.module.alphadecay.AlphaDecayControlPanel;

/**
 * This class is where the model and view classes are created and connected
 * for the portion of the sim that demonstrates alpha decay of a single atomic
 * nucleus. 
 *
 * @author John Blanco
 */
public class SingleNucleusAlphaDecayModule extends PiccoloModule {
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private SingleNucleusAlphaDecayModel _model;
    private SingleNucleusAlphaDecayCanvas _canvas;
    private AlphaDecayControlPanel _controlPanel;

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    public SingleNucleusAlphaDecayModule( Frame parentFrame ) {
        super( NuclearPhysicsStrings.TITLE_SINGLE_ATOM_ALPHA_DECAY_MODULE,
               new NuclearPhysicsClock( SingleNucleusAlphaDecayDefaults.CLOCK_FRAME_RATE, SingleNucleusAlphaDecayDefaults.CLOCK_DT ));
 
        // Model
        NuclearPhysicsClock clock = (NuclearPhysicsClock) getClock();
        _model = new SingleNucleusAlphaDecayModel(clock);

        // Canvas
        _canvas = new SingleNucleusAlphaDecayCanvas( _model );
        setSimulationPanel( _canvas );

        // Control Panel
        _controlPanel = new AlphaDecayControlPanel( this, parentFrame, _model );
        setControlPanel( _controlPanel );
        
        // Help
        if ( hasHelp() ) {
            //XXX add help items
        }

        // Set initial state
        reset();
    }

    //----------------------------------------------------------------------------
    // Module overrides
    //----------------------------------------------------------------------------

    /**
     * Resets the module.
     */
    public void reset() {

        // Reset the clock, which ultimately resets the model too.
        _model.getClock().resetSimulationTime();
        setClockRunningWhenActive( SingleNucleusAlphaDecayDefaults.CLOCK_RUNNING );
    }
}
