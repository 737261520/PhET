/** Sam Reid*/
package edu.colorado.phet.cck3;

import edu.colorado.phet.cck3.circuit.tools.Voltmeter;
import edu.colorado.phet.cck3.circuit.tools.VoltmeterGraphic;
import edu.colorado.phet.common.view.CompositeGraphic;
import edu.colorado.phet.common.view.graphics.DefaultInteractiveGraphic;
import edu.colorado.phet.common.view.graphics.mousecontrols.Translatable;
import edu.colorado.phet.common.view.graphics.transforms.ModelViewTransform2D;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * User: Sam Reid
 * Date: Jun 17, 2004
 * Time: 2:27:28 PM
 * Copyright (c) Jun 17, 2004 by Sam Reid
 */
public class InteractiveVoltmeter extends CompositeGraphic {
    private Voltmeter voltmeter;
    private ModelViewTransform2D transform;
    private VoltmeterGraphic voltmeterGraphic;
    private CCK3Module module;
    private boolean dragAll;

    public InteractiveVoltmeter( final VoltmeterGraphic voltmeterGraphic, final CCK3Module module ) {
        super.setVisible( false );
        this.module = module;
        this.voltmeterGraphic = voltmeterGraphic;
        voltmeter = voltmeterGraphic.getVoltmeter();
        this.transform = module.getTransform();

        DefaultInteractiveGraphic unitInteraction = new DefaultInteractiveGraphic( voltmeterGraphic.getUnitGraphic() );
        unitInteraction.addCursorHandBehavior();
        unitInteraction.addMouseInputListener( new MouseInputAdapter() {
            // implements java.awt.event.MouseListener
            public void mousePressed( MouseEvent e ) {
                //if the leads are on the circuit, only the DVM goes.
//                Branch a = voltmeterGraphic.getRedLeadGraphic().detectBranch( module.getCircuitGraphic() );
//                Branch b = voltmeterGraphic.getBlackLeadGraphic().detectBranch( module.getCircuitGraphic() );
                VoltmeterGraphic.Connection a = voltmeterGraphic.getRedLeadGraphic().detectConnection( module.getCircuitGraphic() );
                VoltmeterGraphic.Connection b = voltmeterGraphic.getBlackLeadGraphic().detectConnection( module.getCircuitGraphic() );
                if( a == null && b == null ) {
                    dragAll = true;
                }
                else {
                    dragAll = false;
                }
            }
        } );
        unitInteraction.addTranslationBehavior( new Translatable() {
            public void translate( double dx, double dy ) {
                Point2D.Double pt = InteractiveVoltmeter.this.transform.viewToModelDifferential( (int)dx, (int)dy );
                if( dragAll ) {
                    InteractiveVoltmeter.this.voltmeter.translateAll( pt.getX(), pt.getY() );
                }
                else {
                    InteractiveVoltmeter.this.voltmeter.translate( pt.getX(), pt.getY() );
                }
            }
        } );
        final JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem( "Vertical Leads" );
        jcbmi.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                setLeadsVertical( jcbmi.isSelected() );
            }
        } );
        JPopupMenu jpm = new JPopupMenu();
        jpm.add( jcbmi );
        unitInteraction.addPopupMenuBehavior( jpm );
        addGraphic( unitInteraction );

        addGraphic( voltmeterGraphic.getRedCableGraphic() );
        addGraphic( voltmeterGraphic.getBlackCableGraphic() );

        DefaultInteractiveGraphic redLead = new DefaultInteractiveGraphic( voltmeterGraphic.getRedLeadGraphic() );
        redLead.addCursorHandBehavior();
        redLead.addTranslationBehavior( new Translatable() {
            public void translate( double dx, double dy ) {
                Point2D.Double pt = InteractiveVoltmeter.this.transform.viewToModelDifferential( (int)dx, (int)dy );
                InteractiveVoltmeter.this.voltmeter.getRedLead().translate( pt.getX(), pt.getY() );
            }
        } );
        addGraphic( redLead );

        DefaultInteractiveGraphic blackLead = new DefaultInteractiveGraphic( voltmeterGraphic.getBlackLeadGraphic() );
        blackLead.addCursorHandBehavior();
        blackLead.addTranslationBehavior( new Translatable() {
            public void translate( double dx, double dy ) {
                Point2D.Double pt = InteractiveVoltmeter.this.transform.viewToModelDifferential( (int)dx, (int)dy );
                InteractiveVoltmeter.this.voltmeter.getBlackLead().translate( pt.getX(), pt.getY() );
            }
        } );
        addGraphic( blackLead );
    }

    private void setLeadsVertical( boolean vertical ) {
        double redangle = 0;
        double blackangle = 0;
        if( !vertical ) {
            redangle = Math.PI / 8;
            blackangle = -redangle;
        }

        voltmeterGraphic.getBlackLeadGraphic().setAngle( blackangle );
        voltmeterGraphic.getRedLeadGraphic().setAngle( redangle );
        voltmeterGraphic.getBlackCableGraphic().changed();
        voltmeterGraphic.getRedCableGraphic().changed();
        module.getApparatusPanel().repaint();
    }

}
