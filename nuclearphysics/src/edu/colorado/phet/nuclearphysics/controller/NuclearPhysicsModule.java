/**
 * Class: NuclearPhysicsModule
 * Class: edu.colorado.phet.nuclearphysics.controller
 * User: Ron LeMaster
 * Date: Feb 28, 2004
 * Time: 12:07:12 PM
 */
package edu.colorado.phet.nuclearphysics.controller;

import edu.colorado.phet.common.application.PhetGraphicsModule;
import edu.colorado.phet.common.model.ModelElement;
import edu.colorado.phet.common.model.clock.IClock;
import edu.colorado.phet.common.model.clock.ClockEvent;
import edu.colorado.phet.common.model.clock.ClockListener;
import edu.colorado.phet.common.view.ApparatusPanel;
import edu.colorado.phet.common.view.ControlPanel;
import edu.colorado.phet.common.view.ApparatusPanel2;
import edu.colorado.phet.nuclearphysics.model.*;
import edu.colorado.phet.nuclearphysics.view.NeutronGraphic;
import edu.colorado.phet.nuclearphysics.view.PhysicalPanel;
import edu.colorado.phet.nuclearphysics.view.NuclearPhysicsApparatusPanel;

import javax.swing.*;
import java.awt.*;

public class NuclearPhysicsModule extends PhetGraphicsModule {
    private ApparatusPanel apparatusPanel;
    private PhysicalPanel physicalPanel;
    private IClock clock;
    private NuclearPhysicsControlPanel nuclearPysicslControlPanel;

    public NuclearPhysicsModule( String name, IClock clock ) {
        super( name, clock );
        this.clock = clock;

        // Start the model
        this.setModel( new NuclearPhysicsModel() );
        this.getModel().addModelElement( new ModelElement() {
            public void stepInTime( double dt ) {
                // todo: get rid of this, and make the graphics self-painting
                apparatusPanel.repaint();
            }
        } );

        apparatusPanel = new ApparatusPanel();
        apparatusPanel = new NuclearPhysicsApparatusPanel( clock );
        super.setApparatusPanel( apparatusPanel );

        physicalPanel = new PhysicalPanel( clock );
        apparatusPanel.setLayout( new GridLayout( 1, 1 ) );
        setPhysicalPanel( physicalPanel );
        apparatusPanel.add( physicalPanel );

        ControlPanel controlPanel = new ControlPanel();
        setControlPanel( controlPanel );
        nuclearPysicslControlPanel = new NuclearPhysicsControlPanel( this );
        getControlPanel().addControl( nuclearPysicslControlPanel );
    }


    public IClock getClock() {
        return clock;
    }

    public void setPhysicalPanel( PhysicalPanel physicalPanel ) {
        apparatusPanel.remove( physicalPanel );
        this.physicalPanel = physicalPanel;
        apparatusPanel.add( physicalPanel );
    }

    protected void addControlPanelElement( JPanel panel ) {
        nuclearPysicslControlPanel.addPanelElement( panel );
//        ( (NuclearPhysicsControlPanel)getControlPanel() ).addPanelElement( panel );
    }

    protected void addNucleus( Nucleus nucleus ) {
        this.getModel().addModelElement( nucleus );
        physicalPanel.addNucleus( nucleus );
    }

    protected void addNeutron( final NuclearParticle particle ) {
        this.getModel().addModelElement( particle );
        final NeutronGraphic ng = new NeutronGraphic( particle );
        physicalPanel.addGraphic( ng );

        particle.addListener( new NuclearModelElement.Listener() {
            public void leavingSystem( NuclearModelElement nme ) {
                physicalPanel.removeGraphic( ng );
                particle.removeListener( this );
            }
        } );
    }

    protected void addNeutron( final NuclearParticle particle, Nucleus nucleus ) {
        this.getModel().addModelElement( particle );
        final NeutronGraphic ng = new NeutronGraphic( particle );
        physicalPanel.addGraphic( ng );

        particle.addListener( new NuclearModelElement.Listener() {
            public void leavingSystem( NuclearModelElement nme ) {
                physicalPanel.removeGraphic( ng );
                particle.removeListener( this );
            }
        } );
    }

    protected void setUraniumNucleus( Uranium235 uraniumNucleus ) {
        addNucleus( uraniumNucleus );
    }

    protected PhysicalPanel getPhysicalPanel() {
        return physicalPanel;
    }



}
