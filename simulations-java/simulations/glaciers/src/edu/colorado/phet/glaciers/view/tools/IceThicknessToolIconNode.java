/* Copyright 2008, University of Colorado */

package edu.colorado.phet.glaciers.view.tools;

import java.awt.geom.Point2D;

import edu.colorado.phet.glaciers.model.AbstractTool;
import edu.colorado.phet.glaciers.model.IToolProducer;
import edu.colorado.phet.glaciers.view.GlaciersModelViewTransform;
import edu.colorado.phet.glaciers.view.tools.AbstractToolIconNode.InteractiveToolIconNode;

/**
 * IceThicknessToolIconNode
 */
public class IceThicknessToolIconNode extends InteractiveToolIconNode {
    
    public IceThicknessToolIconNode( IToolProducer toolProducer, GlaciersModelViewTransform mvt  ) {
        super( IceThicknessToolNode.createImage(), toolProducer, mvt );
    }
    
    public AbstractTool createTool( Point2D position ) {
        return getToolProducer().addIceThicknessTool( position );
    }
}