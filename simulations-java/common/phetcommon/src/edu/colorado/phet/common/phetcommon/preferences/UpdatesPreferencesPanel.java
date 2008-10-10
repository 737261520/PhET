package edu.colorado.phet.common.phetcommon.preferences;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.colorado.phet.common.phetcommon.resources.PhetCommonResources;

public class UpdatesPreferencesPanel extends JPanel {

    final JCheckBox updatesEnabledCheckBox;
    
    public UpdatesPreferencesPanel( final IManualUpdateChecker iCheckForUpdates, boolean updatesEnabled ) {
        setLayout( new GridBagLayout() );
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridx = 0;
        constraints.gridwidth = 1;

        updatesEnabledCheckBox = new JCheckBox( "Automatically check for updates", updatesEnabled );
        add( Box.createRigidArea( new Dimension( 50, 20 ) ), constraints );
        add( updatesEnabledCheckBox, constraints );
        add( Box.createRigidArea( new Dimension( 50, 10 ) ), constraints );
        JButton checkForUpdatesButton = new JButton( PhetCommonResources.getString( "Common.HelpMenu.CheckForUpdates" ) );
        checkForUpdatesButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                iCheckForUpdates.checkForUpdates();
            }
        } );
        add( checkForUpdatesButton, constraints );
    }
    
    public boolean isUpdatesEnabled() {
        return updatesEnabledCheckBox.isSelected();
    }
}
