// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.moleculepolarity.threeatoms;

import edu.colorado.phet.common.phetcommon.math.ImmutableVector2D;
import edu.colorado.phet.common.phetcommon.model.clock.IClock;
import edu.colorado.phet.moleculepolarity.common.model.MPModel2D;
import edu.colorado.phet.moleculepolarity.common.model.TriatomicMolecule;

/**
 * Model for the "Three Atoms" module.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class ThreeAtomsModel extends MPModel2D {

    public final TriatomicMolecule molecule;

    public ThreeAtomsModel( IClock clock ) {
        //REVIEW - Why the hard-coded non-zero location?  Where do these numbers come from? Should at least document.
        super( clock, new TriatomicMolecule( new ImmutableVector2D( 380, 375 ), 0 ) );
        molecule = (TriatomicMolecule) getMolecule(); // hate to cast, but it facilitates moving shared code to base class
    }
}
