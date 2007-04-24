/*, 2003.*/
package edu.colorado.phet.distanceladder.common.view;

import edu.colorado.phet.distanceladder.common.view.graphics.Graphic;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Alternative to a large many to many composite interactive graphic implementation.
 */
public class RevertibleGraphic implements Graphic {
    private Graphic graphic;
    private AffineTransform atx;
    private RevertableGraphicsSetup graphicSetup;

    public RevertibleGraphic( Graphic graphic, AffineTransform atx, RevertableGraphicsSetup graphicSetup ) {
        this.graphic = graphic;
        this.atx = atx;
        this.graphicSetup = graphicSetup;
    }

    public void paint( Graphics2D g ) {
        g.setTransform( atx );
        graphicSetup.setup( g );
        graphic.paint( g );
        graphicSetup.revert( g );
    }
}
