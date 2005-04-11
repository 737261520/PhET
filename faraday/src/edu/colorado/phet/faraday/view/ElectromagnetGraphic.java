/* Copyright 2005, University of Colorado */

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
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import edu.colorado.phet.common.model.BaseModel;
import edu.colorado.phet.common.util.SimpleObserver;
import edu.colorado.phet.common.view.ApparatusPanel2;
import edu.colorado.phet.common.view.ApparatusPanel2.ChangeEvent;
import edu.colorado.phet.common.view.graphics.mousecontrols.TranslationEvent;
import edu.colorado.phet.common.view.graphics.mousecontrols.TranslationListener;
import edu.colorado.phet.common.view.phetgraphics.GraphicLayerSet;
import edu.colorado.phet.common.view.phetgraphics.PhetGraphic;
import edu.colorado.phet.common.view.phetgraphics.PhetShapeGraphic;
import edu.colorado.phet.faraday.FaradayConfig;
import edu.colorado.phet.faraday.model.ACPowerSupply;
import edu.colorado.phet.faraday.model.Battery;
import edu.colorado.phet.faraday.model.Electromagnet;
import edu.colorado.phet.faraday.model.SourceCoil;


/**
 * ElectromagnetGraphic
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class ElectromagnetGraphic 
implements SimpleObserver, ICollidable, ApparatusPanel2.ChangeListener {

    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private Rectangle _parentBounds;
    private Rectangle _bounds;
    private Electromagnet _electromagnetModel;
    private CoilGraphic _coilGraphic;
    private BatteryGraphic _batteryGraphic;
    private ACPowerSupplyGraphic _acPowerSupplyGraphic;
    private GraphicLayerSet _foreground, _background;
    private CollisionDetector _collisionDetector;
    private PhetShapeGraphic _boundsGraphic;
    private Rectangle _someRectangle;
    
    //----------------------------------------------------------------------------
    // Constructors & finalizers
    //----------------------------------------------------------------------------
    
    public ElectromagnetGraphic(
            Component component,
            BaseModel baseModel,
            Electromagnet electromagnetModel,
            SourceCoil sourceCoilModel,
            Battery batteryModel,
            ACPowerSupply acPowerSupplyModel ) {
        
        assert ( component != null );
        assert ( baseModel != null );
        assert ( electromagnetModel != null );
        
        _collisionDetector = new CollisionDetector( this );
        
        _parentBounds = new Rectangle( 0, 0, component.getWidth(), component.getHeight() );
        _bounds = new Rectangle();
        
        _electromagnetModel = electromagnetModel;
        _electromagnetModel.addObserver( this );
        
        // Graphics components
        _coilGraphic = new CoilGraphic( component, sourceCoilModel, baseModel );
        _batteryGraphic = new BatteryGraphic( component, batteryModel );
        _acPowerSupplyGraphic = new ACPowerSupplyGraphic( component, acPowerSupplyModel );
        
        // Foreground composition
        _foreground = new GraphicLayerSet( component );
        _foreground.addGraphic( _coilGraphic.getForeground() );
        _foreground.addGraphic( _batteryGraphic );
        _foreground.addGraphic( _acPowerSupplyGraphic );
        
        // Background composition
        _background = new GraphicLayerSet( component );
        _background.addGraphic( _coilGraphic.getBackground() );
        
        // Graphic that represents the magnet's bounds.
        if ( FaradayConfig.DEBUG_DRAW_ELECTROMAGNET_BOUNDS ) {
            _someRectangle = new Rectangle();
            _boundsGraphic = new PhetShapeGraphic( component );
            _boundsGraphic.setShape( _someRectangle );
            _boundsGraphic.setBorderColor( Color.YELLOW );
            _boundsGraphic.setStroke( new BasicStroke( 1f ) );
            _foreground.addGraphic( _boundsGraphic );
        }
        
        // Interactivity
        _foreground.setCursorHand();
        _background.setCursorHand();
        InteractivityListener listener = new InteractivityListener();
        _foreground.addTranslationListener( listener );
        _foreground.addMouseInputListener( listener );
        _background.addTranslationListener( listener );
        _background.addMouseInputListener( listener );
        
        update();
    }
    
    /**
     * Finalizes an instance of this type.
     * Call this method prior to releasing all references to an object of this type.
     */
    public void finalize() {
        _electromagnetModel.removeObserver( this );
        _electromagnetModel = null;
        _coilGraphic.finalize();
        _batteryGraphic.finalize();
        _acPowerSupplyGraphic.finalize();
    }
    

    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    /**
     * Gets the PhetGraphic that contains the foreground elements of the coil.
     * 
     * @return the foreground graphics
     */
    public PhetGraphic getForeground() {
        return _foreground;
    }
    
    /**
     * Gets the PhetGraphic that contains the background elements of the coil.
     * 
     * @return the background graphics
     */
    public PhetGraphic getBackground() {
        return _background;
    }
    
    /**
     * Gets the coil graphic.
     * This is intended for use in debugging, or for connecting a control panel.
     */
    public CoilGraphic getCoilGraphic() {
        return _coilGraphic;
    }
 
    /**
     * Is a specified point inside the graphic?
     * 
     * @param p the point
     * @return true or false
     */
    public boolean contains( Point p ) {
        return _foreground.contains( p ) || _background.contains( p );
    }
    
    public void setVisible( boolean visible ) {
        _foreground.setVisible( visible );
        _background.setVisible( visible );
        update();
    }
    
    public boolean isVisible() {
        return _foreground.isVisible() || _background.isVisible();
    }
    
    //----------------------------------------------------------------------------
    // SimpleObserver implementation
    //----------------------------------------------------------------------------
    
    public void update() {
        if ( isVisible() ) {
            if ( FaradayConfig.DEBUG_DRAW_ELECTROMAGNET_BOUNDS ) { 
                // Configure the graphic that represents the magnet size.
                _someRectangle.setBounds( 0, 0, (int)_electromagnetModel.getWidth(), (int)_electromagnetModel.getHeight() );
                _boundsGraphic.setShape( _someRectangle );
                _boundsGraphic.centerRegistrationPoint();
            }
            
            // Location
            _foreground.setLocation( (int) _electromagnetModel.getX(), (int) _electromagnetModel.getY() );
            _background.setLocation( (int) _electromagnetModel.getX(), (int) _electromagnetModel.getY() );
            
            // Position the voltage sources at the top of the coil.
            _foreground.clearTransform();
            _background.clearTransform();
            _bounds.setBounds( _coilGraphic.getForeground().getBounds() );
            _bounds.union( _coilGraphic.getBackground().getBounds() );
            int x = 0;
            int y = -( _bounds.height / 2 ) - 14;
            _batteryGraphic.setLocation( x, y );
            _acPowerSupplyGraphic.setLocation( x, y );
            
            _foreground.repaint();
            _background.repaint();
        }
    }
    
    //----------------------------------------------------------------------------
    // ICollidable implementation
    //----------------------------------------------------------------------------
   
    /*
     * @see edu.colorado.phet.faraday.view.ICollidable#getCollisionDetector()
     */
    public CollisionDetector getCollisionDetector() {
        return _collisionDetector;
    }
    
    /*
     * @see edu.colorado.phet.faraday.view.ICollidable#getCollisionBounds()
     */
    public Rectangle[] getCollisionBounds() {
       return _coilGraphic.getCollisionBounds();
    }
    
    //----------------------------------------------------------------------------
    // ApparatusPanel2.ChangeListener implementation
    //----------------------------------------------------------------------------
    
    /*
     * @see edu.colorado.phet.common.view.ApparatusPanel2.ChangeListener#canvasSizeChanged(edu.colorado.phet.common.view.ApparatusPanel2.ChangeEvent)
     */
    public void canvasSizeChanged( ChangeEvent event ) {
        _parentBounds.setBounds( 0, 0, event.getCanvasSize().width, event.getCanvasSize().height );   
    }
    
    //----------------------------------------------------------------------------
    // Inner classes
    //----------------------------------------------------------------------------
    
    /**
     * InteractivityListener is an inner class that handles interactivity.
     *
     * @author Chris Malley (cmalley@pixelzoom.com)
     * @version $Revision$
     */
    private class InteractivityListener extends MouseInputAdapter implements TranslationListener {
        
        private boolean _dragEnabled;
        
        public InteractivityListener() {
            super();
            _dragEnabled = true;
        }

        public void translationOccurred( TranslationEvent e ) {
            if ( _dragEnabled ) {

                boolean inApparatusPanel = _parentBounds.contains( e.getMouseEvent().getPoint() );
                boolean collidesNow = _collisionDetector.collidesNow();
                boolean wouldCollide = _collisionDetector.wouldCollide( e.getDx(), e.getDy() );
                
                if ( !inApparatusPanel || ( !collidesNow && wouldCollide ) ) {
                    // Ignore the translate if the mouse is outside the apparatus panel or 
                    // if the tanslate would result in a collision.
                    _dragEnabled = false;
                }
                else {
                    // Translate if the mouse cursor is inside the parent component.
                    double x = _electromagnetModel.getX() + e.getDx();
                    double y = _electromagnetModel.getY() + e.getDy();
                    _electromagnetModel.setLocation( x, y );
                }
            }
        }
        
        public void mouseDragged( MouseEvent event ) {
            if ( !_dragEnabled && contains( event.getPoint() ) ) {
                _dragEnabled = true;
            }
        }
        
        public void mouseReleased( MouseEvent event ) {
            _dragEnabled = true;
        }
    }
}
