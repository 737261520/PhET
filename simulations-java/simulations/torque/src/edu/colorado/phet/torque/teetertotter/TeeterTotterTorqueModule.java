// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.torque.teetertotter;

import edu.colorado.phet.common.phetcommon.application.Module;
import edu.colorado.phet.torque.teetertotter.model.TeeterTotterTorqueModel;
import edu.colorado.phet.torque.teetertotter.view.TeeterTotterTorqueCanvas;

/**
 * Main module for this tab.
 *
 * @author John Blanco
 */
public class TeeterTotterTorqueModule extends Module {

    TeeterTotterTorqueModel model;

    public TeeterTotterTorqueModule() {
        this( new TeeterTotterTorqueModel() );
        setClockControlPanel( null );
    }

    private TeeterTotterTorqueModule( TeeterTotterTorqueModel model ) {
        // TODO: i18n
        super( "Teeter Totter Torque", model.getClock() );
        this.model = model;
        setSimulationPanel( new TeeterTotterTorqueCanvas( model ) );
        reset();
    }

    @Override public void reset() {
        model.reset();
    }
}
