/**
 * Class: HelpItem
 * Package: edu.colorado.phet.common.view.help
 * Author: Another Guy
 * Date: May 24, 2004
 */
package edu.colorado.phet.common.view.help;

import edu.colorado.phet.common.view.graphics.Graphic;
import edu.colorado.phet.common.view.graphics.ShadowTextGraphic;
import edu.colorado.phet.common.view.graphics.ShapeGraphic;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class HelpItem implements Graphic {
    public final static int TOP = 1;
    public final static int BOTTOM = 2;
    public final static int LEFT = 3;
    public final static int CENTER = 4;
    public final static int RIGHT = 5;

    ShapeGraphic backgroundGraphic;
    ArrayList shadowTextGraphics = new ArrayList();
    int horizontalAlignment;
    int verticalAlignment;
    private Font font = new Font( "Lucida Sans", Font.BOLD, 16 );
    private String text;
    private Point2D.Double location;
    private Color shadowColor;
    private Color foregroundColor;
    boolean inited = false;

    public HelpItem( String text, double x, double y ) {
        this( text, x, y, CENTER, CENTER );
    }

    public HelpItem( String text, double x, double y,
                     int horizontalAlignment, int verticalAlignment ) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.text = text;
        this.location = new Point2D.Double( x, y );
        shadowColor = Color.black;
        foregroundColor = new Color( 156, 156, 0 );
    }

    private String[] tokenizeString( String inputText ) {
        StringTokenizer st = new StringTokenizer( inputText, "\n" );
        ArrayList list = new ArrayList();
        while( st.hasMoreTokens() ) {
            String next = st.nextToken();
            list.add( next );
        }
        return (String[])list.toArray( new String[0] );
    }

    public void paint( Graphics2D g ) {
        if( !inited ) {
            init( g );
            inited = true;
        }
        if( backgroundGraphic != null ) {
            backgroundGraphic.paint( g );
        }
        for( int i = 0; i < shadowTextGraphics.size(); i++ ) {
            ShadowTextGraphic textGraphic = (ShadowTextGraphic)shadowTextGraphics.get( i );
            textGraphic.paint( g );
        }
    }

    public void setShadowColor( Color shadowColor ) {
        this.shadowColor = shadowColor;
        for( int i = 0; i < shadowTextGraphics.size(); i++ ) {
            ShadowTextGraphic textGraphic = (ShadowTextGraphic)shadowTextGraphics.get( i );
            textGraphic.setShadowColor( shadowColor );
        }
    }

    public void setForegroundColor( Color foregroundColor ) {
        this.foregroundColor = foregroundColor;
        for( int i = 0; i < shadowTextGraphics.size(); i++ ) {
            ShadowTextGraphic shadowTextGraphic = (ShadowTextGraphic)shadowTextGraphics.get( i );
            shadowTextGraphic.setForegroundColor( foregroundColor );
        }
    }

    private void init( Graphics2D g ) {
        FontMetrics fontMetrics = g.getFontMetrics( font );
        shadowTextGraphics = new ArrayList();
        String[] sa = tokenizeString( text );
        int x = (int)( location.getX() + fontMetrics.getStringBounds( " ", g ).getWidth() );
        for( int i = 0; i < sa.length; i++ ) {
            int y = (int)location.getY() + ( i + 1 ) * ( fontMetrics.getHeight() + fontMetrics.getLeading() );
            ShadowTextGraphic textGraphic = new ShadowTextGraphic( font, sa[i], 1, 1, x, y, foregroundColor, shadowColor );
            shadowTextGraphics.add( textGraphic );
        }
    }

}