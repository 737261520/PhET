/* Copyright 2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.faraday.view;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import edu.colorado.phet.common.view.phetgraphics.PhetGraphic;


/**
 * CompassNeedleGraphic is the graphical representation of a compass needle.
 * A needle has a "north tip" and a "south tip".
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class CompassNeedleGraphic extends PhetGraphic {

    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------

    private static final Color NORTH_COLOR = Color.RED;
    private static final Color SOUTH_COLOR = Color.WHITE;
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private double _direction; // in radians
    private Dimension _size;
    private Shape _northTip, _southTip;
    private Color _northColor, _southColor;
    private double _strength;  // 0.0 - 1.0
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    /**
     * Sole constructor.
     * 
     * @param component the parent Component
     */
    public CompassNeedleGraphic( Component component ) {
        
        super( component );
        assert( component != null );
        
        setRenderingHints( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
        
        setSize( new Dimension( 40, 20 ) );
        setDirection( 0.0 );
        setStrength( 1.0 );
        
        updateShape();
    }

    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    /**
     * Sets the direction that the north pole of the needle points.  
     * Zero degrees points down the positive X axis.
     * 
     * @param direction the direction, in radians
     */
    public void setDirection( double direction ) {
        _direction = direction;
        clearTransform();
        rotate( direction );
        repaint();
    }
    
    /**
     * Gets the direction that the north pole of the needle points.
     * Zero degrees points down the positive X axis.
     * 
     * @return the direction, in degrees
     */
    public double getDirection() {
        return _direction;
    }

    /** 
     * Sets the size of the needle.
     * Width is measured from the tip-to-tip (north-to-south ).
     * 
     * @param size the size
     */
    public void setSize( Dimension size ) {
        assert( size != null );
        _size = new Dimension( size );
        updateShape();
        repaint();
    }
    
    /** 
     * Sets the size of the needle.
     * Width is measured from the tip-to-tip (north-to-south ).
     * 
     * @param width the width
     * @param height the height
     */
    public void setSize( int width, int height ) {
        setSize( new Dimension( width, height ) );
    }
    
    /** 
     * Gets the size of the needle.
     * Width is measured from the tip-to-tip (north-to-south ).
     * 
     * @return the size
     */
    public Dimension getSize() {
        return new Dimension( _size );
    }
    
    /**
     * Sets the relative strength that is to be displayed by the needle.
     * This is a value between 0-1. The strength value is 
     * a multiplier used to set the alpha channel of the rendered needle.
     * 0 is fully transparent, 1 is fully opaque, values in between are partially transparent.
     * 
     * @param strength the strength
     * @throws IllegalArgumentException if strength is out of range
     */
    public void setStrength( double strength ) {
        if ( ! ( strength >= 0 && strength <= 1 ) ) {
            throw new IllegalArgumentException( "strength must be 0.0-1.0 : " + strength );
        }
        if ( strength != _strength ) {
            _strength = strength;

            // Control the alpha of the color to make the needle look "dimmer".
            int alpha = (int) ( 255 * _strength );
            _northColor = new Color( NORTH_COLOR.getRed(), NORTH_COLOR.getGreen(), NORTH_COLOR.getBlue(), alpha );
            _southColor = new Color( SOUTH_COLOR.getRed(), SOUTH_COLOR.getGreen(), SOUTH_COLOR.getBlue(), alpha );

            repaint();
        }
    }
    
    /**
     * Gets the strength.
     * 
     * @see setStrength
     * @return the strength
     */
    public double getStrength() {
        return _strength;
    }
    
    //----------------------------------------------------------------------------
    // Shapes
    //----------------------------------------------------------------------------

    /*
     * Updates the shape of the needle to match the settings provided.
     * <p>
     * Prior to applying transforms, the north tip of the needle points
     * down the X-axis, and the south tip points down the Y-axis.
     */
    private void updateShape() {
        
        GeneralPath northPath = new GeneralPath();
        northPath.moveTo( 0, -(_size.height/2) );
        northPath.lineTo( (_size.width/2), 0 );
        northPath.lineTo( 0, (_size.height/2) );
        northPath.closePath();
        _northTip = northPath;
        
        GeneralPath southPath = new GeneralPath();
        southPath.moveTo( 0, -(_size.height/2) );
        southPath.lineTo( 0, (_size.height/2) );
        southPath.lineTo( -(_size.width/2), 0 );
        southPath.closePath();
        _southTip = southPath;
    }
    
    //----------------------------------------------------------------------------
    // PhetGraphic implementation
    //----------------------------------------------------------------------------

    /*
     * Determines the bounds of the needle, after transforms are applied.
     * 
     * @return the bounds
     */
    protected Rectangle determineBounds() {
        Rectangle r = new Rectangle( _northTip.getBounds() );
        r.add( _southTip.getBounds() );
        return getNetTransform().createTransformedShape( r ).getBounds();
    }

    /*
     * Draws the needle.
     * 
     * @param g2 the graphics context
     */
    public void paint( Graphics2D g2 ) {
        assert( g2 != null );
        if ( isVisible() ) {
            super.saveGraphicsState( g2 );
            {
                // Request antialiasing
                RenderingHints hints = getRenderingHints();
                if ( hints != null ) {
                    g2.setRenderingHints( hints );
                }
                
                // Transform
                g2.transform( getNetTransform() );
                
                // Draw the "north" tip.
                g2.setPaint( _northColor );
                g2.fill( _northTip );
                
                // Draw the "south" tip.
                g2.setPaint( _southColor );
                g2.fill( _southTip );
            }
            super.restoreGraphicsState();
        }
    }
}
