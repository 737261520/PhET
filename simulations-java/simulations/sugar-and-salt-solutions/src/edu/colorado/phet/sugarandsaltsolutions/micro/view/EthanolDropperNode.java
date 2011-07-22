// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.sugarandsaltsolutions.micro.view;

import java.awt.image.BufferedImage;

import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform;
import edu.colorado.phet.sugarandsaltsolutions.common.view.DispenserNode;
import edu.colorado.phet.sugarandsaltsolutions.micro.model.MicroModel;
import edu.colorado.phet.sugarandsaltsolutions.micro.model.ethanol.EthanolDropper;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;

import static edu.colorado.phet.common.phetcommon.view.util.BufferedImageUtils.multiScaleToHeight;
import static edu.colorado.phet.sugarandsaltsolutions.SugarAndSaltSolutionsResources.Images.*;

/**
 * Sugar dispenser which can be rotated to pour out an endless supply of sugar.
 *
 * @author Sam Reid
 */
public class EthanolDropperNode extends DispenserNode<MicroModel, EthanolDropper> {
    public EthanolDropperNode( final ModelViewTransform transform, final EthanolDropper model, double beakerHeight ) {
        super( transform, model, beakerHeight );

        //Create images to use for full/empty dropper
        int height = 250;
        final BufferedImage fullImage = multiScaleToHeight( DROPPER, height );
        final BufferedImage emptyImage = multiScaleToHeight( DROPPER, height );

        //Images for the button on the dropper
        int buttonHeight = 35;
        final BufferedImage buttonUnpressed = multiScaleToHeight( BUTTON_UNPRESSED, buttonHeight );
        final BufferedImage buttonPressed = multiScaleToHeight( BUTTON_PRESSED, buttonHeight );

        imageNode.addChild( new PImage( buttonUnpressed ) {{
            //Couldn't get PNode centering to work dynamically, so I hard coded it.
            //TODO: can this be done with setOffset(parent.getWidth/2 - getWidth/2)
            setOffset( 9, 20 );

            //Toggle release of the ethanol
            addInputEventListener( new PBasicInputEventHandler() {
                @Override public void mousePressed( PInputEvent event ) {
                    model.setDropperHeight( transform.viewToModelDeltaY( fullImage.getHeight() ) );
                    model.startDropping();
                }

                @Override public void mouseReleased( PInputEvent event ) {
                    model.pressing.set( false );
                }
            } );
            model.pressing.addObserver( new VoidFunction1<Boolean>() {
                public void apply( Boolean pressed ) {
                    setImage( pressed ? buttonPressed : buttonUnpressed );
                }
            } );
        }} );

        //Hide the sugar dispenser if it is not enabled (selected by the user)
        model.enabled.addObserver( new VoidFunction1<Boolean>() {
            public void apply( Boolean enabled ) {
                setVisible( enabled );
            }
        } );

        //Switch between the empty and full images based on whether the user is allowed to add more salt
        model.moreAllowed.addObserver( new VoidFunction1<Boolean>() {
            public void apply( Boolean moreAllowed ) {
                imageNode.setImage( moreAllowed ? fullImage : emptyImage );
            }
        } );

        //Have to update the transform once after the image size changes (since it goes from null to non-null) in the auto-callback above
        //This must be called twice, otherwise it appears at an offset of half its width.
        //TODO: can we make updateTransform work properly so it doesn't need to be called twice?
        updateTransform();
        updateTransform();
    }
}