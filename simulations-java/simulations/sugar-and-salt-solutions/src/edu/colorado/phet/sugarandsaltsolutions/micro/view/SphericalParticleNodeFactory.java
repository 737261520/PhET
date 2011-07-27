package edu.colorado.phet.sugarandsaltsolutions.micro.view;

import edu.colorado.phet.common.phetcommon.model.property.ObservableProperty;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform;
import edu.colorado.phet.sugarandsaltsolutions.micro.model.ItemList;
import edu.colorado.phet.sugarandsaltsolutions.micro.model.SphericalParticle;

/**
 * This class observes an ItemList and creates a PNode when an item is added to the model list,
 * and removes it when the item is removed from the model list.
 *
 * @author Sam Reid
 */
public class SphericalParticleNodeFactory implements VoidFunction1<SphericalParticle> {
    private final ItemList<SphericalParticle> list;
    private final ModelViewTransform transform;
    private final MicroCanvas canvas;
    private ObservableProperty<Boolean> showChargeColor;

    public SphericalParticleNodeFactory( ItemList<SphericalParticle> list, ModelViewTransform transform, MicroCanvas canvas, ObservableProperty<Boolean> showChargeColor ) {
        this.list = list;
        this.transform = transform;
        this.canvas = canvas;
        this.showChargeColor = showChargeColor;
    }

    //Create the PNode for the particle, and wire it up to be removed when the particle leaves the model
    public void apply( SphericalParticle particle ) {
        final SphericalParticleNode node = new SphericalParticleNode( transform, particle, showChargeColor );
        canvas.addChild( node );
        list.addItemRemovedListener( new VoidFunction1<SphericalParticle>() {
            public void apply( SphericalParticle sphericalParticle ) {

                //Only remove the node if it corresponded to the node we created
                if ( sphericalParticle == node.getSphericalParticle() ) {
                    list.removeItemRemovedListener( this );
                    canvas.removeChild( node );
                }
            }
        } );
    }
}
