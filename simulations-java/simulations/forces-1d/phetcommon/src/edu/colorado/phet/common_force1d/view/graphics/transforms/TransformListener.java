/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.common_force1d.view.graphics.transforms;


/**
 * Listens for changes in the model or view viewport.
 *
 * @author ?
 * @version $Revision$
 */
public interface TransformListener {
    public void transformChanged( ModelViewTransform2D mvt );
}
