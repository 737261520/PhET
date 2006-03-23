/* Copyright 2004, Sam Reid */
package edu.colorado.phet.waveinterference.tests;

import edu.colorado.phet.common.application.PhetApplication;
import edu.colorado.phet.common.view.ModelSlider;
import edu.colorado.phet.waveinterference.view.IndexColorMap;
import edu.colorado.phet.waveinterference.view.SimpleLatticeGraphic;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * User: Sam Reid
 * Date: Mar 21, 2006
 * Time: 11:57:27 PM
 * Copyright (c) Mar 21, 2006 by Sam Reid
 */

public class TestWave extends PhetApplication {

    public static class TestWaveModule extends BasicWaveTestModule {
        private SimpleLatticeGraphic simpleLatticeGraphic;

        public TestWaveModule() {
            super( "Test Waves" );

            simpleLatticeGraphic = new SimpleLatticeGraphic( super.getLattice(), 10, 10, new IndexColorMap( super.getLattice() ) );
            super.getPhetPCanvas().addScreenChild( simpleLatticeGraphic );

            BasicWaveTestControlPanel controlPanel = new BasicWaveTestControlPanel( this );
            final ModelSlider cellDim = new ModelSlider( "Cell Dimension", "pixels", 1, 50, simpleLatticeGraphic.getCellDimensions().width );
            cellDim.addChangeListener( new ChangeListener() {
                public void stateChanged( ChangeEvent e ) {
                    int dim = (int)cellDim.getValue();
                    simpleLatticeGraphic.setCellDimensions( dim, dim );
                }
            } );
            controlPanel.addControl( cellDim );
            setControlPanel( controlPanel );
        }

        protected void step() {
            super.step();
            simpleLatticeGraphic.update();
        }

    }

    public TestWave( String[]args ) {
        super( args, "Feasibility Test", "Test", "0.01" );
        addModule( new TestWaveModule() );
    }


    public static void main( String[] args ) {
        new TestWave( args ).startApplication();
    }

}
