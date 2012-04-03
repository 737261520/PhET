// Copyright 2002-2012, University of Colorado
package edu.colorado.phet.fluidpressureandflow.pressure.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.util.ObservableList;
import edu.colorado.phet.common.phetcommon.util.function.Function1;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform;
import edu.colorado.phet.common.piccolophet.event.CursorHandler;
import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.colorado.phet.fluidpressureandflow.pressure.model.ChamberPool;
import edu.colorado.phet.fluidpressureandflow.pressure.model.Mass;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Node that shows a collection of masses for the chamber pool.
 *
 * @author Sam Reid
 */
public class MassesLayer extends PNode {
    public MassesLayer( final ChamberPool pool, final Property<ObservableList<Mass>> massesProperty, final ModelViewTransform transform ) {
        massesProperty.addObserver( new VoidFunction1<ObservableList<Mass>>() {
            @Override public void apply( final ObservableList<Mass> masses ) {
                removeAllChildren();
                Mass dragging = findDragging( masses );
                if ( dragging != null ) {
                    addChild( new DottedLineDropRegion( pool, dragging, transform ) );
                }
                for ( Mass mass : masses ) {
                    addChild( new MassNode( massesProperty, mass, transform ) );
                }
            }

            private Mass findDragging( ObservableList<Mass> masses ) {
                for ( Mass mass : masses ) {
                    if ( mass.dragging ) {
                        return mass;
                    }
                }
                return null;
            }
        } );
    }

    private static class MassNode extends PNode {
        public MassNode( final Property<ObservableList<Mass>> masses, final Mass mass, final ModelViewTransform transform ) {
            addChild( new PhetPPath( transform.modelToView( mass.shape ), Color.gray, new BasicStroke( 2 ), Color.black ) );
            addInputEventListener( new CursorHandler() );
            addInputEventListener( new PBasicInputEventHandler() {
                @Override public void mousePressed( final PInputEvent event ) {
                    masses.set( masses.get().map( new Function1<Mass, Mass>() {
                        @Override public Mass apply( final Mass m ) {
                            return m == mass ? m.withDragging( true ) : m;
                        }
                    } ) );
                }

                @Override public void mouseDragged( final PInputEvent event ) {
                    super.mouseDragged( event );
                    final Dimension2D modelDelta = transform.viewToModelDelta( event.getDelta() );
                    masses.set( masses.get().map( new Function1<Mass, Mass>() {
                        @Override public Mass apply( final Mass mass ) {
                            Mass translatedMass = mass.translate( modelDelta );
//                            if ( translatedMass.shape.getBounds2D().getMinY() < 0 ) {
//                                translatedMass = translatedMass.translate( new Dimension2DDouble( 0, -translatedMass.shape.getBounds2D().getMinY() ) );
//                            }
                            return mass.dragging ? translatedMass : mass;
                        }
                    } ) );
                }

                @Override public void mouseReleased( final PInputEvent event ) {
                    masses.set( masses.get().map( new Function1<Mass, Mass>() {
                        @Override public Mass apply( final Mass m ) {
                            return m.withDragging( false );
                        }
                    } ) );
                }
            } );
        }
    }

    private class DottedLineDropRegion extends PNode {
        public DottedLineDropRegion( ChamberPool pool, final Mass dragging, ModelViewTransform transform ) {
            Shape shape = pool.getLeftOpeningWaterShape();
            Rectangle2D bounds = shape.getBounds2D();
            double maxY = bounds.getMaxY();
            Mass m = dragging.withCenterX( bounds.getCenterX() ).withMinY( maxY );
            addChild( new PhetPPath( transform.modelToView( m.shape ), new Color( 255, 220, 240 ), new BasicStroke( 2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[] { 8, 6 }, 0 ), Color.black ) );
        }
    }
}