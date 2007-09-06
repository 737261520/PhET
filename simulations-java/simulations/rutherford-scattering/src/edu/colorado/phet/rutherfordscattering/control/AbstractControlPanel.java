/* Copyright 2007, University of Colorado */

package edu.colorado.phet.rutherfordscattering.control;

import edu.colorado.phet.common.phetcommon.application.NonPiccoloPhetApplication;
import edu.colorado.phet.common.phetcommon.util.DialogUtils;
import edu.colorado.phet.common.phetcommon.view.ControlPanel;
import edu.colorado.phet.rutherfordscattering.RSResources;
import edu.colorado.phet.rutherfordscattering.module.AbstractModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * AbstractControlPanel is the base class for all control panels.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public abstract class AbstractControlPanel extends ControlPanel {

    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private AbstractModule _module; // module that this control panel is associated with
    private JButton _resetButton;

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    /**
     * Sole constructor.
     *
     * @param module
     */
    public AbstractControlPanel( AbstractModule module ) {
        super();
        int minWidth = RSResources.getInt( "int.minControlPanelWidth", 200 );
        setMinimumWidth( minWidth );
        setInsets( new Insets( 0, 3, 0, 3 ) );
        _module = module;
    }

    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------

    /**
     * Gets the module that this control panel is associated with.
     *
     * @return the module
     */
    public AbstractModule getModule() {
        return _module;
    }

    //----------------------------------------------------------------------------
    // Add things to the control panel
    //----------------------------------------------------------------------------

    /**
     * Gets the reset button, usually for attaching a help item to it.
     *
     * @return the reset button, null if addResetButton has not been called
     */
    public JButton getResetButton() {
        return _resetButton;
    }

    /**
     * Adds a Reset button to the control panel.
     * The button handler calls the module's reset method.
     */
    public void addResetButton( Font font ) {
        _resetButton = new JButton( RSResources.getString( "string.resetAll" ) );
        if ( font != null ) {
            _resetButton.setFont( font );
        }
        _resetButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                Frame frame = NonPiccoloPhetApplication.instance().getPhetFrame();
                String message = RSResources.getString( "string.confirmResetAll" );
                int option = DialogUtils.showConfirmDialog( frame, message, JOptionPane.YES_NO_OPTION );
                if ( option == JOptionPane.YES_OPTION ) {
                    _module.reset();
                }
            }
        } );
        addControl( _resetButton );
    }

    public void addResetButton() {
        addResetButton( null /* font */ );
    }

    /**
     * Sets the minumum width of the control panel.
     *
     * @param minimumWidth
     */
    public void setMinimumWidth( int minimumWidth ) {
        JPanel fillerPanel = new JPanel();
        fillerPanel.setLayout( new BoxLayout( fillerPanel, BoxLayout.X_AXIS ) );
        fillerPanel.add( Box.createHorizontalStrut( minimumWidth ) );
        addControlFullWidth( fillerPanel );
    }
}
