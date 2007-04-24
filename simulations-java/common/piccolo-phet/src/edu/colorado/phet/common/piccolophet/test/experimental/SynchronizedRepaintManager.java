/*  */
package edu.colorado.phet.common.piccolophet.test.experimental;

import javax.swing.*;

/**
 * User: Sam Reid
 * Date: Aug 7, 2005
 * Time: 9:08:18 PM
 * 
 */

public class SynchronizedRepaintManager extends RepaintManager {
    private boolean paintOn = false;

    public void paintDirtyRegions() {
        if( paintOn ) {
            super.paintDirtyRegions();
        }
    }

    public void synchronizedPaint() {
        paintOn = true;
        paintDirtyRegions();
        paintOn = false;
    }
}
