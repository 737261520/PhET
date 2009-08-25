package edu.colorado.phet.titration.prototype;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.colorado.phet.common.phetcommon.view.controls.valuecontrol.LogarithmicValueControl;
import edu.colorado.phet.common.phetcommon.view.util.EasyGridBagLayout;


public class TPControlPanel extends JPanel {
    
    private static final String VOLUME_UNITS = "mL";
    private static final String CONCENTRATION_FORMAT = "0.0E0";
    private static final String CONCENTRATION_UNITS = "M";
    private static final String K_FORMAT = "0.0E0";
    private static final String K_UNITS = "";
    
    private static final double X_MIN = TPConstants.TITRANT_VOLUME_RANGE.getMin();
    private static final double X_MAX = TPConstants.TITRANT_VOLUME_RANGE.getMax();
    private static final double X_DELTA = TPConstants.TITRANT_VOLUME_DELTA;
    
    private static final String MODEL_NAME_STRONG_BASE = "strong base";
    private static final String MODEL_NAME_WEAK_BASE = "weak base";
    private static final String MODEL_NAME_STRONG_ACID = "strong acid";
    private static final String MODEL_NAME_WEAK_ACID = "weak acid";
    private static final String MODEL_NAME_DIPROTIC_ACID = "diprotic acid";
    private static final String MODEL_NAME_TRIPROTIC_ACID = "triprotic acid";
    private static final Object[] CHOICES = {
        MODEL_NAME_STRONG_BASE,
        MODEL_NAME_WEAK_BASE, 
        MODEL_NAME_STRONG_ACID,
        MODEL_NAME_WEAK_ACID,
        MODEL_NAME_DIPROTIC_ACID,
        MODEL_NAME_TRIPROTIC_ACID
    };
    
    private static class ConcentrationControl extends LogarithmicValueControl {
        public ConcentrationControl( String label ) {
            super( TPConstants.CONCENTRATION_RANGE.getMin(), TPConstants.CONCENTRATION_RANGE.getMax(), label, CONCENTRATION_FORMAT, CONCENTRATION_UNITS );
            setValue( TPConstants.CONCENTRATION_RANGE.getMin() );
        }
    }
    
    private static class KControl extends LogarithmicValueControl {
        
        private ArrayList<ChangeListener> listeners;
        
        public KControl( String label ) {
            super( TPConstants.K_RANGE.getMin(), TPConstants.K_RANGE.getMax(), label, K_FORMAT, K_UNITS );
            setValue( TPConstants.K_RANGE.getMin() );
            listeners = new ArrayList<ChangeListener>();
        }
        
        /**
         * Changes the control's value without notifying listeners.
         * @param value
         */
        public void setValueNoNotify( double value ) {
            ArrayList<ChangeListener> listenersCopy = new ArrayList<ChangeListener>( listeners ); // avoid ConcurrentModificationException
            for ( ChangeListener listener : listenersCopy ) {
                removeChangeListener( listener );
            }
            setValue( value );
            for ( ChangeListener listener : listenersCopy ) {
                super.addChangeListener( listener );
            }
        }
        
        @Override
        public void addChangeListener( ChangeListener listener ) {
            super.addChangeListener( listener );
            listeners.add( listener );
        }
    }
    
    private final TPChart chart;
    private final JComboBox modelComboBox;
    private final ConcentrationControl solutionConcentrationControl, titrantConcentrationControl;
    private final KControl k1Control, k2Control, k3Control;
    
    public TPControlPanel( TPChart chart ) {
        super();
        setBorder( new LineBorder( Color.BLACK, 1 ) );
        
        this.chart = chart;
        
        // model choice
        modelComboBox = new JComboBox( CHOICES );
        modelComboBox.addItemListener(  new ItemListener() {
            public void itemStateChanged( ItemEvent e ) {
                if ( e.getStateChange() == ItemEvent.SELECTED ) {
                    update( e.getSource() );
                }
            }
        });
        
        // solution volume is constant
        JLabel solutionVolumeLabel = new JLabel( "solution: " + TPConstants.SOLUTION_VOLUME + " " + VOLUME_UNITS );
        solutionVolumeLabel.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        
        // solution concentration 
        solutionConcentrationControl = new ConcentrationControl( "solution:" );
        solutionConcentrationControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                update( e.getSource() );
            }
        });
        
        // titrant concentration
        titrantConcentrationControl = new ConcentrationControl( "titrant:" );
        titrantConcentrationControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                update( e.getSource() );
            }
        });
        
        // K1 disassociation constant
        k1Control = new KControl( "K1:" );
        k1Control.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                update( e.getSource() );
            }
        } );
        
        // K2 disassociation constant
        k2Control = new KControl( "K2:" );
        k2Control.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                update( e.getSource() );
            }
        } );
        
        // K3 disassociation constant
        k3Control = new KControl( "K3:" );
        k3Control.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                update( e.getSource() );
            }
        } );
        
        // layout
        EasyGridBagLayout layout = new EasyGridBagLayout( this );
        layout.setAnchor( GridBagConstraints.WEST );
        setLayout( layout );
        int row = 0;
        int column = 0;
        layout.addComponent( modelComboBox, row++, column );
        layout.addFilledComponent( new JSeparator(), row++, column, GridBagConstraints.HORIZONTAL );
        layout.addComponent( solutionVolumeLabel, row++, column );
        layout.addFilledComponent( new JSeparator(), row++, column, GridBagConstraints.HORIZONTAL );
        layout.addComponent( solutionConcentrationControl, row++, column );
        layout.addFilledComponent( new JSeparator(), row++, column, GridBagConstraints.HORIZONTAL );
        layout.addComponent( titrantConcentrationControl, row++, column );
        layout.addFilledComponent( new JSeparator(), row++, column, GridBagConstraints.HORIZONTAL );
        layout.addComponent( k1Control, row++, column );
        layout.addFilledComponent( new JSeparator(), row++, column, GridBagConstraints.HORIZONTAL );
        layout.addComponent( k2Control, row++, column );
        layout.addFilledComponent( new JSeparator(), row++, column, GridBagConstraints.HORIZONTAL );
        layout.addComponent( k3Control, row++, column );
        
        update( k1Control );
    }
    
    private void update( Object control ) {
        
        if ( control instanceof KControl ) {
            adjustKControls( (KControl)control );
        }
        
        Object choice = modelComboBox.getSelectedItem();
       
        if ( choice == MODEL_NAME_STRONG_BASE ) {
            updateStrongBase();
         }
        else if ( choice == MODEL_NAME_WEAK_BASE ) {
           updateWeakBase();
        }
        else if ( choice == MODEL_NAME_STRONG_ACID ) {
            updateStrongAcid();
         }
        else if ( choice == MODEL_NAME_WEAK_ACID ) {
            updateWeakAcid();
        }
        else if ( choice == MODEL_NAME_DIPROTIC_ACID ) {
            updateDiproticAcid();
        }
        else if ( choice == MODEL_NAME_TRIPROTIC_ACID ) {
            updateTriproticAcid();
        }
    }
    
    private void updateStrongBase() {
        // controls
        k1Control.setEnabled( false );
        k2Control.setEnabled( false );
        k3Control.setEnabled( false );
        // data points
        chart.clear();
        for ( double x = X_MIN; x <= X_MAX; x += X_DELTA ) {
            double Ca = titrantConcentrationControl.getValue();
            double Cb = solutionConcentrationControl.getValue();
            double Va = x;
            double Vb = TPConstants.SOLUTION_VOLUME;
            double y = TPModel.strongBase( Ca, Cb, Va, Vb );
            chart.addPoint( x, y);
        }
    }
    
    private void updateWeakBase() {
        // controls
        k1Control.setEnabled( true );
        k2Control.setEnabled( false );
        k3Control.setEnabled( false );
        // data points
        chart.clear();
        for ( double x = X_MIN; x <= X_MAX; x += X_DELTA ) {
            double Ca = titrantConcentrationControl.getValue();
            double Cb = solutionConcentrationControl.getValue();
            double Va = x;
            double Vb = TPConstants.SOLUTION_VOLUME;
            double Ka = k1Control.getValue();
            double y = TPModel.weakBase( Ca, Cb, Va, Vb, Ka );
            chart.addPoint( x, y );
        }
    }
    
    private void updateStrongAcid() {
        // controls
        k1Control.setEnabled( false );
        k2Control.setEnabled( false );
        k3Control.setEnabled( false );
        // data points
        chart.clear();
        for ( double x = X_MIN; x <= X_MAX; x += X_DELTA ) {
            double Ca = solutionConcentrationControl.getValue();
            double Cb = titrantConcentrationControl.getValue();
            double Va = TPConstants.SOLUTION_VOLUME;
            double Vb = x;
            double y = TPModel.strongAcid( Ca, Cb, Va, Vb );
            chart.addPoint( y, x );
        }
    }
    
    private void updateWeakAcid() {
        // controls
        k1Control.setEnabled( true );
        k2Control.setEnabled( false );
        k3Control.setEnabled( false );
        // data points
        chart.clear();
        for ( double x = X_MIN; x <= X_MAX; x += X_DELTA ) {
            double Ca = solutionConcentrationControl.getValue();
            double Cb = titrantConcentrationControl.getValue();
            double Va = TPConstants.SOLUTION_VOLUME;
            double Vb = x;
            double Ka = k1Control.getValue();
            double y = TPModel.weakAcid( Ca, Cb, Va, Vb, Ka );
            chart.addPoint( x, y );
        }
    }
    
    private void updateDiproticAcid() {
        // controls
        k1Control.setEnabled( true );
        k2Control.setEnabled( true );
        k3Control.setEnabled( false );
        // data points
        chart.clear();
        for ( double x = X_MIN; x <= X_MAX; x += X_DELTA ) {
            double Ca = solutionConcentrationControl.getValue();
            double Cb = titrantConcentrationControl.getValue();
            double Va = TPConstants.SOLUTION_VOLUME;
            double Vb = x;
            double Ka1 = k1Control.getValue();
            double Ka2 = k2Control.getValue();
            double y = TPModel.diproticAcid( Ca, Cb, Va, Vb, Ka1, Ka2 );
            chart.addPoint( y, x );
        }
    }
    
    private void updateTriproticAcid() {
        // controls
        k1Control.setEnabled( true );
        k2Control.setEnabled( true );
        k3Control.setEnabled( true );
        // data points
        chart.clear();
        for ( double x = X_MIN; x <= X_MAX; x += X_DELTA ) {
            double Ca = solutionConcentrationControl.getValue();
            double Cb = titrantConcentrationControl.getValue();
            double Va = TPConstants.SOLUTION_VOLUME;
            double Vb = x;
            double Ka1 = k1Control.getValue();
            double Ka2 = k2Control.getValue();
            double Ka3 = k3Control.getValue();
            double y = TPModel.triproticAcid( Ca, Cb, Va, Vb, Ka1, Ka2, Ka3 );
            chart.addPoint( y, x );
        }
    }
    
    /*
     * Each slider can be dragged throughout its full range.
     * If the condition K3 <= K2 <= K1 is violated, then the
     * sliders that are not being dragged are adjusted.
     */
    private void adjustKControls( KControl controlChanged ) {
        if ( controlChanged == k1Control ){
            // adjust K2
            if ( k1Control.getValue() < k2Control.getValue() ) {
                k2Control.setValueNoNotify( k1Control.getValue() );
            }
            // adjust K3
            if ( k2Control.getValue() < k3Control.getValue() ) {
                k3Control.setValueNoNotify( k2Control.getValue() );
            }
        }
        else if ( controlChanged == k2Control ) {
            // adjust K1
            if ( k2Control.getValue() > k1Control.getValue() ) {
                k1Control.setValueNoNotify( k2Control.getValue() );
            }
            // adjust K3
            if ( k2Control.getValue() < k3Control.getValue() ) {
                k3Control.setValueNoNotify( k2Control.getValue() );
            }
        }
        else if ( controlChanged == k3Control ) {
            // adjust K2
            if ( k3Control.getValue() > k2Control.getValue()  ) {
                k2Control.setValueNoNotify( k3Control.getValue() );
               
            }
            // adjust K1
            if ( k2Control.getValue() > k1Control.getValue() ) {
                k1Control.setValueNoNotify( k2Control.getValue() );
            }
        }
    }
}
