// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.fractions.intro.intro.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;

import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.piccolophet.event.CursorHandler;
import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * @author Sam Reid
 */
public class ShapeElement extends PNode {
    //characteristic length
    public static double DIM = 20;

    public ShapeElement( ArrayList<Shape> unfilled, ArrayList<Shape> filled, final Property<ChosenRepresentation> chosenRepresentation, final ChosenRepresentation representation ) {
        for ( Shape shape : unfilled ) {
            addChild( new PhetPPath( shape, Color.white, new BasicStroke( 1 ), Color.gray ) );
        }
        for ( Shape shape : filled ) {
            addChild( new PhetPPath( shape, Color.green, new BasicStroke( 1 ), Color.gray ) );
        }

        addInputEventListener( new CursorHandler() );
        addInputEventListener( new PBasicInputEventHandler() {
            @Override public void mousePressed( PInputEvent event ) {
                chosenRepresentation.set( representation );
            }
        } );
    }
}
