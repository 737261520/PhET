package edu.colorado.phet.acidbasesolutions.model;

import edu.colorado.phet.acidbasesolutions.ABSConstants;
import edu.colorado.phet.acidbasesolutions.ABSStrings;
import edu.colorado.phet.acidbasesolutions.ABSSymbols;


public class WeakBase {
    
    // specific weak bases
    public static final WeakBase AMMONIA = new WeakBase( ABSStrings.AMMONIA, ABSSymbols.NH3, ABSSymbols.NH4_PLUS, 1.8E-5 );
    public static final WeakBase PYRIDINE = new WeakBase( ABSStrings.PYRIDINE, ABSSymbols.C5H5N, ABSSymbols.C5H5NH_PLUS, 1.7E-9 );

    private final String name;
    private final String symbol;
    private final String conjugateAcidSymbol;
    private double strength;
    
    private WeakBase( String name, String symbol, String conjugateAcidSymbol, double strength ) {
        this.name = name;
        this.symbol = symbol;
        this.conjugateAcidSymbol = conjugateAcidSymbol;
        this.strength = strength;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getConjugateAcidSymbolSymbol() {
        return conjugateAcidSymbol;
    }
    
    protected void setStrength( double strength ) {
        if ( !( ABSConstants.WEAK_STRENGTH_RANGE.contains( strength ) ) ) {
            throw new IllegalArgumentException( "strength out of range: " + strength );
        }
        if ( strength != this.strength ) {
            this.strength = strength;
            //XXX notify
        }
    }
    
    public double getStrength() {
        return strength;
    }
    
    public static class CustomWeakBase extends WeakBase {
        
        private static final double DEFAULT_STRENGTH = ABSConstants.WEAK_STRENGTH_RANGE.getMin();
        
        public CustomWeakBase() {
            super( ABSStrings.CUSTOM_WEAK_BASE, ABSSymbols.B, ABSSymbols.BH_PLUS, DEFAULT_STRENGTH );
        }
        
        public void setStrength( double strength ) {
            super.setStrength( strength );
        }
    }
}
