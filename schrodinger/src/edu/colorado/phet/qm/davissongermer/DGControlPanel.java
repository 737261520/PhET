/* Copyright 2004, Sam Reid */
package edu.colorado.phet.qm.davissongermer;

import edu.colorado.phet.common.view.ControlPanel;
import edu.colorado.phet.common.view.ModelSlider;
import edu.colorado.phet.qm.controls.ClearButton;
import edu.colorado.phet.qm.controls.RulerPanel;
import edu.colorado.phet.qm.model.DiscreteModel;
import edu.colorado.phet.qm.view.gun.AbstractGunGraphic;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * User: Sam Reid
 * Date: Feb 4, 2006
 * Time: 10:54:25 PM
 * Copyright (c) Feb 4, 2006 by Sam Reid
 */

public class DGControlPanel extends ControlPanel {
    private DGModule dgModule;
    private DGPlotFrame dgPlotFrame;

    public DGControlPanel( DGModule dgModule ) {
        super( dgModule );
        this.dgModule = dgModule;
        addRulerPanel();
        addProtractorPanel();
        addControl( new ClearButton( dgModule.getSchrodingerPanel() ) );


        final JCheckBox plotCheckBox = new JCheckBox( "Plot" );
        plotCheckBox.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                DGControlPanel.this.setPlotVisible( plotCheckBox.isSelected() );
            }
        } );
        addControl( plotCheckBox );


        addControlFullWidth( new AtomLatticeControlPanel( dgModule.getDGModel() ) );

        dgPlotFrame = new DGPlotFrame( dgModule.getPhetFrame(), dgModule );

        AbstractGunGraphic gun = dgModule.getSchrodingerPanel().getSchrodingerScreenNode().getGunGraphic();
        if( gun instanceof DGGun ) {
            DGGun dgGun = (DGGun)gun;
            final DGParticle particle = dgGun.getDgParticle();
            final ModelSlider covariance = new ModelSlider( "Covariance", "", 0, 0.3, particle.getCovariance() );
            covariance.addChangeListener( new ChangeListener() {
                public void stateChanged( ChangeEvent e ) {
                    particle.setCovariance( covariance.getValue() );
                }
            } );
            addControl( covariance );

            final ModelSlider y0 = new ModelSlider( "Particle y0", "", 0, 1.0, particle.getStartYFraction() );
            y0.addChangeListener( new ChangeListener() {
                public void stateChanged( ChangeEvent e ) {
                    particle.setStartYFraction( y0.getValue() );
                }
            } );
            addControl( y0 );
        }
    }

    private void setPlotVisible( boolean selected ) {
        dgPlotFrame.setVisible( selected );
    }

    private DiscreteModel getDiscreteModel() {
        return dgModule.getDiscreteModel();
    }

    private void addRulerPanel() {
        try {
            RulerPanel rulerPanel = new RulerPanel( dgModule.getSchrodingerPanel() );
            addControl( rulerPanel );
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }

    private void addProtractorPanel() {
        final JCheckBox protractor = new JCheckBox( "Protractor", false );
        protractor.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dgModule.setProtractorVisible( protractor.isSelected() );
            }
        } );
        addControl( protractor );
    }
}
