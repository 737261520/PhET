// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.beerslawlab.beerslaw.view;

import java.awt.geom.Dimension2D;

import edu.colorado.phet.beerslawlab.common.BLLResources;
import edu.colorado.phet.beerslawlab.common.BLLResources.Strings;
import edu.colorado.phet.beerslawlab.common.BLLSimSharing.UserComponents;
import edu.colorado.phet.common.phetcommon.simsharing.messages.UserComponentTypes;
import edu.colorado.phet.common.piccolophet.event.CursorHandler;
import edu.colorado.phet.common.piccolophet.nodes.RulerNode;
import edu.colorado.phet.common.piccolophet.simsharing.SimSharingDragHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * A 2cm ruler, constrained to drag within the bounds of the stage.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class BLLRulerNode extends RulerNode {

    public BLLRulerNode( double length, final Dimension2D stageSize ) {
        super( length, 40, new String[] { "0", "1", "2" }, Strings.UNITS_CENTIMETERS, 9, 12 );
        addInputEventListener( new CursorHandler() );
        addInputEventListener( new SimSharingDragHandler( UserComponents.ruler, UserComponentTypes.sprite ) {
            @Override protected void drag( PInputEvent event ) {
                super.drag( event );
                // entire ruler must be in the stage bounds
                double xOffset = Math.min( Math.max( 0, getXOffset() + event.getDelta().getWidth() ), stageSize.getWidth() - getFullBoundsReference().getWidth() );
                double yOffset = Math.min( Math.max( 0, getYOffset() + event.getDelta().getHeight() ), stageSize.getHeight() - getFullBoundsReference().getHeight() );
                setOffset( xOffset, yOffset );
            }
        } );
    }
}
