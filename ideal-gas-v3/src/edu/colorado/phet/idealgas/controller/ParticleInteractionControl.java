/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.idealgas.controller;

import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.idealgas.model.GasMolecule;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controls whether molecules collide with or simply ignore each other.
 */
public class ParticleInteractionControl extends JPanel {
    public ParticleInteractionControl() {
        final JCheckBox sphereSphereCollisionCB = new JCheckBox( SimStrings.get( "Molecules-interact" ) );
        this.add( sphereSphereCollisionCB );
        sphereSphereCollisionCB.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                GasMolecule.enableParticleParticleInteractions( sphereSphereCollisionCB.isSelected() );
            }
        } );

        // Set default state
        sphereSphereCollisionCB.setSelected( true );
        GasMolecule.enableParticleParticleInteractions( sphereSphereCollisionCB.isSelected() );
    }
}
