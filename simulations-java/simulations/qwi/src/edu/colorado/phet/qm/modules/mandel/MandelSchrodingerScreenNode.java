/*  */
package edu.colorado.phet.qm.modules.mandel;

import edu.colorado.phet.qm.davissongermer.QWIStrings;
import edu.colorado.phet.qm.view.QWIPanel;
import edu.colorado.phet.qm.view.piccolo.QWIScreenNode;

/**
 * User: Sam Reid
 * Date: Mar 2, 2006
 * Time: 11:13:19 PM
 *
 */

public class MandelSchrodingerScreenNode extends QWIScreenNode {
    private MandelModule mandelModule;

    public MandelSchrodingerScreenNode( MandelModule module, QWIPanel QWIPanel ) {
        super( module, QWIPanel );
        getDetectorSheetPNode().setTitle( QWIStrings.getString( "black.white.screen" ) );
        this.mandelModule = module;
    }

    protected double getWavefunctionGraphicX( double availableWidth ) {
//        double x = mandelModule.getMandelSchrodingerPanel().getMandelGunSet().getLeftGun().getGunControlPanelPSwing().getFullBounds().getWidth();
//        return Math.max( x, 150 );
        return 168;
    }

//    protected double getLayoutMinX() {
//        double sx = super.getLayoutMinX();
//        double ax = mandelModule.getMandelSchrodingerPanel().getMandelGunSet().getLeftGun().getGunControlPanelPSwing().getFullBounds().getX();
//        System.out.println( "sx = " + sx +", ax="+ax);
//        return Math.min(sx,ax );
//    }
}
