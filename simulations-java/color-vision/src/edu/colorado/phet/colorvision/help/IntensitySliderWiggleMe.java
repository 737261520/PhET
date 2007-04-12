/* Copyright 2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.colorvision.help;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import edu.colorado.phet.colorvision.phetcommon.model.BaseModel;
import edu.colorado.phet.colorvision.phetcommon.view.graphics.shapes.Arrow;
import edu.colorado.phet.colorvision.phetcommon.view.phetgraphics.PhetImageGraphic;
import edu.colorado.phet.colorvision.phetcommon.view.util.SimStrings;

/**
 * IntensitySliderWiggleMe is a help graphic that tells the user to move an 
 * intensity slider. This class sets up the "look" of the graphic, while the
 * superclass handles the animation behavior.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class IntensitySliderWiggleMe extends WiggleMe {

    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------

    private static final Dimension DEFAULT_WIGGLE_SIZE = new Dimension( 30, 30 );
    private static final int DEFAULT_CYCLE_LENGTH = 20;
    private static final Font DEFAULT_FONT = new Font( "SansSerif", Font.PLAIN, 18 );
    private static final Color DEFAULT_COLOR = Color.YELLOW;

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    /**
     * Sole constructor.
     * 
     * @param component the parent Component
     * @param model the module model
     */
    public IntensitySliderWiggleMe( Component component, BaseModel model ) {

        super( component, model );

        Shape arrowShape = null;
        String string = SimStrings.get( "intensitySlider.wiggleMe.text" );

        // Create shapes, calculate bounds.
        Rectangle arrowBounds = null;
        Rectangle textBounds = null;
        Rectangle bounds = null;
        {
            // Arrow pointing left.
            Point2D tip = new Point2D.Double( 0, 0 );
            Point2D tail = new Point2D.Double( 25, 25 );
            Arrow arrow = new Arrow( tail, tip, 6, 6, 2, 100, false );
            arrowShape = arrow.getShape();
            arrowBounds = arrowShape.getBounds();

            // Text to right of arrow.
            int x = arrowBounds.x + arrowBounds.width + 10;
            int y = arrowBounds.y + arrowBounds.height + 10;
            FontMetrics fontMetrics = component.getFontMetrics( DEFAULT_FONT );
            int width = fontMetrics.stringWidth( string );
            int height = fontMetrics.getHeight();
            textBounds = new Rectangle( x, y, width, height );

            // Bounds union
            bounds = new Rectangle( arrowBounds );
            bounds.add( textBounds );
        }

        // Create a buffered image.
        BufferedImage bufferedImage = new BufferedImage( bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB );

        // Draw on the buffered image.
        {
            // Get a graphics context for the image.
            Graphics2D g2 = bufferedImage.createGraphics();

            // Set the graphics state
            RenderingHints hints = new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            g2.setRenderingHints( hints );
            g2.setColor( DEFAULT_COLOR );
            g2.setFont( DEFAULT_FONT );

            // Draw the arrow.
            g2.fill( arrowShape );

            // Draw the text.     
            g2.drawString( string, textBounds.x, textBounds.y );
        }

        // Set the wiggle-me graphic.
        PhetImageGraphic graphic = new PhetImageGraphic( component, bufferedImage );
        super.setGraphic( graphic );

        // Defaults.
        super.setWiggleTravel( DEFAULT_WIGGLE_SIZE );
        super.setCycleLength( DEFAULT_CYCLE_LENGTH );
    }
}