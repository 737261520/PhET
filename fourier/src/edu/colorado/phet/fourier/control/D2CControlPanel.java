/* Copyright 2005, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.fourier.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.colorado.phet.common.view.util.EasyGridBagLayout;
import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.fourier.FourierConstants;
import edu.colorado.phet.fourier.control.sliders.K1SpacingSlider;
import edu.colorado.phet.fourier.control.sliders.KWidthSlider;
import edu.colorado.phet.fourier.control.sliders.XWidthSlider;
import edu.colorado.phet.fourier.module.FourierModule;
import edu.colorado.phet.fourier.view.D2CAmplitudesGraph;



/**
 * D2CControlPanel
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class D2CControlPanel extends FourierControlPanel {

    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------
    
    private static final int TITLED_BORDER_WIDTH = 1;
    
    private static final int MIN_SPACING = 0;
    private static final int MAX_SPACING = 100;
    private static final int MIN_X_WIDTH = 0;
    private static final int MAX_X_WIDTH = 100;
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    // Things to be controlled.
    private D2CAmplitudesGraph _amplitudesGraph;

    // UI components
    private FourierComboBox _domainComboBox;
    private K1SpacingSlider _k1SpacingSlider;
    private JCheckBox _continuousCheckBox;
    private KWidthSlider _kWidthSlider;
    private XWidthSlider _xWidthSlider;
    private FourierComboBox _waveTypeComboBox;
    
    // Choices
    private ArrayList _domainChoices;
    private ArrayList _waveTypeChoices;
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    /**
     * Sole constructor.
     * 
     * @param module
     * @param amplitudesGraph
     */
    public D2CControlPanel( FourierModule module, D2CAmplitudesGraph amplitudesGraph ) {
        super( module );
        
        assert( amplitudesGraph != null );
        _amplitudesGraph = amplitudesGraph;
        
        // Set the control panel's minimum width.
        String widthString = SimStrings.get( "D2CControlPanel.width" );
        int width = Integer.parseInt( widthString );
        setMinumumWidth( width );

        JPanel miscPanel = new JPanel();
        {
            String title = SimStrings.get( "D2CControlPanel.gaussianWavePacket" );
            miscPanel.setBorder( new TitledBorder( title ) );
            
            // Domain
            {
                // Label
                String label = SimStrings.get( "D2CControlPanel.domain" );

                // Choices
                _domainChoices = new ArrayList();
                _domainChoices.add( new FourierComboBox.Choice( FourierConstants.DOMAIN_SPACE, SimStrings.get( "domain.space" ) ) );
                _domainChoices.add( new FourierComboBox.Choice( FourierConstants.DOMAIN_TIME, SimStrings.get( "domain.time" ) ) );
 
                // Function combo box
                _domainComboBox = new FourierComboBox( label, _domainChoices );
            }

            // Wave Type
            {
                // Label
                String label = SimStrings.get( "D2CControlPanel.waveType" );

                // Choices
                _waveTypeChoices = new ArrayList();
                _waveTypeChoices.add( new FourierComboBox.Choice( FourierConstants.WAVE_TYPE_SINE, SimStrings.get( "waveType.sines" ) ) );
                _waveTypeChoices.add( new FourierComboBox.Choice( FourierConstants.WAVE_TYPE_COSINE, SimStrings.get( "waveType.cosines" ) ) );

                // Wave Type combo box
                _waveTypeComboBox = new FourierComboBox( label, _waveTypeChoices );
            }
            
            // Layout
            EasyGridBagLayout layout = new EasyGridBagLayout( miscPanel );
            miscPanel.setLayout( layout );
            int row = 0;
            layout.addComponent( _domainComboBox, row++, 0 );
            layout.addComponent( _waveTypeComboBox, row++, 0 );
        }
        
        JPanel packetSpacingPanel = new JPanel();
        {
            String title = SimStrings.get( "D2CControlPanel.packetSpacing" );
            packetSpacingPanel.setBorder( new TitledBorder( title ) );
            
            // Spacing
            _k1SpacingSlider = new K1SpacingSlider();

            // Continuous checkbox
            _continuousCheckBox = new JCheckBox( SimStrings.get( "D2CControlPanel.continuous" ) );
            
            // Layout
            EasyGridBagLayout layout = new EasyGridBagLayout( packetSpacingPanel );
            packetSpacingPanel.setLayout( layout );
            int row = 0;
            layout.addComponent( _k1SpacingSlider, row++, 0 );
            layout.addComponent( _continuousCheckBox, row++, 0 );
        }
        
        // Packet width panel
        JPanel packetWidthPanel = new JPanel();
        {
            String title = SimStrings.get( "D2CControlPanel.packetWidth" );
            packetWidthPanel.setBorder( new TitledBorder( title ) );
            
            // k-space width
            _kWidthSlider = new KWidthSlider();

            // x-space width
            _xWidthSlider = new XWidthSlider();
            
            // Layout
            EasyGridBagLayout layout = new EasyGridBagLayout( packetWidthPanel );
            packetWidthPanel.setLayout( layout );
            int row = 0;
            layout.addComponent( _kWidthSlider, row++, 0 );
            layout.addComponent( _xWidthSlider, row++, 0 );
        }

        // Layout
        addFullWidth( miscPanel );
        addVerticalSpace( 15 );
        addFullWidth( packetSpacingPanel );
        addVerticalSpace( 15 );
        addFullWidth( packetWidthPanel );
        
        // Set the state of the controls.
        reset();
        
        // Wire up event handling (after setting state with reset).
        {
            EventListener listener = new EventListener();
            _domainComboBox.addItemListener( listener );
            _k1SpacingSlider.addChangeListener( listener );
            _continuousCheckBox.addActionListener( listener );
            _kWidthSlider.addChangeListener( listener );
            _xWidthSlider.addChangeListener( listener );
            _waveTypeComboBox.addItemListener( listener );
        }
    }
    
    public void reset() {
        
        _domainComboBox.setSelectedKey( FourierConstants.DOMAIN_SPACE );
        
        _amplitudesGraph.setDomain( FourierConstants.DOMAIN_SPACE );
        
        _k1SpacingSlider.setValue( 2 * Math.PI );
        _kWidthSlider.setValue( 3 * Math.PI );
        _xWidthSlider.setValue( 1 / ( 3 * Math.PI ) );
    }
    
    //----------------------------------------------------------------------------
    // Inner classes
    //----------------------------------------------------------------------------
    
    /*
     * EventListener is a nested class that is private to this control panel.
     * It handles dispatching of all events generated by the controls.
     */
    private class EventListener implements ActionListener, ChangeListener, ItemListener {

        public EventListener() {}

        public void actionPerformed( ActionEvent event ) {
            if ( event.getSource() == _continuousCheckBox ) {
                handleContinuous();
            }
            else {
                throw new IllegalArgumentException( "unexpected event: " + event );
            }
        }
        
        public void stateChanged( ChangeEvent event ) {

            if ( event.getSource() == _k1SpacingSlider ) {
                handleSpacing();
            }
            else if ( event.getSource() == _kWidthSlider ) {
                handleKWidth();
            }
            else if ( event.getSource() == _xWidthSlider ) {
                handleXWidth();
            }
            else {
                throw new IllegalArgumentException( "unexpected event: " + event );
            }
        }

        public void itemStateChanged( ItemEvent event ) {
            if ( event.getStateChange() == ItemEvent.SELECTED ) {
                if ( event.getSource() == _domainComboBox.getComboBox() ) {
                    handleDomain();
                }
                else if ( event.getSource() == _waveTypeComboBox.getComboBox() ) {
                    handleWaveType();
                }
                else {
                    throw new IllegalArgumentException( "unexpected event: " + event );
                }
            }
        } 
    }
    
    //----------------------------------------------------------------------------
    // Event handling
    //----------------------------------------------------------------------------
    
    private int _oldK1WidthSliderValue;
    
    private void handleContinuous() {
        System.out.println( "continuous=" + _continuousCheckBox.isSelected() );//XXX
    }
    
    private void handleSpacing() {
        System.out.println( "spacing=" + _k1SpacingSlider.getValue() );//XXX
    }
    
    private void handleKWidth() {
        System.out.println( "k width=" + _kWidthSlider.getValue() );//XXX
        double kWidth = _kWidthSlider.getValue();
        _xWidthSlider.setValue( 1/kWidth );
    }
    
    private void handleXWidth() {
        System.out.println( "x width=" + _xWidthSlider.getValue() );//XXX
        double xWidth = _xWidthSlider.getValue();
        _kWidthSlider.setValue( 1/xWidth );
    }
    
    private void handleDomain() {
        int domain = _domainComboBox.getSelectedKey();
        System.out.println( "domain=" + domain );//XXX
        _amplitudesGraph.setDomain( domain );
    }
    
    private void handleWaveType() {
        System.out.println( "wave type=" + _waveTypeComboBox.getSelectedItem() );//XXX
    }

}
