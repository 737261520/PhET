/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.solublesalts.control;

import edu.colorado.phet.common.view.ControlPanel;
import edu.colorado.phet.common.view.components.ModelSlider;
import edu.colorado.phet.common.model.ModelElement;
import edu.colorado.phet.common.application.PhetApplication;
import edu.colorado.phet.solublesalts.model.*;
import edu.colorado.phet.solublesalts.model.crystal.Crystal;
import edu.colorado.phet.solublesalts.model.affinity.RandomAffinity;
import edu.colorado.phet.solublesalts.module.SolubleSaltsModule;
import edu.colorado.phet.solublesalts.util.DefaultGridBagConstraints;
import edu.colorado.phet.solublesalts.SolubleSaltsConfig;
import edu.colorado.phet.solublesalts.view.IonGraphicManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.List;

/**
 * SolubleSaltsControlPanel
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class SolubleSaltsControlPanel extends ControlPanel {
    private ModelSlider vesselIonStickSlider;
    private ModelSlider vesselIonReleaseSlider;
    private ModelSlider dissociationSlider;

    public SolubleSaltsControlPanel( final SolubleSaltsModule module ) {
        super( module );

        final SolubleSaltsModel model = (SolubleSaltsModel)module.getModel();

        addControl( makeSodiumPanel( model ) );
        addControl( makeChloridePanel( model ) );
//        addControl( makeHeatControl( model ) );


        // Sliders for affinity adjustment
        vesselIonStickSlider = new ModelSlider( "Lattice stick likelihood",
                                                "",
                                                0,
                                                1,
                                                0,
                                                new DecimalFormat( "0.00" ) );
        vesselIonStickSlider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                model.getVessel().setIonStickAffinity( new RandomAffinity( vesselIonStickSlider.getValue() ) );
            }
        } );
        vesselIonStickSlider.setValue( 0.9 );
        vesselIonStickSlider.setNumMajorTicks( 5 );

        dissociationSlider = new ModelSlider( "Lattice dissociation likelihood",
                                              "",
                                              0,
                                              1,
                                              0,
                                              new DecimalFormat( "0.000" ) );
        dissociationSlider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                Crystal.setDissociationLikelihood( dissociationSlider.getValue() );
            }
        } );
        dissociationSlider.setValue( 0.01 );
        dissociationSlider.setNumMajorTicks( 5 );

        addControl( vesselIonStickSlider );
        addControl( dissociationSlider );
        addControl( makeConcentrationPanel( model ) );
        addControl( makeWaterLevelPanel( model ) );

        // Zoom button
        final JToggleButton zoomButton = new JToggleButton( "Zoom" );
        final ZoomDlg zoomDlg = new ZoomDlg();
        zoomButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                module.setZoomEnabled( zoomButton.isSelected() );
                zoomDlg.setVisible( zoomButton.isSelected() );
            }
        } );
        addControl( zoomButton );

        // Reset button
        JButton resetBtn = new JButton( "Reset" );
        resetBtn.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent arg0 ) {
                model.reset();
            }
        } );
        addControl( resetBtn );
    }

    private JPanel makeConcentrationPanel( final SolubleSaltsModel model ) {
        final ModelSlider kspSlider = new ModelSlider( "Ksp", "", 0, 3E-4, 0 );
        kspSlider.setSliderLabelFormat( new DecimalFormat( "0E00" ) );
        kspSlider.setTextFieldFormat( new DecimalFormat( "0E00" ) );
        kspSlider.setNumMajorTicks( 3 );
        kspSlider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                model.setKsp( kspSlider.getValue() );
            }
        } );
        model.setKsp( kspSlider.getValue() );

        final JTextField concentrationTF = new JTextField( 8 );
        model.addModelElement( new ModelElement() {
            DecimalFormat format = new DecimalFormat( "0E00" );

            public void stepInTime( double dt ) {
                double concentration = model.getConcentration();
                concentrationTF.setText( format.format( concentration ) );
            }
        } );

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setBorder( BorderFactory.createTitledBorder( "Concentration" ) );
        GridBagConstraints gbc = new DefaultGridBagConstraints();
        gbc.gridwidth = 2;
        panel.add( kspSlider, gbc );
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add( new JLabel( "Concentration:" ), gbc );
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add( concentrationTF, gbc );

        return panel;
    }


    private Component makeHeatControl( final SolubleSaltsModel model ) {
        JPanel heatControlPanel = new JPanel();
        final ModelSlider heatSlider = new ModelSlider( "Heat Control", "", -20, 20, 0 );
        heatSlider.setSliderLabelFormat( new DecimalFormat( "#0" ) );
        heatSlider.setTextFieldFormat( new DecimalFormat( "#0" ) );
        heatSlider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                model.getHeatSource().setHeatChangePerClockTick( heatSlider.getValue() );
            }
        } );
        heatSlider.getSlider().addMouseListener( new MouseAdapter() {
            public void mouseReleased( MouseEvent e ) {
                heatSlider.setValue( 0 );
            }
        } );
        heatControlPanel.add( heatSlider );
        return heatControlPanel;
    }

    /**
     * @param model
     * @return
     */
    private JPanel makeSodiumPanel( final SolubleSaltsModel model ) {
        JPanel sodiumPanel = new JPanel( new GridBagLayout() );
        GridBagConstraints gbc = new DefaultGridBagConstraints();

        JLabel label = new JLabel( "Sodium", new ImageIcon( IonGraphicManager.getIonImage( Sodium.class ) ), JLabel.LEADING );
        gbc.anchor = GridBagConstraints.EAST;
        sodiumPanel.add( label, gbc );

        // Spinners for the number of ions
        final JSpinner spinner = new JSpinner( new SpinnerNumberModel( model.getNumIonsOfType( Sodium.class ),
                                                                       0,
                                                                       100,
                                                                       1 ) );
        model.addIonListener( new IonCountSyncAgent( model, Sodium.class, spinner ) );
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        sodiumPanel.add( spinner, gbc );

        spinner.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                int dIons = ( (Integer)spinner.getValue() ).intValue()
                            - model.getNumIonsOfType( Sodium.class );
                if( dIons > 0 ) {
                    for( int i = 0; i < dIons; i++ ) {
                        Ion ion = new Sodium();
                        IonInitializer.initialize( ion, model );
                        model.addModelElement( ion );
                    }
                }
                if( dIons < 0 ) {
                    for( int i = dIons; i < 0; i++ ) {
                        List ions = model.getIonsOfType( Sodium.class );
                        if( ions != null ) {
                            Ion ion = (Ion)ions.get( 0 );
                            model.removeModelElement( ion );
                        }
                    }
                }
            }
        } );

        JTextField ionCountTF = new JTextField( 4 );
        model.addIonListener( new FreeIonCountSyncAgent( model, Sodium.class, ionCountTF ) );
        gbc.gridx++;
        sodiumPanel.add( ionCountTF, gbc );

        return sodiumPanel;
    }

    /**
     * @param model
     * @return
     */
    private JPanel makeChloridePanel( final SolubleSaltsModel model ) {
        JPanel panel = new JPanel( new GridBagLayout() );
        GridBagConstraints gbc = new DefaultGridBagConstraints();

        JLabel label = new JLabel( "Chloride",
                                   new ImageIcon( IonGraphicManager.getIonImage( Chloride.class ) ),
                                   JLabel.LEADING );
        gbc.anchor = GridBagConstraints.EAST;
        panel.add( label, gbc );

        // Spinners for the number of ions
        final JSpinner spinner = new JSpinner( new SpinnerNumberModel( model.getNumIonsOfType( Chloride.class ),
                                                                       0,
                                                                       100,
                                                                       1 ) );
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add( spinner, gbc );

        spinner.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                int dIons = ( (Integer)spinner.getValue() ).intValue()
                            - model.getNumIonsOfType( Chloride.class );
                if( dIons > 0 ) {
                    for( int i = 0; i < dIons; i++ ) {
                        Ion ion = new Chloride();
                        IonInitializer.initialize( ion, model );
                        model.addModelElement( ion );
                    }
                }
                if( dIons < 0 ) {
                    for( int i = dIons; i < 0; i++ ) {
                        List ions = model.getIonsOfType( Chloride.class );
                        if( ions != null ) {
                            Ion ion = (Ion)ions.get( 0 );
                            model.removeModelElement( ion );
                        }
                    }
                }
            }
        } );

        model.addIonListener( new IonCountSyncAgent( model, Chloride.class, spinner ) );
        return panel;
    }

    private JPanel makeWaterLevelPanel( final SolubleSaltsModel model ) {
        JPanel panel = new JPanel();
        final ModelSlider slider = new ModelSlider( "Water level", "",
                                                    0,
                                                    model.getVessel().getDepth(),
                                                    model.getVessel().getWaterLevel() );
        slider.setTextFieldFormat( new DecimalFormat( "#" ) );
        slider.setNumMajorTicks( 6 );
        slider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                model.getVessel().setWaterLevel( slider.getValue() );
            }
        } );
        panel.add( slider );
        return panel;
    }

    private class IonCountSyncAgent implements SolubleSaltsModel.IonListener {
        private SolubleSaltsModel model;
        private Class ionClass;
        private JSpinner spinner;

        public IonCountSyncAgent( SolubleSaltsModel model, Class ionClass, JSpinner spinner ) {
            this.model = model;
            this.ionClass = ionClass;
            this.spinner = spinner;
        }

        public void ionAdded( SolubleSaltsModel.IonEvent event ) {
            syncSpinner();
        }

        public void ionRemoved( SolubleSaltsModel.IonEvent event ) {
            syncSpinner();
        }

        private void syncSpinner() {
            if( model.getIonsOfType( ionClass ) != null
                && model.getIonsOfType( ionClass ).size() != ( (Integer)spinner.getValue() ).intValue() ) {
                spinner.setValue( new Integer( model.getIonsOfType( ionClass ).size() ) );
            }
        }
    }

    private class FreeIonCountSyncAgent implements SolubleSaltsModel.IonListener {
        private SolubleSaltsModel model;
        private Class ionClass;
        private JTextField testField;

        public FreeIonCountSyncAgent( SolubleSaltsModel model, Class ionClass, JTextField testField ) {
            this.model = model;
            this.ionClass = ionClass;
            this.testField = testField;
        }

        public void ionAdded( SolubleSaltsModel.IonEvent event ) {
            syncSpinner();
        }

        public void ionRemoved( SolubleSaltsModel.IonEvent event ) {
            syncSpinner();
        }

        private void syncSpinner() {
            int freeIonCnt = 0;
            List ions = model.getIonsOfType( ionClass );
            if( ions != null ) {
                for( int i = 0; i < ions.size(); i++ ) {
                    Ion ion = (Ion)ions.get( i );
                    if( !ion.isBound() ) {
                        freeIonCnt++;
                    }
                }
            }
            testField.setText( Integer.toString( freeIonCnt ) );
        }
    }


    /**
     * A modelss dialog that explains how to zoom. This is a temporary thing
     */
    class ZoomDlg extends JDialog {
        public ZoomDlg() throws HeadlessException {
            super( PhetApplication.instance().getPhetFrame(), false );
            String text = "To Zoom:"
                          + "\t\n\t1) Move the mouse to the spot you would like to zoom to."
                          + "\t\n\t2) Press the left mouse button."
                          + "\t\n\t3) Move the mouse to the right to zoom in,"
                          + "\t\n\t4) Move the mouse to the left to zoom out."
                    ;
            JTextArea textArea = new JTextArea( text );
            textArea.setBackground( new Color( 255, 255, 160 ) );
            getContentPane().add( textArea );
            pack();
//            setLocation( 100, 30);
            setLocationRelativeTo( PhetApplication.instance().getPhetFrame() );
        }
    }
}
