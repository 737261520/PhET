/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.molecularreactions.model;

/**
 * MoleculeAB
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class MoleculeAB extends CompositeMolecule {

    public MoleculeAB( SimpleMolecule[] components, Bond[] bonds ) {
        super( components, bonds );
    }

    public MoleculeA getMoleculeA() {
        return (MoleculeA)getMoleculeOfType( MoleculeA.class );
    }

    public MoleculeB getMoleculeB() {
        return (MoleculeB)getMoleculeOfType( MoleculeB.class );
    }

    private Molecule getMoleculeOfType( Class moleculeType ) {
        Molecule m = null;
        if( moleculeType.isInstance( getComponentMolecules()[0]) ) {
            m = getComponentMolecules()[0];
        }
        else if( moleculeType.isInstance( getComponentMolecules()[1]) ) {
            m = getComponentMolecules()[1];
        }
        else {
            throw new RuntimeException( "internal error" );
        }
        return m;
    }
}
