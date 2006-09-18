package edu.colorado.phet.cck.piccolo_cck;

import edu.colorado.phet.cck.CCKImageSuite;
import edu.colorado.phet.cck.model.CCKModel;
import edu.colorado.phet.cck.model.components.Resistor;
import edu.colorado.phet.common_cck.util.SimpleObserver;
import edu.colorado.phet.piccolo.PhetPNode;
import edu.colorado.phet.piccolo.event.CursorHandler;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PDimension;

import java.awt.geom.Line2D;

/**
 * User: Sam Reid
 * Date: Sep 17, 2006
 * Time: 7:49:22 PM
 * Copyright (c) Sep 17, 2006 by Sam Reid
 */

public class ResistorNode extends PhetPNode {
    private PImage pImage;
    private CCKModel model;
    private Resistor resistor;

    public ResistorNode( final CCKModel model, final Resistor resistor ) {
        this.model = model;
        this.resistor = resistor;
        pImage = new PImage( CCKImageSuite.getInstance().getLifelikeSuite().getResistorImage() );
        pImage.scale( 0.01 );
        addChild( pImage );
        update();
        resistor.addObserver( new SimpleObserver() {
            public void update() {
                ResistorNode.this.update();
            }
        } );
        addInputEventListener( new CursorHandler() );
        addInputEventListener( new PBasicInputEventHandler() {
            public void mouseDragged( PInputEvent event ) {
                PDimension delta = event.getDeltaRelativeTo( ResistorNode.this );
                resistor.translate( delta.width, delta.height );
            }

            public void mousePressed( PInputEvent event ) {
                model.getCircuit().setSelection( resistor );
            }
        } );
    }

    private void update() {
        Line2D.Double wireLine = new Line2D.Double( resistor.getStartPoint(), resistor.getEndPoint() );
        double resistorLength = resistor.getStartPoint().distance( resistor.getEndPoint() );
        double imageLength = pImage.getFullBounds().getWidth();
        double scale = resistorLength / imageLength;
        setScale( scale );
        setOffset( resistor.getStartPoint() );
        translate( 0, -getFullBounds().getHeight() / 4 );
//        setOffset( Math.random() * 1, Math.random() * 1 );
    }

}
