/* Copyright 2007, University of Colorado */
package edu.colorado.phet.energyskatepark.view.bargraphs;

import edu.colorado.phet.common.piccolophet.nodes.ShadowHTMLGraphic;
import edu.umd.cs.piccolo.PNode;

import java.awt.*;

/**
 * User: Sam Reid
 * Date: Feb 12, 2005
 * Time: 9:47:54 AM
 *
 */

public class VerticalTextGraphic extends PNode {
    private Font font;
    private String text;

    public VerticalTextGraphic( Font font, String text, Color color, Color outline ) {
        this.font = font;
        this.text = text;
        ShadowHTMLGraphic phetTextGraphic = new ShadowHTMLGraphic( text );//, font, color, 1, 1, outline );
        phetTextGraphic.setColor( color );
        phetTextGraphic.setShadowColor( outline );
        phetTextGraphic.setFont( font );

        double h = phetTextGraphic.getFullBounds().getHeight();
//        System.out.println( "h = " + h );
        phetTextGraphic.translate( -3, -10 );
        phetTextGraphic.rotate( -Math.PI / 2 );

        addChild( phetTextGraphic );
    }

    public String getText() {
        return text;
    }
}
