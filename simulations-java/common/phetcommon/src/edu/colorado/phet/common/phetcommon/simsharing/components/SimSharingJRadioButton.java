// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.common.phetcommon.simsharing.components;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JRadioButton;

import edu.colorado.phet.common.phetcommon.simsharing.Parameter;
import edu.colorado.phet.common.phetcommon.simsharing.SimSharingActions;
import edu.colorado.phet.common.phetcommon.simsharing.SimSharingEvents;
import edu.colorado.phet.common.phetcommon.simsharing.SimSharingObjects;
import edu.colorado.phet.common.phetcommon.simsharing.SimSharingParameters;

/**
 * Swing radio button that sends sim-sharing events.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class SimSharingJRadioButton extends JRadioButton {

    public SimSharingJRadioButton() {
    }

    public SimSharingJRadioButton( Icon icon ) {
        super( icon );
    }

    public SimSharingJRadioButton( Action a ) {
        super( a );
    }

    public SimSharingJRadioButton( Icon icon, boolean selected ) {
        super( icon, selected );
    }

    public SimSharingJRadioButton( String text ) {
        super( text );
    }

    public SimSharingJRadioButton( String text, boolean selected ) {
        super( text, selected );
    }

    public SimSharingJRadioButton( String text, Icon icon ) {
        super( text, icon );
    }

    public SimSharingJRadioButton( String text, Icon icon, boolean selected ) {
        super( text, selected );
    }

    @Override protected void fireActionPerformed( ActionEvent event ) {
        SimSharingEvents.sendEvent( SimSharingObjects.RADIO_BUTTON,
                                    SimSharingActions.PRESSED,
                                    Parameter.param( SimSharingParameters.TEXT, getText() ) );
        super.fireActionPerformed( event );
    }
}
