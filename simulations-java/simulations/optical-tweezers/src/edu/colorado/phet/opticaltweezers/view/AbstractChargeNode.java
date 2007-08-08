/* Copyright 2007, University of Colorado */

package edu.colorado.phet.opticaltweezers.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Observable;
import java.util.Observer;

import edu.colorado.phet.common.piccolophet.PhetPNode;
import edu.colorado.phet.opticaltweezers.model.Bead;
import edu.colorado.phet.opticaltweezers.model.Laser;
import edu.colorado.phet.opticaltweezers.model.ModelViewTransform;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * AbstractChargeNode is the base class for nodes that display charge on the bead.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public abstract class AbstractChargeNode extends PhetPNode implements Observer {
    
    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------
    
    private static final Color POSITIVE_COLOR = Color.RED;
    private static final Color NEGATIVE_COLOR = Color.BLUE;
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private Bead _bead;
    private Laser _laser;
    private ModelViewTransform _modelViewTransform;
    private Point2D _pModel; // reusable point
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    public AbstractChargeNode( Bead bead, Laser laser, ModelViewTransform modelViewTransform ) {
        super();
        
        setPickable( false );
        setChildrenPickable( false );
        
        _bead = bead;
        _bead.addObserver( this );
        
        _laser = laser;
        _laser.addObserver( this );
        
        _modelViewTransform = modelViewTransform;
        
        _pModel = new Point2D.Double();
        
        initialize();
        updatePosition();
    }

    public void cleanup() {
        _bead.deleteObserver( this );
        _laser.deleteObserver( this );
    }
    
    //----------------------------------------------------------------------------
    // Abstract methods
    //----------------------------------------------------------------------------
    
    /**
     * Implemented by subclasses to initial the node.
     */
    protected abstract void initialize();
    
    //----------------------------------------------------------------------------
    // Setters and getters
    //----------------------------------------------------------------------------
    
    protected Bead getBead() {
        return _bead;
    }
    
    protected Laser getLaser() {
        return _laser;
    }
    
    protected ModelViewTransform getModelViewTransform() {
        return _modelViewTransform;
    }
    
    //----------------------------------------------------------------------------
    // Node creation
    //----------------------------------------------------------------------------
    
    /**
     * Creates the nodes that represents a positive charge, looks like a '+' sign.
     * 
     * @param size the width and height of the node
     * @param strokeWidth width of the stroke
     */
    protected static PNode createPositiveNode( double size, double strokeWidth ) {
        Stroke stroke = createStroke( strokeWidth );
        // Positive charge is a '+' sign
        Line2D horizontalLine = new Line2D.Double( -size/2, 0, size/2, 0 );
        PPath horizontalPathNode = new PPath( horizontalLine );
        horizontalPathNode.setStrokePaint( POSITIVE_COLOR );
        horizontalPathNode.setStroke( stroke );
        Line2D verticalLine = new Line2D.Double( 0, -size/2, 0, size/2 );
        PPath verticalPathNode = new PPath( verticalLine );
        verticalPathNode.setStrokePaint( POSITIVE_COLOR );
        verticalPathNode.setStroke( stroke );
        PComposite parentNode = new PComposite();
        parentNode.addChild( horizontalPathNode );
        parentNode.addChild( verticalPathNode );
        // Convert to an image
        PImage imageNode = new PImage( parentNode.toImage() );
        return imageNode;
    }
    
    /**
     * Creates the nodes that represents a positive charge, looks like a '-' sign.
     * 
     * @param size the width the node
     * @param strokeWidth width of the stroke
     */
    protected static PNode createNegativeNode( double size, double strokeWidth ) {
        Stroke stroke = createStroke( strokeWidth );
        // Negative charge is a horizontal line
        Line2D line = new Line2D.Double( 0, 0, size, 0 );
        PPath pathNode = new PPath( line );
        pathNode.setStrokePaint( NEGATIVE_COLOR );
        pathNode.setStroke( stroke );
        // Convert to an image
        PImage imageNode = new PImage( pathNode.toImage() );
        return imageNode;
    }
    
    /*
     * Use a butt cap so that line lengths are not extended.
     * See BasicStroke.CAP_BUTT.
     */
    private static Stroke createStroke( double strokeWidth ) {
        return new BasicStroke( (float)strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
    }
    
    //----------------------------------------------------------------------------
    // Superclass overrides
    //----------------------------------------------------------------------------
    
    /**
     * Updates the node when it's made visible.
     * 
     * @param visible
     */
    public void setVisible( boolean visible ) {
        if ( visible != isVisible() ) {
            if ( visible ) {
                updatePosition();
                updateCharge();
            }
            super.setVisible( visible );
        }
    }
    
    //----------------------------------------------------------------------------
    // Observer implementation
    //----------------------------------------------------------------------------
    
    /**
     * Updates the view to match the model.
     * 
     * @param o
     * @param arg
     */
    public void update( Observable o, Object arg ) {
        if ( isVisible() ) {
            if ( o == _bead ) {
                if ( arg == Bead.PROPERTY_POSITION ) {
                    updatePosition();
                }
            }
            else if ( o == _laser ) {
                if ( arg == Laser.PROPERTY_ELECTRIC_FIELD ) {
                    updateCharge();
                }
            }
        }
    }
    
    //----------------------------------------------------------------------------
    // Updaters
    //----------------------------------------------------------------------------
    
    /*
     * Updates the position of the node to match the bead's position.
     */
    private void updatePosition() {

        _modelViewTransform.modelToView( _bead.getPositionReference(), _pModel );
        setOffset( _pModel );
    }
    
    /**
     * Updates the view of the charge to match the model.
     */
    protected abstract void updateCharge();
}
