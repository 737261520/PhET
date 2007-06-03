/* Copyright 2007, University of Colorado */
package edu.colorado.phet.energyskatepark.view;

import edu.colorado.phet.common.piccolophet.PhetPCanvas;
import edu.colorado.phet.common.piccolophet.event.PDebugKeyHandler;
import edu.colorado.phet.common.piccolophet.event.PanZoomWorldKeyHandler;
import edu.colorado.phet.energyskatepark.EnergySkateParkModule;
import edu.colorado.phet.energyskatepark.SkaterCharacter;
import edu.colorado.phet.energyskatepark.model.*;
import edu.colorado.phet.energyskatepark.model.physics.ControlPointParametricFunction2D;
import edu.colorado.phet.energyskatepark.view.piccolo.EnergySkateParkRootNode;
import edu.colorado.phet.energyskatepark.view.piccolo.SkaterNode;
import edu.colorado.phet.energyskatepark.view.piccolo.SplineNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: Sam Reid
 * Date: Sep 21, 2005
 * Time: 3:06:51 AM
 */

public class EnergySkateParkSimulationPanel extends PhetPCanvas implements EnergySkateParkSplineEnvironment {
    private EnergySkateParkModule module;
    private EnergySkateParkModel energySkateParkModel;
    private MultiKeyHandler multiKeyHandler = new MultiKeyHandler();
    private EnergySkateParkRootNode rootNode;
    private double matchThresholdWorldCoordinates = 1.5 * 0.33;
    private ArrayList listeners = new ArrayList();
    public static final int NUM_CUBIC_SPLINE_SEGMENTS = 25;

    public EnergySkateParkSimulationPanel( EnergySkateParkModule module ) {
        super( new Rectangle2D.Double( 0, -1, 15, 10 ) );
        this.module = module;
        this.energySkateParkModel = module.getEnergySkateParkModel();
        this.rootNode = new EnergySkateParkRootNode( module, this );
        setPhetRootNode( rootNode );
        addFocusRequest();
        addKeyHandling();
        addKeyListener( new PanZoomWorldKeyHandler( this ) );
        energySkateParkModel.addEnergyModelListener( new EnergySkateParkModel.EnergyModelListenerAdapter() {
            public void preStep( double dt ) {
                updateThrust();
            }
        } );
        addKeyListener( new PDebugKeyHandler() );
        addKeyListener( new KeyListener() {
            public void keyPressed( KeyEvent e ) {
                if( e.getKeyCode() == KeyEvent.VK_F ) {
                    toggleFullScreen();
                }
            }

            public void keyReleased( KeyEvent e ) {
            }

            public void keyTyped( KeyEvent e ) {
            }
        } );
        addMouseListener( new MouseListener() {
            public void mouseClicked( MouseEvent e ) {
            }

            public void mouseEntered( MouseEvent e ) {
            }

            public void mouseExited( MouseEvent e ) {
            }

            public void mousePressed( MouseEvent e ) {
                requestFocus();
            }

            public void mouseReleased( MouseEvent e ) {
            }
        } );
        addMouseListener( new MouseAdapter() {
            public void mouseReleased( MouseEvent e ) {
                if( energySkateParkModel.getGravity() != EnergySkateParkModel.G_SPACE ) {
                    new BumpUpSplines( energySkateParkModel ).bumpUpSplines();
                }
            }
        } );
    }

    protected void updateScale() {
        super.updateScale();
        if( rootNode != null ) {
            rootNode.updateScale();
        }
    }

    public void paintComponent( Graphics g ) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
        super.paintComponent( g );
    }

    private void toggleFullScreen() {
        getEnergyConservationModule().getModulePanel().setFullScreen( !getEnergyConservationModule().getModulePanel().isFullScreen() );
    }

    private void addFocusRequest() {
        addMouseListener( new MouseAdapter() {
            public void mousePressed( MouseEvent e ) {
                requestFocus();
            }
        } );
    }

    private void addKeyHandling() {
        addKeyListener( new KeyListener() {
            public void keyPressed( KeyEvent e ) {
                EnergySkateParkSimulationPanel.this.keyPressed( e );
            }

            public void keyReleased( KeyEvent e ) {
                EnergySkateParkSimulationPanel.this.keyReleased( e );
            }

            public void keyTyped( KeyEvent e ) {
                EnergySkateParkSimulationPanel.this.keyTyped( e );
            }
        } );
    }

    private void debugScreenSize() {
        System.out.println( "getSize( ) = " + getSize() );
    }

    private void updateThrust() {
        if( energySkateParkModel.getNumBodies() > 0 ) {
            Body body = energySkateParkModel.getBody( 0 );
            double xThrust = 0.0;
            double yThrust = 0.0;
            int thrustValue = 15;
            if( multiKeyHandler.isPressed( KeyEvent.VK_RIGHT ) ) {
                xThrust = thrustValue;
            }
            else if( multiKeyHandler.isPressed( KeyEvent.VK_LEFT ) ) {
                xThrust = -thrustValue;
            }
            if( multiKeyHandler.isPressed( KeyEvent.VK_UP ) ) {
                yThrust = thrustValue;
            }
            else if( multiKeyHandler.isPressed( KeyEvent.VK_DOWN ) ) {
                yThrust = -thrustValue;
            }
            body.setThrust( xThrust, yThrust );
        }
    }

    public void addSplineGraphic( SplineNode splineNode ) {
        rootNode.addSplineGraphic( splineNode );
    }

    private void removeSkater() {
        if( getEnergySkateParkModel().getNumBodies() > 1 ) {
            energySkateParkModel.removeBody( 1 );
        }
    }

    private void addSkater() {
        Body body = module.createBody();
        module.returnSkater( body );
        energySkateParkModel.addBody( body );
    }

    public void addSkaterNode( SkaterNode skaterNode ) {
        rootNode.addSkaterNode( skaterNode );
    }

    private void printControlPoints() {
        energySkateParkModel.getSpline( 0 ).printControlPointCode();
    }

    public SplineMatch proposeMatch( SplineNode splineNode, final Point2D toMatch ) {
        ArrayList matches = new ArrayList();
        for( int i = 0; i < numSplineGraphics(); i++ ) {
            SplineNode target = splineGraphicAt( i );
            PNode startNode = target.getControlPointGraphic( 0 );
            double dist = distance( toMatch, startNode );

            if( dist < matchThresholdWorldCoordinates && ( splineNode != target ) ) {
                SplineMatch match = new SplineMatch( target, 0 );
                matches.add( match );
            }

            PNode endNode = target.getControlPointGraphic( target.numControlPointGraphics() - 1 );
            double distEnd = distance( toMatch, endNode );
            if( distEnd < matchThresholdWorldCoordinates && splineNode != target ) {
                SplineMatch match = new SplineMatch( target, target.numControlPointGraphics() - 1 );
                matches.add( match );
            }
        }
        Collections.sort( matches, new Comparator() {
            public int compare( Object o1, Object o2 ) {
                SplineMatch a = (SplineMatch)o1;
                SplineMatch b = (SplineMatch)o2;
                return Double.compare( distance( toMatch, a.getTarget() ), distance( toMatch, b.getTarget() ) );
            }
        } );
        if( matches.size() == 0 ) {
            return null;
        }
        return (SplineMatch)matches.get( 0 );
    }

    private double distance( Point2D toMatch, PNode startNode ) {
        return startNode.getFullBounds().getCenter2D().distance( toMatch );
    }

    private SplineNode splineGraphicAt( int i ) {
        return rootNode.splineGraphicAt( i );
    }

    private int numSplineGraphics() {
        return rootNode.numSplineGraphics();
    }

    public void attach( SplineNode splineNode, int index, SplineMatch match ) {
        TraversalState origState = getEnergySkateParkModel().getBody( 0 ).getTraversalState();
        boolean change = false;
        boolean rollerCoaster = getRollerCoaster( splineNode.getSpline(), match.getEnergySkateParkSpline() );
        if( getEnergySkateParkModel().getBody( 0 ).getSpline() == splineNode.getParametricFunction2D() || getEnergySkateParkModel().getBody( 0 ).getSpline() == match.getSplineGraphic().getParametricFunction2D() ) {
            change = true;
        }
        //delete both of those, add one new parent.
        removeSpline( splineNode );
        removeSpline( match.getSplineGraphic() );

        PreFabSplines.CubicSpline spline = new PreFabSplines.CubicSpline();
        ControlPointParametricFunction2D a = splineNode.getSpline().getParametricFunction2D();
        EnergySkateParkSpline b = match.getEnergySkateParkSpline();
        if( index == 0 ) {
            for( int i = a.numControlPoints() - 1; i >= 0; i-- ) {
                spline.addControlPoint( a.controlPointAt( i ) );
            }
        }
        else {
            for( int i = 0; i < a.numControlPoints(); i++ ) {
                spline.addControlPoint( a.controlPointAt( i ) );
            }
        }
        if( match.matchesBeginning() ) {
            for( int i = 1; i < b.numControlPoints(); i++ ) {
                spline.addControlPoint( b.controlPointAt( i ) );
            }
        }
        else if( match.matchesEnd() ) {
            for( int i = b.numControlPoints() - 2; i >= 0; i-- ) {
                spline.addControlPoint( b.controlPointAt( i ) );
            }
        }
        EnergySkateParkSpline energySkateParkSpline = new EnergySkateParkSpline( spline.getControlPoints() );
        energySkateParkSpline.setRollerCoasterMode( rollerCoaster );
        energySkateParkModel.addSplineSurface( energySkateParkSpline );
        addSplineGraphic( new SplineNode( this, energySkateParkSpline, this ) );
        System.out.println( "change = " + change );
        if( change ) {
            TraversalState traversalState = energySkateParkModel.getBody( 0 ).getBestTraversalState( origState );
            energySkateParkModel.getBody( 0 ).setSpline( energySkateParkModel.getEnergySkateParkSpline( traversalState.getParametricFunction2D() ), traversalState.isTop(), traversalState.getAlpha() );
        }
    }

    private boolean getRollerCoaster( EnergySkateParkSpline s1, EnergySkateParkSpline s2 ) {
        return s1.numControlPoints() > s2.numControlPoints() ? s1.isRollerCoasterMode() : s2.isRollerCoasterMode();
    }

    private void removeSplineGraphic( SplineNode splineNode ) {
        rootNode.removeSplineNode( splineNode );
    }

    public EnergySkateParkModel getEnergySkateParkModel() {
        return energySkateParkModel;
    }

    public void removeSpline( SplineNode splineNode ) {
        removeSplineGraphic( splineNode );
        energySkateParkModel.removeSplineSurface( splineNode.getSpline() );
    }

    public EnergySkateParkModule getEnergyConservationModule() {
        return module;
    }

    public void reset() {
        rootNode.reset();
        multiKeyHandler.clear();
    }

    public void keyPressed( KeyEvent e ) {
        multiKeyHandler.keyPressed( e );
        if( hasFocus() ) {
            if( e.getKeyCode() == KeyEvent.VK_P ) {
                System.out.println( "spline.getSegmentPath().getLength() = " + energySkateParkModel.getSpline( 0 ).numControlPoints() );
                printControlPoints();
            }
            else if( e.getKeyCode() == KeyEvent.VK_A ) {
                addSkater();
            }
            else if( e.getKeyCode() == KeyEvent.VK_R ) {
                removeSkater();
            }
            else if( e.getKeyCode() == KeyEvent.VK_D ) {
                removeSkater();
                debugScreenSize();
            }
        }
    }

    public void keyReleased( KeyEvent e ) {
        multiKeyHandler.keyReleased( e );
    }

    public void keyTyped( KeyEvent e ) {
        multiKeyHandler.keyTyped( e );
    }

    public boolean isMeasuringTapeVisible() {
        return rootNode.isMeasuringTapeVisible();
    }

    public void setMeasuringTapeVisible( boolean selected ) {
        rootNode.setMeasuringTapeVisible( selected );
    }

    public boolean isPieChartVisible() {
        return rootNode.isPieChartVisible();
    }

    public void setPieChartVisible( boolean selected ) {
        rootNode.setPieChartVisible( selected );
    }

    public EnergySkateParkRootNode getRootNode() {
        return rootNode;
    }

    public void setZeroPointVisible( boolean selected ) {
        rootNode.setZeroPointVisible( selected );
    }

    public boolean isZeroPointVisible() {
        return rootNode.isZeroPointVisible();
    }

    public void dragSplineSurface( PInputEvent event, EnergySkateParkSpline createdSurface ) {
        SplineNode splineNode = getSplineGraphic( createdSurface );
        PDimension delta = event.getCanvasDelta();
        rootNode.screenToWorld( delta );
        splineNode.processExternalDragEvent( event, delta.width, delta.height );
    }

    public SplineNode getSplineGraphic( EnergySkateParkSpline createdSurface ) {
        for( int i = 0; i < numSplineGraphics(); i++ ) {
            if( splineGraphicAt( i ).getSpline() == createdSurface ) {
                return splineGraphicAt( i );
            }
        }
        return null;
    }

//    public void setSkaterCharacter( SkaterCharacter skaterCharacter ) {
//        rootNode.setSkaterCharacter( skaterCharacter );
//    }

    public void setEnergyErrorVisible( boolean selected ) {
        rootNode.setEnergyErrorVisible( selected );
    }

    public boolean isEnergyErrorVisible() {
        return rootNode.isEnergyErrorVisible();
    }

    public static interface Listener {
        void zoomChanged();
    }

    public void addListener( Listener listener ) {
        listeners.add( listener );
    }

    public void fireZoomEvent() {
        for( int i = 0; i < listeners.size(); i++ ) {
            Listener listener = (Listener)listeners.get( i );
            listener.zoomChanged();
        }
    }
}
