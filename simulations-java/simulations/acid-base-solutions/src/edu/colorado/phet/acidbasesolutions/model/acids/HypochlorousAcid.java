package edu.colorado.phet.acidbasesolutions.model.acids;

import edu.colorado.phet.acidbasesolutions.ABSStrings;
import edu.colorado.phet.acidbasesolutions.ABSSymbols;


public class HypochlorousAcid implements IWeakAcid {
    
    public String getName() {
        return ABSStrings.HYPOCHLOROUS_ACID;
    }

    public String getSymbol() {
        return ABSSymbols.HClO;
    }

    public String getConjugateBaseSymbol() {
        return ABSSymbols.ClO;
    }
    
    public double getStrength() {
        return 2.9E-8;
    }
}
