/*, 2003.  Editing.*/
package edu.colorado.phet.common.conductivity.view;

import edu.colorado.phet.common.conductivity.application.Module;
import edu.colorado.phet.common.conductivity.application.ModuleManager;
import edu.colorado.phet.common.conductivity.application.ModuleObserver;
import edu.colorado.phet.common.conductivity.application.PhetApplication;
import edu.colorado.phet.common.conductivity.view.apparatuspanelcontainment.ApparatusPanelContainer;

import java.io.IOException;

/**
 * User: Sam Reid
 * Date: Jun 12, 2003
 * Time: 7:27:29 AM
 *
 */
public class ApplicationView {
    private PhetFrame phetFrame;
    private ApparatusPanelContainer appPnlContainer;
//    private ApplicationModelControlPanel controlPanel;

    private BasicPhetPanel basicPhetPanel;

    public ApplicationView( PhetApplication application ) throws IOException {
        appPnlContainer = application.getContainerStrategy().createApparatusPanelContainer( application.getModuleManager() );

        if( application.getClock() == null ) {
            throw new RuntimeException( "Clock is null" );
        }
//        controlPanel = new ApplicationModelControlPanel( application.getClock() );
        basicPhetPanel = new BasicPhetPanel( null, null, null, null);
        basicPhetPanel.setApparatusPanelContainer( appPnlContainer.getComponent() );
        new ControlAndMonitorSwapper( basicPhetPanel, application.getModuleManager() );
        phetFrame = new PhetFrame( application );
        phetFrame.setContentPane( basicPhetPanel );
    }

    public BasicPhetPanel getBasicPhetPanel() {
        return basicPhetPanel;
    }

    public void setVisible( boolean isVisible ) {
        phetFrame.setVisible( isVisible );
    }

    public ApparatusPanelContainer getApparatusPanelContainer() {
        return appPnlContainer;
    }

    private class ControlAndMonitorSwapper implements ModuleObserver {
        BasicPhetPanel bpp;

        public ControlAndMonitorSwapper( BasicPhetPanel bpp, ModuleManager mm ) {
            this.bpp = bpp;
            mm.addModuleObserver( this );
        }

        public void moduleAdded( Module m ) {
        }

        public void activeModuleChanged( Module m ) {
            bpp.setControlPanel( m.getControlPanel() );
//            bpp.setMonitorPanel( m.getMonitorPanel() );
        }
    }
}
