/* Copyright 2005, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.piccolo.help;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import edu.colorado.phet.common.view.graphics.Arrow;
import edu.colorado.phet.piccolo.nodes.HTMLGraphic;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolox.pswing.PSwing;

/**
 * HelpBalloon is a help item that consists of text (with optional shadow)
 * inside a balloon, with an option arrow.   This class handles the "look"
 * of the help item -- issues related to position and visibility are
 * handles in the base class.
 * <p>
 * If no arrow is specified, calls to pointAt will position the 
 * upper-left corner of the balloon at the specified location.
 * <p>
 * Many visual attributes can be customized using set methods.
 *
 * @author Chris Malley
 * @version $Revision$
 */
public class HelpBalloon extends AbstractHelpItem {
    
    //----------------------------------------------------------------------------
    // Public class data
    //----------------------------------------------------------------------------
    
    // Positions where the arrow tail is attached to the balloon.
    public static final Object TOP_LEFT = new String( "top left" );
    public static final Object TOP_CENTER = new String( "top center" );
    public static final Object TOP_RIGHT = new String( "top right" );
    public static final Object BOTTOM_LEFT = new String( "bottom left" );
    public static final Object BOTTOM_CENTER = new String( "bottom center" );
    public static final Object BOTTOM_RIGHT = new String( "bottom right" );
    public static final Object LEFT_TOP = new String( "left top" );
    public static final Object LEFT_CENTER = new String( "left center" );
    public static final Object LEFT_BOTTOM = new String( "left bottom" );
    public static final Object RIGHT_TOP = new String( "right top" );
    public static final Object RIGHT_CENTER = new String( "right center" );
    public static final Object RIGHT_BOTTOM = new String( "right bottom" );
    
    public static final double MAX_ARROW_ROTATION = 70; // degrees
    public static final double MIN_ARROW_ROTATION = -MAX_ARROW_ROTATION;
    
    //----------------------------------------------------------------------------
    // Private class data
    //----------------------------------------------------------------------------

    // Text parameters
    private static final Font DEFAULT_TEXT_FONT = new Font( "Lucida Sans", Font.PLAIN, 14 );
    private static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final double DEFAULT_TEXT_MARGIN = 4; // pixels
    private static final Dimension DEFAULT_SHADOW_TEXT_OFFSET = new Dimension( 1, 1 ); // pixels
    private static final Color DEFAULT_SHADOW_TEXT_COLOR = Color.RED;
    
    // Arrow parameters
    private static final int DEFAULT_ARROW_HEAD_WIDTH = 10; // pixels
    private static final int DEFAULT_ARROW_HEAD_HEIGHT = 10; // pixels
    private static final int DEFAULT_ARROW_TAIL_WIDTH = 3; // pixels
    private static final Paint DEFAULT_ARROW_FILL_PAINT = new Color( 250, 250, 170, 175 );
    private static final Paint DEFAULT_ARROW_BORDER_PAINT = Color.BLACK;
    private static final Stroke DEFAULT_ARROW_STROKE = new BasicStroke( 1f );
    
    // Balloon parameters
    private static final Paint DEFAULT_BALLOON_FILL_PAINT = DEFAULT_ARROW_FILL_PAINT;
    private static final Paint DEFAULT_BALLOON_BORDER_PAINT = DEFAULT_ARROW_BORDER_PAINT;
    private static final Stroke DEFAULT_BALLOON_STROKE = DEFAULT_ARROW_STROKE;
    private static final int DEFAULT_BALLOON_CORNER_RADIUS = 15; // pixels
    private static final double DEFAULT_BALLOON_ARROW_SPACING = 0; // pixels between arrow and balloon
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private HTMLGraphic _textNode;
    private HTMLGraphic _shadowTextNode;
    private PPath _balloonNode;
    private PPath _arrowNode; 
    
    private Object _arrowTailPosition; // where the arrow tail is attached to the balloon
    private double _arrowLength; // pixels
    private double _arrowRotation; // degrees
    private Dimension _arrowHeadSize;
    private double _arrowTailWidth;
    private double _textMargin;
    private Dimension _shadowTextOffset;
    private double _balloonCornerRadius;
    private double _balloonArrowSpacing;
    
    private boolean _updateEnabled;
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    /**
     * Creates a HelpBalloon with no arrow.
     * 
     * @param helpPanel
     * @param text
     */
    public HelpBalloon( JComponent helpPanel, String text ) {
        this( helpPanel, text, TOP_LEFT /* don't care */, 0 /* arrowLength */, 0 /* arrowRotation */ );
    }
    
    /**
     * Creates a HelpBalloon with an arrow that is perpendicular to one of the sides.
     * 
     * @param helpPanel
     * @param text
     * @param arrowTailPosition
     * @param arrowLength
     */
    public HelpBalloon( JComponent helpPanel, String text, Object arrowTailPosition, double arrowLength ) {
        this( helpPanel, text, arrowTailPosition, arrowLength, 0 /* arrowRotation */ );
    }
    
    /**
     * Creates a HelpBalloon with an arrow that is attached to a specified position
     * on the balloon.  If arrowRotation is zero, then the arrow is perpendicular to
     * the ballon's side. The arrowRotation value specifies how much the 
     * arrow is rotated from the perpendicular position.
     * 
     * @param helpPanel
     * @param text
     * @param arrowTailPosition
     * @param arrowLength
     * @param arrowRotation
     */
    public HelpBalloon( JComponent helpPanel, String text, Object arrowTailPosition, double arrowLength, double arrowRotation ) {
        super( helpPanel );
        
        // Validate arguments
        if ( !isValidArrowtailPostion( arrowTailPosition ) ) {
            throw new IllegalArgumentException( "invalid arrowTailPosition: " + arrowTailPosition );
        }
        if ( ! isValidArrowLength( arrowLength ) ) {
            throw new IllegalArgumentException( "invalid arrowLength: " + arrowLength );
        }
        if ( ! isValidArrowRotation( arrowRotation ) ) {
            throw new IllegalArgumentException( "invalid arrowRotation: " + arrowRotation );
        }
        
        // Not interactive
        setPickable( false );
        setChildrenPickable( false );
        
        // Arrow
        {
            _arrowTailPosition = arrowTailPosition;
            _arrowLength = arrowLength;
            _arrowRotation = arrowRotation;
            _arrowHeadSize = new Dimension( DEFAULT_ARROW_HEAD_WIDTH, DEFAULT_ARROW_HEAD_HEIGHT );
            _arrowTailWidth = DEFAULT_ARROW_TAIL_WIDTH;
            
            _arrowNode = new PPath();
            _arrowNode.setPaint( DEFAULT_ARROW_FILL_PAINT );
            _arrowNode.setStroke( DEFAULT_ARROW_STROKE );
            _arrowNode.setStrokePaint( DEFAULT_ARROW_BORDER_PAINT );
        }
        
        // Balloon
        {
            _balloonCornerRadius = DEFAULT_BALLOON_CORNER_RADIUS;
            _balloonArrowSpacing = DEFAULT_BALLOON_ARROW_SPACING;
            
            _balloonNode = new PPath();
            _balloonNode.setPaint( DEFAULT_BALLOON_FILL_PAINT );
            _balloonNode.setStroke( DEFAULT_BALLOON_STROKE );
            _balloonNode.setStrokePaint( DEFAULT_BALLOON_BORDER_PAINT );
        }

        // Text
        {
            _textMargin = DEFAULT_TEXT_MARGIN;

            _textNode = new HTMLGraphic( text );
            _textNode.setFont( DEFAULT_TEXT_FONT );
            _textNode.setColor( DEFAULT_TEXT_COLOR );
            
            _shadowTextOffset = new Dimension( DEFAULT_SHADOW_TEXT_OFFSET );
            
            _shadowTextNode = new HTMLGraphic( text );
            _shadowTextNode.setFont( DEFAULT_TEXT_FONT );
            _shadowTextNode.setColor( DEFAULT_SHADOW_TEXT_COLOR );
            _shadowTextNode.setVisible( false ); // default is no shadow text
        }

        addChild( _arrowNode );
        addChild( _balloonNode );
        addChild( _shadowTextNode );
        addChild( _textNode ); // add text last so that it's on top

        _updateEnabled = true;
        updateDisplay();
    }
    
    //----------------------------------------------------------------------------
    // Object overrides
    //----------------------------------------------------------------------------
    
    public String toString() {
        return getClass().getName() + " [text=" + _textNode.getHtml() + "]";
    }
    
    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    /**
     * Enables or disables updates.
     * If you're going to change a lot of attributes, set update to false first.
     * Then set it back to true after changing the attributes.
     * 
     * @param enabled true or false
     */
    public void setUpdateEnabled( boolean enabled ) {
        if ( enabled != _updateEnabled ) {
            _updateEnabled = enabled;
            if ( enabled ) {
                updateDisplay();
            }
        }
    }
    
    // Text attributes -----------------------------------------------------------
    
    public void setText( String html ) {
        _textNode.setHtml( html );
        _shadowTextNode.setHtml( html );
        updateDisplay();
    }

    public void setFont( Font font ) {
        _textNode.setFont( font );
        _shadowTextNode.setFont( font );
        updateDisplay();
    }
    
    public void setTextColor( Color color ) {
        _textNode.setColor( color );
    }
    
    /**
     * Margin between text and edges of balloon.
     * 
     * @param margin in pixels
     */
    public void setTextMargin( double margin ) {
        _textMargin = margin;
        updateDisplay();
    }
    
    // Shadow text attributes ------------------------------------------------------
        
    public void setShadowTextEnabled( boolean enabled ) {
        _shadowTextNode.setVisible( enabled );
    }
    
    public void setShadowTextColor( Color color ) {
        _shadowTextNode.setColor( color );
    }
    
    /**
     * Offset between primary and shadow text, in pixels.
     * @param x
     * @param y
     */
    public void setShadowTextOffset( double x, double y ) {
        _shadowTextOffset.setSize( x, y );
        updateDisplay();
    }

    // Balloon attributes ------------------------------------------------------
    
    public void setBalloonFillPaint( Paint paint ) {
        _balloonNode.setPaint( paint );
    }
    
    public void setBalloonStrokePaint( Paint paint ) {
        _balloonNode.setStrokePaint( paint );
    }
    
    public void setBalloonStroke( Stroke stroke ) {
        _balloonNode.setStroke( stroke );
    }
    
    /**
     * The balloon is a rounded rectangle.
     * This sets the corner radius on the rounded rectangle.
     * 
     * @param radius
     */
    public void setBalloonCornerRadius( double radius ) {
        if ( radius < 0 ) {
            throw new IllegalArgumentException( "radius < 0: " + radius );
        }
        _balloonCornerRadius = radius;
        updateDisplay();
    }
    
    // Arrow attributes ------------------------------------------------------
    
    public void setArrowFillPaint( Paint paint ) {
        _arrowNode.setPaint( paint );
    }
    
    public void setArrowStrokePaint( Paint paint ) {
        _arrowNode.setStrokePaint( paint );
    }
     
    public void setArrowStroke( Stroke stroke ) {
        _arrowNode.setStroke( stroke );
    }
    
    public void setArrowHeadSize( int width, int height ) {
        if ( width <= 0 ) {
            throw new IllegalArgumentException( "width <= 0: " + width );
        }
        if ( height <= 0 ) {
            throw new IllegalArgumentException( "height <= 0: " + height );
        }
        _arrowHeadSize = new Dimension( width, height );
        updateDisplay();
    }
    
    public void setArrowTailWidth( double width ) {
        if ( width <= 0 ) {
            throw new IllegalArgumentException( "width <= 0: " + width );
        }
        _arrowTailWidth = width;
        updateDisplay();
    }
    
    /**
     * Sets the spacing between the balloon and the arrow tail.
     * 
     * @param spacing
     */
    public void setArrowBalloonSpacing( double spacing ) {
        _balloonArrowSpacing = spacing;
        updateDisplay();
    }
    
    public void setArrowTailPosition( Object arrowTailPosition ) {
        if ( !isValidArrowtailPostion( arrowTailPosition ) ) {
            throw new IllegalArgumentException( "invalid arrow tail position: " + arrowTailPosition );
        }
        _arrowTailPosition = arrowTailPosition;
        updatePosition();
        updateDisplay();
    }
    
    public void setArrowLength( double arrowLength ) {
        if ( ! isValidArrowLength( arrowLength ) ) {
            throw new IllegalArgumentException( "invlaid arrow length: " + arrowLength );
        }
        _arrowLength = arrowLength;
        updateDisplay();
    }
    
    public void setArrowRotation( double arrowRotation ) {
        if ( ! isValidArrowRotation( arrowRotation ) ) {
            throw new IllegalArgumentException( "invalid arrowRotation: " + arrowRotation ); 
        }
        _arrowRotation = arrowRotation;
        updateDisplay();
    }
    
    //----------------------------------------------------------------------------
    // Convenience function for determining arrow position and orientation.
    //----------------------------------------------------------------------------
    
    protected boolean isArrowVertical() {
        return isArrowOnTop() || isArrowOnBottom();
    }
    
    protected boolean isArrowHorizontal() {
        return !isArrowVertical();
    }
    
    protected boolean isArrowOnTop() {
        return ( _arrowTailPosition == TOP_LEFT || _arrowTailPosition == TOP_CENTER || _arrowTailPosition == TOP_RIGHT  );
    }
    
    protected boolean isArrowOnBottom() {
        return ( _arrowTailPosition == BOTTOM_LEFT || _arrowTailPosition == BOTTOM_CENTER || _arrowTailPosition == BOTTOM_RIGHT );
    }
    
    protected boolean isArrowOnLeft() {
        return ( _arrowTailPosition == LEFT_TOP || _arrowTailPosition == LEFT_CENTER || _arrowTailPosition == LEFT_BOTTOM );
    }
    
    protected boolean isArrowOnRight() {
        return ( _arrowTailPosition == RIGHT_TOP || _arrowTailPosition == RIGHT_CENTER || _arrowTailPosition == RIGHT_BOTTOM );
    }
      
    /**
     * Determines if a  value for arrow tail position is valid.
     * 
     * @param arrowTailPosition
     * @return  true or false
     */
    public boolean isValidArrowtailPostion( Object arrowTailPosition ) {
        return ( 
                arrowTailPosition == TOP_LEFT ||
                arrowTailPosition == TOP_CENTER || 
                arrowTailPosition == TOP_RIGHT ||
                arrowTailPosition == BOTTOM_LEFT ||
                arrowTailPosition == BOTTOM_CENTER || 
                arrowTailPosition == BOTTOM_RIGHT ||
                arrowTailPosition == LEFT_TOP ||
                arrowTailPosition == LEFT_CENTER || 
                arrowTailPosition == LEFT_BOTTOM ||
                arrowTailPosition == RIGHT_TOP ||
                arrowTailPosition == RIGHT_CENTER ||
                arrowTailPosition == RIGHT_BOTTOM );
    }
    
    public boolean isValidArrowLength( double arrowLength ) {
        return ( arrowLength >= 0 );
    }
    
    public boolean isValidArrowRotation( double arrowRotation ) {
        return ( arrowRotation >= MIN_ARROW_ROTATION && arrowRotation <= MAX_ARROW_ROTATION );
    }
    
    //----------------------------------------------------------------------------
    // PNode overrides
    //----------------------------------------------------------------------------
    
    /**
     * Updates the display when we become visible.
     * 
     * @param visible
     */
    public void setVisible( boolean visible ) {
        super.setVisible( visible );
        if ( getVisible() == true ) {
            updateDisplay();
        }
    }
    
    //----------------------------------------------------------------------------
    // Layout
    //----------------------------------------------------------------------------
    
    /**
     * Sizes the balloon to fit the text.
     * Creates the arrow with its tip at (0,0).
     * Adjusts arrow rotation.
     * Positions the balloon and text relative to the arrow position.
     */
    public void updateDisplay() {
        
        if ( !_updateEnabled ) {
            return;
        }
        
        // Resize the balloon to fit the text.
        {
            double width = _textNode.getWidth() + ( 2 * _textMargin );
            double height = _textNode.getHeight() + ( 2 * _textMargin );
            Shape shape = new RoundRectangle2D.Double( 0, 0, width, height, _balloonCornerRadius, _balloonCornerRadius );
            _balloonNode.setPathTo( shape );
        }
        
        // Do we have an arrow?
        if ( _arrowLength <= _arrowHeadSize.getHeight() ) {
            _arrowNode.setVisible( false );
        }
        else {
            _arrowNode.setVisible( true );
            
            // Create the arrow with tip at (0,0)
            Point tipPoint = new Point( 0, 0 );
            Point tailPoint = new Point();
            if ( isArrowOnTop() ) {
                tailPoint.setLocation( 0, _arrowLength );
            }
            else if ( isArrowOnBottom() ) {
                tailPoint.setLocation( 0, -_arrowLength );
            }
            else if ( isArrowOnLeft() ) {
                tailPoint.setLocation( _arrowLength, 0 );
            }
            else if ( isArrowOnRight() ) {
                tailPoint.setLocation( -_arrowLength, 0 );
            }
            else {
                throw new IllegalArgumentException( "illegal arrow position: " + _arrowTailPosition );
            }
            Arrow arrow = new Arrow( tailPoint, tipPoint, _arrowHeadSize.getHeight(), _arrowHeadSize.getWidth(), _arrowTailWidth );
            
            // Rotate the arrow...
            AffineTransform transform = new AffineTransform();
            transform.rotate( Math.toRadians( _arrowRotation ) );
            Shape shape = transform.createTransformedShape( arrow.getShape() );
            
            _arrowNode.setPathTo( shape );
        }
        
        // Position the balloon & text relative to the arrow, account for arrow rotation...
        double x = 0;
        double y = 0;
        final boolean hasArrow = _arrowNode.getVisible();
        final double offset = _arrowHeadSize.getWidth() / 2;
        if ( _arrowTailPosition == TOP_LEFT ) {
            if ( !hasArrow ) {
                x = 0;
                y = 0;
            }
            else {
                x = -( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - offset;
                y = _arrowNode.getHeight() + _balloonArrowSpacing;
            }
        }
        else if ( _arrowTailPosition == TOP_CENTER ) {
            if ( !hasArrow ) {
                x = -_balloonNode.getWidth() / 2;
                y = 0;
            }
            else {
                x = -( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - ( _balloonNode.getWidth() / 2 );
                y = _arrowNode.getHeight() + _balloonArrowSpacing;
            }
        }
        else if ( _arrowTailPosition ==  TOP_RIGHT ) {
            if ( !hasArrow ) {
                x = -_balloonNode.getWidth();
                y = 0;
            }
            else {
                x = -( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - _balloonNode.getWidth() + offset;
                y = _arrowNode.getHeight() + _balloonArrowSpacing;
            }
        }
        else if ( _arrowTailPosition == BOTTOM_LEFT ) {
            if ( !hasArrow ) {
                x = 0;
                y = -_balloonNode.getHeight();
            }
            else {
                x = ( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - offset;
                y = -( _balloonNode.getHeight() + _arrowNode.getHeight() + _balloonArrowSpacing );
            }
        }
        else if ( _arrowTailPosition == BOTTOM_CENTER ) {
            if ( !hasArrow ) {
                x = -_balloonNode.getWidth() / 2;
                y = -_balloonNode.getHeight();
            }
            else {
                x = ( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - ( _balloonNode.getWidth() / 2 );
                y = -( _balloonNode.getHeight() + _arrowNode.getHeight() + _balloonArrowSpacing );
            }
        }    
        else if ( _arrowTailPosition == BOTTOM_RIGHT ) {
            if ( !hasArrow ) {
                x = -_balloonNode.getWidth();
                y = -_balloonNode.getHeight();
            }
            else {
                x = ( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - _balloonNode.getWidth() + offset;
                y = -( _balloonNode.getHeight() + _arrowNode.getHeight() + _balloonArrowSpacing );
            }
        }
        else if ( _arrowTailPosition == LEFT_TOP ) {
            if ( !hasArrow ) {
                x = 0;
                y = 0;   
            }
            else {
                x = _arrowNode.getWidth() + _balloonArrowSpacing;
                y = ( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - offset;
            }
        }
        else if ( _arrowTailPosition == LEFT_CENTER ) {
            if ( !hasArrow ) {
                x = 0;
                y = -_balloonNode.getHeight() / 2;
            }
            else {
                x = _arrowNode.getWidth() + _balloonArrowSpacing;
                y = ( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - ( _balloonNode.getHeight() / 2 );
            }
        }
        else if ( _arrowTailPosition == LEFT_BOTTOM ) {
            if ( !hasArrow ) {
                x = 0;
                y = -_balloonNode.getHeight();
            }
            else {
                x = _arrowNode.getWidth() + _balloonArrowSpacing;
                y = ( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - _balloonNode.getHeight() + offset;
            }
        }
        else if ( _arrowTailPosition == RIGHT_TOP ) {
            if ( !hasArrow ) {
                x = -_balloonNode.getWidth();
                y = 0;
            }
            else {
                x = -( _balloonNode.getWidth() + _arrowNode.getWidth() + _balloonArrowSpacing );
                y = -( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - offset;
            }
        }
        else if ( _arrowTailPosition == RIGHT_CENTER ) {
            if ( !hasArrow ) {
                x = -_balloonNode.getWidth();
                y = -_balloonNode.getHeight() / 2;
            }
            else {
                x = -( _balloonNode.getWidth() + _arrowNode.getWidth() + _balloonArrowSpacing );
                y = -( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - ( _balloonNode.getHeight() / 2 );
            }
        }
        else if ( _arrowTailPosition == RIGHT_BOTTOM ) {
            if ( !hasArrow ) {
                x = -_balloonNode.getWidth();
                y = -_balloonNode.getHeight();
            }
            else {
                x = -( _balloonNode.getWidth() + _arrowNode.getWidth() + _balloonArrowSpacing );
                y = -( _arrowLength * Math.sin( Math.toRadians( _arrowRotation ) ) ) - _balloonNode.getHeight() + offset;
            }
        }
        else {
            throw new IllegalArgumentException( "illegal arrow position: " + _arrowTailPosition );
        }
        
        _balloonNode.setOffset( x, y );
        _textNode.setOffset( x + _textMargin, y + _textMargin );
        _shadowTextNode.setOffset( x + _textMargin + _shadowTextOffset.getWidth(), y + _textMargin + _shadowTextOffset.getHeight() );
    }
    
    //----------------------------------------------------------------------------
    // AbstractHelpItem overrides
    //----------------------------------------------------------------------------
    
    /**
     * Maps to a point that allows us to point to the center of 
     * one of the node's edges, instead of its upper-left corner.
     * Which edge is dependent on the arrow position.
     * 
     * @param node
     * @param canvas
     */
    public Point2D mapLocation( PNode node, PCanvas canvas ) {
        
        // Map the node's location (upper-left corner) to help pane coordinates...
        Point2D p = super.mapLocation( node, canvas );
        
        // Determine the node's dimensions, in help pane coordinates...
        Rectangle2D fullBounds = node.getFullBounds(); // in parent's local coordinates
        Rectangle2D globalFullBounds = node.getParent().localToGlobal( fullBounds );
        PCamera camera = canvas.getCamera();
        PAffineTransform transform = camera.getViewTransformReference();
        Rectangle2D bounds = transform.transform( globalFullBounds, null );
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        
        // Translate the help pane coordinates, using the node's dimensions and arrow position...
        if ( isArrowOnTop() ) {
            p.setLocation( p.getX() + ( width / 2 ), p.getY() + height );
        }
        else if ( isArrowOnBottom() ) {
            p.setLocation( p.getX() + ( width / 2 ), p.getY() );
        }
        else if ( isArrowOnRight() ) {
            p.setLocation( p.getX(), p.getY() + ( height / 2 ) );
        }
        else {
            p.setLocation( p.getX() + width, p.getY() + ( height / 2 ) );
        }
        
        return p;
    }
    
    /**
     * Maps to a point that allows us to point to the center of 
     * one of the component's edges, instead of its upper-left corner.
     * Which edge is dependent on the arrow position.
     * 
     * @param component
     */
    public Point2D mapLocation( JComponent component ) {
        
        // Map the component's location (upper-left corner) to help pane coordinates...
        Point2D p = super.mapLocation( component );
        
        // Determine the component's dimensions...
        double width = component.getWidth();
        double height = component.getHeight();
        
        // Translate the help pane coordinates, using the component's dimensions and arrow position...
        if ( isArrowOnTop() ) {
            p.setLocation( p.getX() + ( width / 2 ), p.getY() + height );
        }
        else if ( isArrowOnBottom() ) {
            p.setLocation( p.getX() + ( width / 2 ), p.getY() );
        }
        else if ( isArrowOnRight() ) {
            p.setLocation( p.getX(), p.getY() + ( height / 2 ) );
        }
        else {
            p.setLocation( p.getX() + width, p.getY() + ( height / 2 ) );
        }

        return p;
    }
    
    public Point2D mapLocation( JComponent component, PSwing pswing, PCanvas canvas ) {
        
        // Map the component's location to help pane coordinates...
        Point2D p = super.mapLocation( component, pswing, canvas );
        
        // Determine the component's dimensions, in help pane coordinates...
        Rectangle2D fullBounds = component.getBounds();
        Rectangle2D globalFullBounds = pswing.getParent().localToGlobal( fullBounds );
        PCamera camera = canvas.getCamera();
        PAffineTransform transform = camera.getViewTransformReference();
        Rectangle2D bounds = transform.transform( globalFullBounds, null );
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        
        // Translate the help pane coordinates, using the component's dimensions and arrow position...
        if ( isArrowOnTop() ) {
            p.setLocation( p.getX() + ( width / 2 ), p.getY() + height );
        }
        else if ( isArrowOnBottom() ) {
            p.setLocation( p.getX() + ( width / 2 ), p.getY() );
        }
        else if ( isArrowOnRight() ) {
            p.setLocation( p.getX(), p.getY() + ( height / 2 ) );
        }
        else {
            p.setLocation( p.getX() + width, p.getY() + ( height / 2 ) );
        }
        
        return p;
    }
}