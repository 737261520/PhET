/* Copyright 2007, University of Colorado */

package edu.colorado.phet.opticaltweezers.control.developer;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.colorado.phet.common.phetcommon.view.controls.valuecontrol.LinearValueControl;
import edu.colorado.phet.common.phetcommon.view.util.EasyGridBagLayout;
import edu.colorado.phet.opticaltweezers.model.DNAStrand;
import edu.colorado.phet.opticaltweezers.view.DNAStrandNode;

/**
 * DNAStrandControlPanel constains developer controls for the DNA strand model.
 * This panel is for developers only, and it not localized.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class DNAStrandControlPanel extends JPanel implements Observer, PropertyChangeListener {

    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private DNAStrand _dnaStrand;
    private DNAStrandNode _dnaStrandNode;
    
    private JCheckBox _pivotsCheckBox;
    private JCheckBox _extensionCheckBox;
    private LinearValueControl _springConstantControl;
    private LinearValueControl _dragCoefficientControl;
    private LinearValueControl _kickConstant;
    private LinearValueControl _numberOfEvolutionsPerClockTickControl;
    private LinearValueControl _evolutionDtScaleControl;
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    public DNAStrandControlPanel( Font titleFont, Font controlFont, DNAStrand dnaStrand, DNAStrandNode dnaStrandNode ) {
        super();
        
        _dnaStrand = dnaStrand;
        _dnaStrand.addObserver( this );
        
        _dnaStrandNode = dnaStrandNode;
        _dnaStrandNode.addPropertyChangeListener( this );
        
        TitledBorder border = new TitledBorder( "DNA strand model" );
        border.setTitleFont( titleFont );
        this.setBorder( border );
        
        _pivotsCheckBox = new JCheckBox( "Show pivots" );
        _pivotsCheckBox.setToolTipText( 
                "<html>Determines whether you can see the<br>" + 
                "pivot points used to model the strand</html>" );
        _pivotsCheckBox.setFont( controlFont );
        _pivotsCheckBox.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent event ) {
                handlePivotsCheckBox();
            }
        } );
        
        _extensionCheckBox = new JCheckBox( "Show extension" );
        _extensionCheckBox.setToolTipText( 
                "<html>Draws a straight line between<br>" + 
                "the ends of the DNA strand</html>" );
        _extensionCheckBox.setFont( controlFont );
        _extensionCheckBox.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent event ) {
                handleExtensionCheckBox();
            }
        } );
        
        double min = dnaStrand.getSpringConstantRange().getMin();
        double max = dnaStrand.getSpringConstantRange().getMax();
        _springConstantControl = new LinearValueControl( min, max, "k/m (spring):", "0.00", "" );
        _springConstantControl.setToolTipText( "<html>spring constant divided by mass<html>" );
        _springConstantControl.setUpDownArrowDelta( 0.01 );
        _springConstantControl.setFont( controlFont );
        _springConstantControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent event ) {
                handleSpringConstantControl();
            }
        });
        
        min = dnaStrand.getDragCoefficientRange().getMin();
        max = dnaStrand.getDragCoefficientRange().getMax();
        _dragCoefficientControl = new LinearValueControl( min, max, "b (drag):", "0.00", "" );
        _dragCoefficientControl.setToolTipText( "<html>drag coefficient</html>" );
        _dragCoefficientControl.setUpDownArrowDelta( 0.01 );
        _dragCoefficientControl.setFont( controlFont );
        _dragCoefficientControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent event ) {
                handleDampingConstantControl();
            }
        });
        
        min = dnaStrand.getKickConstantRange().getMin();
        max = dnaStrand.getKickConstantRange().getMax();
        _kickConstant = new LinearValueControl( min, max, "k (kick):", "0.00", "" );
        _kickConstant.setToolTipText( "<html>kick applied to velocity on each evolution</html>" );
        _kickConstant.setUpDownArrowDelta( 0.01 );
        _kickConstant.setFont( controlFont );
        _kickConstant.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent event ) {
                handleKickControl();
            }
        });
        
        min = dnaStrand.getNumberOfEvolutionsPerClockTickRange().getMin();
        max = dnaStrand.getNumberOfEvolutionsPerClockTickRange().getMax();
        _numberOfEvolutionsPerClockTickControl = new LinearValueControl( min, max, "evolutions per clockstep:", "#0", "" );
        _numberOfEvolutionsPerClockTickControl.setToolTipText( 
                "<html>number of times to run the evolution<br>" + 
                "algorithm each time the clock ticks</html>" );
        _numberOfEvolutionsPerClockTickControl.setUpDownArrowDelta( 1 );
        _numberOfEvolutionsPerClockTickControl.setFont( controlFont );
        _numberOfEvolutionsPerClockTickControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent event ) {
                handleNumberOfEvolutionsPerClockTickControl();
            }
        });
        
        min = dnaStrand.getEvolutionDtScaleRange().getMin();
        max = dnaStrand.getEvolutionDtScaleRange().getMax();
        _evolutionDtScaleControl = new LinearValueControl( min, max, "evolution dt scale", "###0", "" );
        _evolutionDtScaleControl.setToolTipText( 
                "<html>The simulation clock's scale is much too small to see the strand evolve.<br>" +
                "dt used in the evolution algorithm is:<br>" + 
                "evolutionDtScale * clockstep / evolutionsPerClockstep</html>" );
        _evolutionDtScaleControl.setUpDownArrowDelta( 0.01 );
        _evolutionDtScaleControl.setFont( controlFont );
        _evolutionDtScaleControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent event ) {
                handleEvolutionDtScaleControl();
            }
        });
        
        // Layout
        EasyGridBagLayout layout = new EasyGridBagLayout( this );
        layout.setInsets( new Insets( 0, 0, 0, 0 ) );
        this.setLayout( layout );
        int row = 0;
        int column = 0;
        layout.addComponent( _pivotsCheckBox, row++, column );
        layout.addComponent( _extensionCheckBox, row++, column );
        layout.addComponent( _springConstantControl, row++, column );
        layout.addComponent( _dragCoefficientControl, row++, column );
        layout.addComponent( _kickConstant, row++, column );
        layout.addComponent( _numberOfEvolutionsPerClockTickControl, row++, column );
        layout.addComponent( _evolutionDtScaleControl, row++, column );
        
        // Default state
        _pivotsCheckBox.setSelected( _dnaStrandNode.isPivotsVisible() );
        _extensionCheckBox.setSelected( _dnaStrandNode.isExtensionVisible() );
        _springConstantControl.setValue( _dnaStrand.getSpringConstant() );
        _dragCoefficientControl.setValue( _dnaStrand.getDragCoefficient() );
        _kickConstant.setValue( _dnaStrand.getKickConstant() );
        _numberOfEvolutionsPerClockTickControl.setValue( _dnaStrand.getNumberOfEvolutionsPerClockTick() );
        _evolutionDtScaleControl.setValue( _dnaStrand.getEvolutionDtScale() );
    }
    
    public void cleanup() {
        _dnaStrand.deleteObserver( this );
        _dnaStrandNode.removePropertyChangeListener( this );
    }
    
    //----------------------------------------------------------------------------
    // Event handlers
    //----------------------------------------------------------------------------
    
    private void handlePivotsCheckBox() {
        _dnaStrandNode.setPivotsVisible( _pivotsCheckBox.isSelected() );
    }
    
    private void handleExtensionCheckBox() {
        _dnaStrandNode.setExtensionVisible( _extensionCheckBox.isSelected() );
    }
    
    private void handleSpringConstantControl() {
        double value = _springConstantControl.getValue();
        _dnaStrand.setSpringConstant( value );
    }
    
    private void handleDampingConstantControl() {
        double value = _dragCoefficientControl.getValue();
        _dnaStrand.setDragCoefficient( value );
    }
    
    private void handleKickControl() {
        double value = _kickConstant.getValue();
        _dnaStrand.setKickConstant( value );
    }
    
    private void handleNumberOfEvolutionsPerClockTickControl() {
        int value = (int) Math.round( _numberOfEvolutionsPerClockTickControl.getValue() );
        _dnaStrand.setNumberOfEvolutionsPerClockTick( value );
    }
    
    private void handleEvolutionDtScaleControl() {
        double value = _evolutionDtScaleControl.getValue();
        _dnaStrand.setEvolutionDtScale( value );
    }

    //----------------------------------------------------------------------------
    // Observer implementation
    //----------------------------------------------------------------------------
    
    public void update( Observable o, Object arg ) {
        if ( o == _dnaStrand ) {
            if ( arg == DNAStrand.PROPERTY_SPRING_CONSTANT ) {
                _springConstantControl.setValue( _dnaStrand.getSpringConstant() );
            }
            else if ( arg == DNAStrand.PROPERTY_DRAG_COEFFICIENT ) {
                _dragCoefficientControl.setValue( _dnaStrand.getDragCoefficient() );
            }
            else if ( arg == DNAStrand.PROPERTY_KICK_CONSTANT ) {
                _kickConstant.setValue( _dnaStrand.getKickConstant() );
            }
            else if ( arg == DNAStrand.PROPERTY_NUMBER_OF_EVOLUTIONS_PER_CLOCK_TICK ) {
                _numberOfEvolutionsPerClockTickControl.setValue( _dnaStrand.getNumberOfEvolutionsPerClockTick() ); 
            }
            else if ( arg == DNAStrand.PROPERTY_EVOLUTION_DT_SCALE ) {
                _evolutionDtScaleControl.setValue( _dnaStrand.getEvolutionDtScale() );
            }
        }
    }

    //----------------------------------------------------------------------------
    // PropertChangeListener implementation
    //----------------------------------------------------------------------------
    
    public void propertyChange( PropertyChangeEvent event ) {
        if ( event.getSource() == _dnaStrandNode ) {
            if ( event.getPropertyName() == DNAStrandNode.PROPERTY_PIVOTS_VISIBLE ) {
                _pivotsCheckBox.setSelected( _dnaStrandNode.isPivotsVisible() );
            }
            else if ( event.getPropertyName() == DNAStrandNode.PROPERTY_EXTENSION_VISIBLE ) {
                _extensionCheckBox.setSelected( _dnaStrandNode.isExtensionVisible() );
            }
        }
    }
}
