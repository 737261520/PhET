// Copyright 2002-2011, University of Colorado

package edu.colorado.phet.buildamolecule;

import java.util.HashMap;
import java.util.Map;

import edu.colorado.phet.chemistry.model.Atom;

/**
 * A collection of localized strings used by this simulations.
 * We load all strings as statics so that we will be warned at startup time of any missing strings.
 */
public class BuildAMoleculeStrings {

    /* not intended for instantiation */
    private BuildAMoleculeStrings() {
    }

    public static final String TITLE_MAKE_MOLECULE = BuildAMoleculeResources.getString( "title.makeMolecule" );
    public static final String TITLE_COLLECT_MULTIPLE = BuildAMoleculeResources.getString( "title.collectMultiple" );
    public static final String TITLE_LARGER_MOLECULES = BuildAMoleculeResources.getString( "title.largerMolecules" );

    public static final String ATOM_HYDROGEN = BuildAMoleculeResources.getString( "atom.hydrogen" );
    public static final String ATOM_OXYGEN = BuildAMoleculeResources.getString( "atom.oxygen" );
    public static final String ATOM_CARBON = BuildAMoleculeResources.getString( "atom.carbon" );
    public static final String ATOM_NITROGEN = BuildAMoleculeResources.getString( "atom.nitrogen" );
    public static final String ATOM_FLUORINE = BuildAMoleculeResources.getString( "atom.fluorine" );
    public static final String ATOM_CHLORINE = BuildAMoleculeResources.getString( "atom.chlorine" );
    public static final String ATOM_BORON = BuildAMoleculeResources.getString( "atom.boron" );
    public static final String ATOM_SULPHUR = BuildAMoleculeResources.getString( "atom.sulphur" );
    public static final String ATOM_SILICON = BuildAMoleculeResources.getString( "atom.silicon" );
    public static final String ATOM_PHOSPHORUS = BuildAMoleculeResources.getString( "atom.phosphorus" );
    public static final String ATOM_IODINE = BuildAMoleculeResources.getString( "atom.iodine" );
    public static final String ATOM_BROMINE = BuildAMoleculeResources.getString( "atom.bromine" );

    public static final String KIT_LABEL = BuildAMoleculeResources.getString( "kit.label" );
    public static final String KIT_RESET = BuildAMoleculeResources.getString( "kit.resetKit" );

    public static final String JMOL_3D_SPACE_FILLING = BuildAMoleculeResources.getString( "3d.spaceFilling" );
    public static final String JMOL_3D_BALL_AND_STICK = BuildAMoleculeResources.getString( "3d.ballAndStick" );
    public static final String JMOL_3D_LOADING = BuildAMoleculeResources.getString( "3d.loading" );

    public static final String COLLECTION_AREA_YOUR_MOLECULE_COLLECTION = BuildAMoleculeResources.getString( "collection.yourMoleculeCollection" );
    public static final String COLLECTION_ALL_FILLED = BuildAMoleculeResources.getString( "collection.allFilled" );
    public static final String COLLECTION_TRY_WITH_DIFFERENT_MOLECULES = BuildAMoleculeResources.getString( "collection.tryWithDifferentMolecules" );
    public static final String COLLECTION_SINGLE_FORMAT = BuildAMoleculeResources.getString( "collection.single.format" );
    public static final String COLLECTION_MULTIPLE_GOAL_FORMAT = BuildAMoleculeResources.getString( "collection.multiple.goalFormat" );
    public static final String COLLECTION_MULTIPLE_QUANTITY_FORMAT = BuildAMoleculeResources.getString( "collection.multiple.quantityFormat" );
    public static final String COLLECTION_MULTIPLE_QUANTITY_EMPTY = BuildAMoleculeResources.getString( "collection.multiple.quantityEmpty" );

    private static final Map<String, String> atomStringMap = new HashMap<String, String>();

    static {
        atomStringMap.put( "H", ATOM_HYDROGEN );
        atomStringMap.put( "O", ATOM_OXYGEN );
        atomStringMap.put( "C", ATOM_CARBON );
        atomStringMap.put( "N", ATOM_NITROGEN );
        atomStringMap.put( "F", ATOM_FLUORINE );
        atomStringMap.put( "Cl", ATOM_CHLORINE );
        atomStringMap.put( "B", ATOM_BORON );
        atomStringMap.put( "S", ATOM_SULPHUR );
        atomStringMap.put( "Si", ATOM_SILICON );
        atomStringMap.put( "P", ATOM_PHOSPHORUS );
        atomStringMap.put( "I", ATOM_IODINE );
        atomStringMap.put( "Br", ATOM_BROMINE );
    }

    public static String getAtomName( Atom atom ) {
        return atomStringMap.get( atom.getSymbol() );
    }
}
