/* Copyright 2006, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.hydrogenatom.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import edu.colorado.phet.common.view.VerticalLayoutPanel;
import edu.colorado.phet.common.view.util.EasyGridBagLayout;
import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.common.view.util.SwingUtils;
import edu.colorado.phet.hydrogenatom.model.BohrModel;

/**
 * TransitionsDialog shows the wavelengths that cause state transitions.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class TransitionsDialog extends JDialog {

    private static final String TRANSITION_FORMAT = "{0} <-> {1}";
    private static final DecimalFormat WAVELENGTH_FORMATTER = new DecimalFormat( "0" );
    
    public TransitionsDialog( Frame owner ) {
        super( owner, SimStrings.getInstance().getString( "dialog.transitions.title") );
        setResizable( false );
        
        JPanel inputPanel = createInputPanel();
        JPanel actionsPanel = createActionsPanel();
        
        VerticalLayoutPanel panel = new VerticalLayoutPanel();
        panel.setFillHorizontal();
        panel.add( inputPanel );
        panel.add( new JSeparator() );
        panel.add( actionsPanel );
        
        setContentPane( panel );
        pack();
        SwingUtils.centerDialogInParent( this );
    }
    
    private JPanel createInputPanel() {
        
        JPanel panel = new JPanel();
        panel.setBorder( new EmptyBorder( 10, 20, 10, 20 ) );
        EasyGridBagLayout layout = new EasyGridBagLayout( panel );
        layout.setInsets( new Insets( 0, 0, 0, 0 ) );
        layout.setMinimumWidth( 1, 15 );
        layout.setMinimumWidth( 3, 3 );
        panel.setLayout( layout );
        int row = 0;
        
        layout.addAnchoredComponent( new JLabel( SimStrings.getInstance().getString( "dialog.transitions.transition" ) ), row, 0, GridBagConstraints.CENTER );
        layout.addAnchoredComponent( new JLabel( SimStrings.getInstance().getString( "dialog.transitions.wavelength" ) ), row, 2, GridBagConstraints.EAST );
        layout.addAnchoredComponent( new JLabel( "(nm)" ), row, 4, GridBagConstraints.WEST );
        row++;
        layout.addComponent( new JSeparator(), row, 0, 5, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL );
        row++;

        final int groundState = BohrModel.getGroundState();
        final int numberOfStates = BohrModel.getNumberOfStates();
        final int maxState = groundState + numberOfStates - 1;
        for ( int m = groundState; m < maxState; m++ ) {
            for ( int n = m + 1; n <= maxState; n++ ) {
                Object[] args = { new Integer( m ), new Integer( n ) };
                String transition = MessageFormat.format( TRANSITION_FORMAT, args );
                String wavelength = WAVELENGTH_FORMATTER.format( BohrModel.getWavelengthAbsorbed( m, n ) );
                layout.addAnchoredComponent( new JLabel( transition ), row, 0, GridBagConstraints.CENTER );
                layout.addAnchoredComponent( new JLabel( wavelength ), row, 2, GridBagConstraints.EAST );
                row++;
            }
        }
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JButton closeButton = new JButton( SimStrings.getInstance().getString( "button.close" ) );
        closeButton.addActionListener( new ActionListener() {

            public void actionPerformed( ActionEvent event ) {
                dispose();
            }
        } );

        JPanel innerPanel = new JPanel( new GridLayout( 1, 1, 0, 0 ) );
        innerPanel.add( closeButton );

        JPanel actionPanel = new JPanel( new FlowLayout() );
        actionPanel.add( innerPanel );

        return actionPanel;
    }
}
