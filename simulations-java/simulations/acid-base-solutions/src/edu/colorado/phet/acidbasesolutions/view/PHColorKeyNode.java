/* Copyright 2010, University of Colorado */

package edu.colorado.phet.acidbasesolutions.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import edu.colorado.phet.acidbasesolutions.constants.ABSConstants;
import edu.colorado.phet.acidbasesolutions.constants.ABSStrings;
import edu.colorado.phet.acidbasesolutions.model.PHPaper;
import edu.colorado.phet.acidbasesolutions.model.SolutionRepresentation.SolutionRepresentationChangeAdapter;
import edu.colorado.phet.common.phetcommon.math.Function;
import edu.colorado.phet.common.phetcommon.math.Function.LinearFunction;
import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.PhetPNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * Displays a color key for the pH paper.
 * pH values are mapped to colors in the visible spectrum.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class PHColorKeyNode extends PhetPNode {
    
    private static final Color TICK_COLOR = Color.BLACK;
    private static final Stroke TICK_STROKE = new BasicStroke( 1f );
    private static final int TICK_LENGTH = 5;
    private static final int TICK_INCREMENT = 1;
    
    private static final Color LABEL_COLOR = Color.BLACK;
    private static final Font LABEL_FONT = new PhetFont( 12 );
    
    private static final Color TITLE_COLOR = Color.BLACK;
    private static final Font TITLE_FONT = new PhetFont( 18 );
    
    public PHColorKeyNode( final PHPaper paper ) {
        
        setPickable( false );
        setChildrenPickable( false );
        
        paper.addSolutionRepresentationChangeListener( new SolutionRepresentationChangeAdapter() {
            public void visibilityChanged() {
                setVisible( paper.isVisible() );
            }
        });
        
        // title
        PText titleNode = new PText( ABSStrings.PH_COLOR_KEY );
        titleNode.setTextPaint( TITLE_COLOR );
        titleNode.setFont( TITLE_FONT );
        addChild( titleNode );
        
        // spectrum
        SpectrumNode spectrumNode = new SpectrumNode( ABSConstants.PH_COLOR_KEY_SIZE );
        addChild( spectrumNode );
        spectrumNode.setOffset( 0, titleNode.getFullBoundsReference().getMaxY() + 3 );
        
        // tick marks
        LinearFunction f = new Function.LinearFunction( ABSConstants.MIN_PH, ABSConstants.MAX_PH, 0, ABSConstants.PH_COLOR_KEY_SIZE.getWidth() );
        for ( int pH = ABSConstants.MIN_PH; pH <= ABSConstants.MAX_PH; pH = pH + TICK_INCREMENT ) {
            TickMarkNode tickMarkNode = new TickMarkNode( pH );
            addChild( tickMarkNode );
            double x = f.evaluate( pH );
            double y = spectrumNode.getFullBoundsReference().getMaxY();
            tickMarkNode.setOffset( x, y );
        }
        
        setVisible( paper.isVisible() );
    }
    
    /*
     * Color spectrum, origin at upper-left corner.
     */
    private static class SpectrumNode extends PComposite {
        
        public SpectrumNode( PDimension size ) {
            
            PPath outlineNode = new PPath( new Rectangle2D.Double( 0, 0, size.getWidth(), size.getHeight() ) );
            outlineNode.setStroke( new BasicStroke( 1f ) );
            outlineNode.setStrokePaint( Color.BLACK );
            addChild( outlineNode );
        }
    }
    
    /*
     * Tick mark, a vertical line with a pH value centered below it.
     * Origin at top center of tick line.
     */
    private static class TickMarkNode extends PComposite {
        
        public TickMarkNode( int pH ) {
            
            PPath lineNode = new PPath( new Line2D.Double( 0, 0, 0, TICK_LENGTH ) );
            lineNode.setStroke( TICK_STROKE );
            lineNode.setStrokePaint( TICK_COLOR );
            addChild( lineNode );
            
            PText labelNode = new PText( String.valueOf( pH ) );
            labelNode.setTextPaint( LABEL_COLOR );
            labelNode.setFont( LABEL_FONT );
            addChild( labelNode );
            
            double x = -lineNode.getFullBoundsReference().getWidth() / 2;
            double y = 0;
            lineNode.setOffset( x, y );
            x = -labelNode.getFullBoundsReference().getWidth() / 2;
            y = lineNode.getFullBoundsReference().getMaxY() + 1;
            labelNode.setOffset( x, y );
        }
    }
}
