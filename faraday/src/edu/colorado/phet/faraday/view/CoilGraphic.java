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
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;

import edu.colorado.phet.common.model.BaseModel;
import edu.colorado.phet.common.util.SimpleObserver;
import edu.colorado.phet.common.view.phetgraphics.CompositePhetGraphic;
import edu.colorado.phet.common.view.phetgraphics.PhetGraphic;
import edu.colorado.phet.common.view.phetgraphics.PhetShapeGraphic;
import edu.colorado.phet.faraday.model.AbstractCoil;
import edu.colorado.phet.faraday.model.Electron;
import edu.colorado.phet.faraday.model.QuadBezierSpline;


/**
 * CoilGraphic is the graphical representation of a coil of wire.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class CoilGraphic implements SimpleObserver {

    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------
    
    // Loop parameters
    private static final Color FOREGROUND_COLOR = new Color( 153, 102, 51 ); // light brown
    private static final Color MIDDLEGROUND_COLOR = new Color( 92, 52, 12 ); // dark brown
    private static final Color BACKGROUND_COLOR = new Color( 40, 23, 3 ); // really dark brown
    private static final int WIRE_WIDTH = 16;
    private static final double LOOP_SPACING_FACTOR = 0.3; // ratio of loop spacing to loop radius
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private Component _component;
    private AbstractCoil _coilModel;
    private BaseModel _baseModel;
    private CompositePhetGraphic _foreground;
    private CompositePhetGraphic _background;
    private boolean _currentAnimationEnabled;
    private Stroke _loopStroke;
    private Color _foregroundColor, _middlegroundColor, _backgroundColor;
    private ArrayList _paths; // array of QuadBezierSpline
    private ArrayList _electrons; // array of Electron
    
    //----------------------------------------------------------------------------
    // Constructors & finalizers
    //----------------------------------------------------------------------------

    /**
     * Sole constructor.
     * 
     * @param component parent Component
     * @param coilModel the coil that this graphic is watching
     */
    public CoilGraphic( Component component, BaseModel baseModel, AbstractCoil coilModel ) {
        assert( component != null );
        assert( coilModel != null );
        
        _component = component;
        _baseModel = baseModel;
        
        _coilModel = coilModel;
        _coilModel.addObserver( this );
        
        RenderingHints hints = new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ); 
        _foreground = new CompositePhetGraphic( _component );
        _background = new CompositePhetGraphic( _component );
        _foreground.setRenderingHints( hints );
        _background.setRenderingHints( hints );
        
        _currentAnimationEnabled = true;  //XXX
        _loopStroke = new BasicStroke( WIRE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL );
        _foregroundColor = FOREGROUND_COLOR;
        _middlegroundColor = MIDDLEGROUND_COLOR;
        _backgroundColor = BACKGROUND_COLOR;
        
        _paths = new ArrayList();
        _electrons = new ArrayList();
        
        update();
    }
    
    /**
     * Finalizes an instance of this type.
     * Call this method prior to releasing all references to an object of this type.
     */
    public void finalize() {
        _coilModel.removeObserver( this );
        _coilModel = null;
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
     * Enables/disables animation of current flow.
     * 
     * @param enabled true to enabled, false to disable
     */
    public void setCurrentAnimationEnabled( boolean enabled ) {
        if ( enabled != _currentAnimationEnabled ) {
            _currentAnimationEnabled = enabled;
            update();
        }
    }
    
    /**
     * Determines whether animation of current flow is enabled.
     * 
     * @return true if enabled, false if disabled
     */
    public boolean isCurrentAnimationEnabled() {
        return _currentAnimationEnabled;
    }

    /**
     * Sets the colors used for the loops.
     * Three colors are combined in various gradients to provide a 3D look.
     * <p>
     * Colors are changed only if their corresponding parameter is not null.
     * For example, to change only the foreground color, use setColor(Color.RED,null,null).
     * 
     * @param foregroundColor the foreground color
     * @param middlegroundColor the middleground color
     * @param backgroundColor the background color
     */
    public void setColors( Color foregroundColor, Color middlegroundColor, Color backgroundColor ) {
        if ( foregroundColor != null ) {
            _foregroundColor = foregroundColor;
        }
        if ( middlegroundColor != null ) {
            _middlegroundColor = middlegroundColor;
        }
        if ( backgroundColor != null ) {
            _backgroundColor = backgroundColor;
        }
        update();
    }
    
    /**
     * Gets the foreground color used to draw the coil.
     * 
     * @return the foreground color
     */
    public Color getForegroundColor() {
        return _foregroundColor;
    }
    
    /**
     * Gets the middleground color used to draw the coil.
     * 
     * @return the middleground color
     */
    public Color getMiddlegroundColor() {
        return _middlegroundColor;
    }
    
    /**
     * Gets the background color used to draw the coil.
     * 
     * @return the background color
     */
    public Color getBackgroundColor() {
        return _backgroundColor;
    }
    
    //----------------------------------------------------------------------------
    // SimpleObserver implementation
    //----------------------------------------------------------------------------
    
    /*
     * @see edu.colorado.phet.common.util.SimpleObserver#update()
     */
    public void update() {
        if ( _foreground.isVisible() || _background.isVisible() ) {
            updateGraphics();
        }
    }
    
    private void updateGraphics() {
        
        // Removing any existing graphics components.
        _foreground.clear();
        _background.clear();
        
        // Clear the parametric path list.
        _paths.clear();
        
        // Remove electrons from the model.
        for ( int i = 0; i < _electrons.size(); i ++ ) {
            Electron electron = (Electron) _electrons.get(i);
            electron.removeAllObservers();
            _baseModel.removeModelElement( electron );
        }
        _electrons.clear();
        
        int numberOfLoops = _coilModel.getNumberOfLoops();
        double radius = _coilModel.getRadius();
        
        // Loop spacing
        int loopSpacing = (int)(radius * LOOP_SPACING_FACTOR );
        
        // Center of all loops should remain fixed.
        int firstLoopCenter = -( loopSpacing * (numberOfLoops - 1) / 2 );
        
        // Back of loops
        for ( int i = 0; i < numberOfLoops; i++ ) {
            
            int xOffset = firstLoopCenter + ( i * loopSpacing );
            
            // Back bottom
            {  
                Point end1 = new Point( (int)(radius * .25) + xOffset, 0 ); // upper
                Point end2 = new Point( xOffset, (int) radius ); // lower
                Point control = new Point( (int)(radius * .35) + xOffset, (int)(radius * 1.2) );
                QuadCurve2D.Double curve = new QuadCurve2D.Double();
                curve.setCurve( end1, control, end2 );
                
                // Vertical gradient, upper to lower
                Paint paint = new GradientPaint( 0, (int)(radius * 0.92), _backgroundColor, 0, (int)(radius), _middlegroundColor );
                
                PhetShapeGraphic shapeGraphic = new PhetShapeGraphic( _component );
                shapeGraphic.setShape( curve );
                shapeGraphic.setStroke( _loopStroke );
                shapeGraphic.setBorderPaint( paint );
                _background.addGraphic( shapeGraphic );
            }
            
            if ( i != 0 ) {
                // Back top
                Point end1 = new Point( -loopSpacing + xOffset, (int)-radius ); // upper
                Point end2 = new Point( (int)(radius * .25) + xOffset, 0 ); // lower
                Point control = new Point( (int)(radius * .15) + xOffset, (int)(-radius * 1.20));
                QuadCurve2D.Double curve = new QuadCurve2D.Double();
                curve.setCurve( end1, control, end2 );
                
                // Diagonal gradient, upper left to lower right.
                Paint paint = new GradientPaint( (int)(end1.x + (radius * .1)), (int)-(radius), _middlegroundColor, xOffset, (int)-(radius * 0.92), _backgroundColor );
                
                PhetShapeGraphic shapeGraphic = new PhetShapeGraphic( _component );
                shapeGraphic.setShape( curve );
                shapeGraphic.setStroke( _loopStroke );
                shapeGraphic.setBorderPaint( paint );
                _background.addGraphic( shapeGraphic );
            }
            else {
                // Back top (left-most)
                {
                    Point end1 = new Point( -loopSpacing / 2 + xOffset, (int) -radius ); // upper
                    Point end2 = new Point( (int) ( radius * .25 ) + xOffset, 0 ); // lower
                    Point control = new Point( (int) ( radius * .15 ) + xOffset, (int) ( -radius * .70 ) );
                    QuadCurve2D.Double curve = new QuadCurve2D.Double();
                    curve.setCurve( end1, control, end2 );
                    
                    Paint paint = _backgroundColor;
                    
                    PhetShapeGraphic shapeGraphic = new PhetShapeGraphic( _component );
                    shapeGraphic.setShape( curve );
                    shapeGraphic.setStroke( _loopStroke );
                    shapeGraphic.setBorderPaint( paint );
                    _background.addGraphic( shapeGraphic );
                }

                // Left connection wire
                {
                    Point end1 = new Point( -loopSpacing / 2 + xOffset, (int) -radius ); // lower
                    Point end2 = new Point( end1.x - 15, end1.y - 40 ); // upper
                    Point control = new Point( end1.x - 20, end1.y - 20 );
                    QuadCurve2D.Double curve = new QuadCurve2D.Double();
                    curve.setCurve( end1, control, end2 );
                    
                    // Horizontal gradient, left to right.
                    Paint paint = new GradientPaint( end2.x, 0, _middlegroundColor, end1.x, 0, _backgroundColor );
                    
                    PhetShapeGraphic shapeGraphic = new PhetShapeGraphic( _component );
                    shapeGraphic.setShape( curve );
                    shapeGraphic.setStroke( _loopStroke );
                    shapeGraphic.setBorderPaint( paint );
                    _background.addGraphic( shapeGraphic );
                }
            }
        }
        
        // Front of loops
        for ( int i = numberOfLoops - 1; i >= 0; i-- ) {
            
            int xOffset = firstLoopCenter + ( i * loopSpacing );;
            
            // Right connection wire
            if ( i == numberOfLoops - 1 ) {
                Point end1 = new Point( xOffset, (int) -radius ); // lower
                Point end2 = new Point( end1.x + 15, end1.y - 40 ); // upper
                Point control = new Point( end1.x + 20, end1.y - 20 );
                QuadCurve2D.Double curve = new QuadCurve2D.Double();
                curve.setCurve( end1, control, end2 );
                
                Paint paint = _middlegroundColor;
                
                PhetShapeGraphic shapeGraphic = new PhetShapeGraphic( _component );
                shapeGraphic.setShape( curve );
                shapeGraphic.setStroke( _loopStroke );
                shapeGraphic.setBorderPaint( paint );
                _foreground.addGraphic( shapeGraphic );
            }
            
            // Horizontal gradient, left to right
            Paint paint = new GradientPaint( (int)(-radius * .25) + xOffset, 0, _foregroundColor, (int)(-radius * .15) + xOffset, 0, _middlegroundColor );
            
            // Front top
            {
                Point end1 = new Point( xOffset, (int) -radius ); // upper
                Point end2 = new Point( (int) ( -radius * .25 ) + xOffset, 0 ); // lower
                Point control = new Point( (int) ( -radius * .25 ) + xOffset, (int) ( -radius * 0.80) );
                QuadCurve2D.Double curve = new QuadCurve2D.Double();
                curve.setCurve( end1, control, end2 );
                
                QuadBezierSpline path = new QuadBezierSpline( end1, control, end2 );
                _paths.add( path );
                
                PhetShapeGraphic shapeGraphic = new PhetShapeGraphic( _component );
                shapeGraphic.setShape( curve );
                shapeGraphic.setStroke( _loopStroke );
                shapeGraphic.setBorderPaint( paint );
                _foreground.addGraphic( shapeGraphic );
            }

            //  Front bottom
            {
                Point end1 = new Point( (int) ( -radius * .25 ) + xOffset, 0 ); // upper
                Point end2 = new Point( xOffset, (int) radius ); // lower
                Point control = new Point( (int) ( -radius * .25 ) + xOffset, (int) ( radius * 0.80 ) );
                QuadCurve2D.Double curve = new QuadCurve2D.Double();
                curve.setCurve( end1, control, end2 );
                
                QuadBezierSpline path = new QuadBezierSpline( end1, control, end2 );
                _paths.add( path );
                
                PhetShapeGraphic shapeGraphic = new PhetShapeGraphic( _component );
                shapeGraphic.setShape( curve );
                shapeGraphic.setStroke( _loopStroke );
                shapeGraphic.setBorderPaint( paint );
                _foreground.addGraphic( shapeGraphic );
            }
        }
        
        // Add electrons.
        if ( _currentAnimationEnabled )
        {
            int numberOfElectrons = (int) ( radius / 20 );
            for ( int j = 0; j < _paths.size(); j++ ) {
                for ( int i = 0; i < numberOfElectrons; i++ ) {
                    Electron electron = new Electron();
                    electron.setCurves( _paths );
                    electron.setCurveLocation( j, i / (double) numberOfElectrons );
                    _electrons.add( electron );
                    _baseModel.addModelElement( electron );

                    ElectronGraphic electronGraphic = new ElectronGraphic( _component, electron );
                    _foreground.addGraphic( electronGraphic );
                }
            }
        }
        
        _foreground.repaint();
        _background.repaint();
    }

}
