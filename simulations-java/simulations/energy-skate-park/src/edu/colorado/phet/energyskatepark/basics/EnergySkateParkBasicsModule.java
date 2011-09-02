// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.energyskatepark.basics;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import edu.colorado.phet.common.phetcommon.util.function.VoidFunction0;
import edu.colorado.phet.common.phetcommon.view.PhetFrame;
import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.nodes.ControlPanelNode;
import edu.colorado.phet.common.piccolophet.nodes.TextButtonNode;
import edu.colorado.phet.common.piccolophet.nodes.layout.VBox;
import edu.colorado.phet.energyskatepark.AbstractEnergySkateParkModule;
import edu.colorado.phet.energyskatepark.model.EnergySkateParkOptions;
import edu.colorado.phet.energyskatepark.model.Planet;
import edu.colorado.phet.energyskatepark.serialization.EnergySkateParkIO;
import edu.colorado.phet.energyskatepark.view.EnergySkateParkLookAndFeel;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.pswing.PSwing;

/**
 * Module for Energy Skate Park Basics.
 *
 * @author Sam Reid
 */
public class EnergySkateParkBasicsModule extends AbstractEnergySkateParkModule {
    public static final double INSET = 5;
    public final ControlPanelNode energyGraphControlPanel;
    public final String PARABOLA = "energy-skate-park/tracks/basics/parabola.esp";
    private static final Font CONTROL_FONT = new PhetFont( 16 );

    public EnergySkateParkBasicsModule( String name, PhetFrame phetFrame ) {
        super( name, phetFrame, new EnergySkateParkOptions() );

        //Don't allow users to apply rocket force with the keyboard
        energySkateParkSimulationPanel.setThrustEnabled( false );

        //Show the sky for the earth background
        new Planet.Earth().apply( this );

        //Control panels are floating not docked
        setControlPanel( null );

        //Add the energy graph control panel
        energyGraphControlPanel = new ControlPanelNode( new VBox(
                new PText( "Energy Graphs" ) {{setFont( new PhetFont( 16, true ) );}},

                //Checkbox to show/hide the pie chart
                new PSwing( new JCheckBox( "Pie Chart", isPieChartVisible() ) {{
                    setFont( CONTROL_FONT );
                    addActionListener( new ActionListener() {
                        public void actionPerformed( ActionEvent e ) {
                            setPieChartVisible( isSelected() );
                        }
                    } );
                }} ),
                new TextButtonNode( "Bar Chart" ) {{
                    setFont( CONTROL_FONT );
                }}
        ), EnergySkateParkLookAndFeel.backgroundColor ) {{

            //Set its location when the layout changes in the piccolo node, since this sim isn't using stage coordinates
            energySkateParkSimulationPanel.getRootNode().addLayoutListener( new VoidFunction0() {
                public void apply() {
                    setOffset( energySkateParkSimulationPanel.getWidth() - getFullBounds().getWidth() - INSET, INSET );
                }
            } );
        }};
        energySkateParkSimulationPanel.getRootNode().addChild( energyGraphControlPanel );

        //Move the legend out from behind the control panel
        energySkateParkSimulationPanel.getRootNode().addLayoutListener( new VoidFunction0() {
            public void apply() {
                energySkateParkSimulationPanel.getRootNode().getLegend().translate( -energyGraphControlPanel.getFullBounds().getWidth() - INSET, 0 );
            }
        } );
    }

    //Load the specified track and set its roller coaster mode, used when the user presses different track selection buttons
    public void load( String location, boolean rollerCoasterMode ) {
        EnergySkateParkIO.open( location, this );
        getEnergySkateParkModel().setRollerCoasterMode( rollerCoasterMode );
    }

    //Show the reset all button below the bottom control panel
    public void addResetAllButton( final PNode parent ) {

        //Add the "Reset all" button
        energySkateParkSimulationPanel.getRootNode().addChild( new TextButtonNode( "Reset All" ) {{

            //Set its location when the layout changes in the piccolo node, since this sim isn't using stage coordinates
            energySkateParkSimulationPanel.getRootNode().addLayoutListener( new VoidFunction0() {
                public void apply() {
                    setOffset( energyGraphControlPanel.getFullBounds().getCenterX() - getFullBounds().getWidth() / 2, parent.getFullBounds().getMaxY() + INSET * 2 );
                }
            } );
        }} );
    }

    //Show buttons that allows the user to choose different tracks
    public void addTrackSelectionControlPanel() {
        final ControlPanelNode trackSelectionNode = new ControlPanelNode( new VBox(
                new TrackButton( this, PARABOLA, true ),
                new TrackButton( this, "energy-skate-park/tracks/basics/to-the-floor.esp", true ),
                new TrackButton( this, "energy-skate-park/tracks/basics/double-well.esp", true )
        ), EnergySkateParkLookAndFeel.backgroundColor ) {{

            //Set its location when the layout changes in the piccolo node, since this sim isn't using stage coordinates
            energySkateParkSimulationPanel.getRootNode().addLayoutListener( new VoidFunction0() {
                public void apply() {
                    setOffset( INSET, INSET );
                }
            } );
        }};
        energySkateParkSimulationPanel.getRootNode().addChild( trackSelectionNode );
    }
}