/* Copyright 2007, University of Colorado */
package edu.colorado.phet.energyskatepark.view.bargraphs;

import edu.colorado.phet.common.phetcommon.math.ModelViewTransform1D;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

import java.awt.*;

/**
 * User: Sam Reid
 * Date: Feb 12, 2005
 * Time: 9:32:14 AM
 *
 */

public class BarGraphic2D extends PNode {
    private ModelViewTransform1D transform1D;
    private double value;
    private int x;
    private int width;
    private int y;
    private VerticalTextGraphic label;
    private PPath rectanglePath;
    public double labelHeight;
    private double labelWidth;

    public BarGraphic2D( String text, ModelViewTransform1D transform1D, double value, int x, int width, int y, int dx, int dy, Color color, Font barFont ) {
        this.transform1D = transform1D;
        this.value = value;
        this.x = x;
        this.y = y;
        this.width = width;

        rectanglePath = new PPath( null );
        rectanglePath.setPaint( color );
        rectanglePath.setStroke( new BasicStroke( 1 ) );
        rectanglePath.setStrokePaint( Color.black );

//        Color textColor = new Color( 240, 225, 255 );
//        label = new VerticalTextGraphic( barFont, text, Color.black, textColor );
        label = new VerticalTextGraphic( barFont, text, color, Color.black );
        addChild( rectanglePath );

        addChild( label );
//        label.setOffset( label.getOffset().getX(),label.getHeight()+5);
        labelHeight = label.getHeight();
        labelWidth = label.getWidth();
        updateBar();
    }

    private void updateBar() {
        int height = computeHeight();
        Rectangle rect = new Rectangle( x, y - height, width, height );
//        label.setOffset( rect.x + 7 - labelWidth, (int)( 5 + y + labelHeight ) );
//        label.setOffset( rect.x + 2 - labelWidth, (int)( 5 + y + labelHeight ) );
        label.setOffset( rect.x + 2 - labelWidth, (int)( y + label.getFullBounds().getHeight() + 14 ) );
//        label.setOffset( rect.x + 7 - labelWidth, (int)( y + labelHeight ) );
        rectanglePath.setPathTo( rect );
    }

    public void setValue( double value ) {
//        if( value != this.value && Math.abs( value ) != Math.abs( this.value ) ) {
        if( value != this.value ) {
            this.value = value;
            if( value < 0 ) {
                rectanglePath.setOffset( 0, -computeHeight() );//a big hack to make negative values work.
            }
            else {
                rectanglePath.setOffset( 0, 0 );
            }
            this.value = Math.abs( value );
            updateBar();
        }
    }

    private int computeHeight() {
        return transform1D.modelToView( value );
    }

    public void setBarHeight( double baselineY ) {
        this.y = (int)baselineY;
        updateBar();
    }
}
