/* Copyright 2010, University of Colorado */

package edu.colorado.phet.reactantsproductsandleftovers.module.game;

import java.util.ArrayList;

import edu.colorado.phet.reactantsproductsandleftovers.model.ChemicalReaction;
import edu.colorado.phet.reactantsproductsandleftovers.model.Reactant;
import edu.colorado.phet.reactantsproductsandleftovers.model.OneProductReactions.*;
import edu.colorado.phet.reactantsproductsandleftovers.model.TwoProductReactions.*;
import edu.colorado.phet.reactantsproductsandleftovers.module.game.GameChallenge.ChallengeType;

/**
 * A simple game strategy. 
 * <p> 
 * <ul>
 * <li>Level 1: one product random, After
 * <li>Level 2: one product random, Before 
 * <li>Level 3: two products random, After or Before random
 * </ul>
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class SimpleGameStrategy extends AbstractGameStrategy {
    
    // level 1 is all the one-product reactions
    private static final Class<?>[] LEVEL1_REACTIONS = { 
        WaterReaction.class, 
        Reaction_H2_F2__2HF.class,
        Reaction_H2_Cl2__2HCl.class,
        Reaction_CO_2H2__CH3OH.class,
        Reaction_CH2O_H2__CH3OH.class,
        Reaction_C2H4_H2__C2H6.class,
        Reaction_C2H2_2H2__C2H6.class,
        Reaction_C_O2__CO2.class,
        Reaction_2C_O2__2CO.class,
        Reaction_2CO_O2__2CO2.class,
        Reaction_C_CO2__2CO.class,
        Reaction_C_2S__CS2.class,
        AmmoniaReaction.class,
        Reaction_N2_O2__2NO.class,
        Reaction_2NO_O2__2NO2.class,
        Reaction_2N2_O2__2NO2.class,
        Reaction_P4_6H2__4PH3.class,
        Reaction_P4_6F2__4PF3.class,
        Reaction_P4_6Cl2__4PCl3.class,
        Reaction_P4_10Cl2__4PCl5.class,
        Reaction_PCl3_Cl2__PCl5.class,
        Reaction_2SO2_O2__2SO3.class 
        };

    // level 2 uses the same reactions as level 1
    private static final Class<?>[] LEVEL2_REACTIONS = LEVEL1_REACTIONS;

    // level 3 is all the two-product reactions
    private static final Class<?>[] LEVEL3_REACTIONS = { 
        Reaction_2C_2H2O__CH4_CO2.class, 
        Reaction_CH4_H2O__3H2_CO.class, 
        MethaneReaction.class, 
        Reaction_2C2H6_7O2__4CO2_6H2O.class, 
        Reaction_C2H4_3O2__2CO2_2H2O.class, 
        Reaction_2C2H2_5O2__4CO2_2H2O.class, 
        Reaction_C2H5OH_3O2__2CO2_3H2O.class, 
        Reaction_C2H6_Cl2__C2H5Cl_HCl.class, 
        Reaction_CH4_4S__CS2_2H2S.class, 
        Reaction_CS2_3O2__CO2_2SO2.class, 
        Reaction_4NH3_3O2__2N2_6H2O.class, 
        Reaction_4NH3_5O2__4NO_6H2O.class, 
        Reaction_4NH3_7O2__4NO2_6H2O.class, 
        Reaction_4NH3_6NO__5N2_6H2O.class, 
        Reaction_SO2_2H2__S_2H2O.class, 
        Reaction_SO2_3H2__H2S_2H2O.class, 
        Reaction_2F2_H2O__OF2_2HF.class, 
        Reaction_OF2_H2O__O2_2HF.class 
        };

    private static final Class<?>[][] REACTIONS = { LEVEL1_REACTIONS, LEVEL2_REACTIONS, LEVEL3_REACTIONS };
    
    /**
     * Default constructor.
     */
    public SimpleGameStrategy() {}

    protected GameChallenge[] createChallengesAux( int level ) {

        GameChallenge[] challenges = new GameChallenge[ getChallengesPerGame() ];
        ChemicalReaction previousReaction = null;
        for ( int i = 0; i < challenges.length; i++ ) {

            // reaction
            ChemicalReaction reaction = getRandomReaction( level, previousReaction );

            // challenge type
            ChallengeType challengeType;
            if ( level == 1 ) {
                challengeType = ChallengeType.AFTER;
            }
            else if ( level == 2 ) {
                challengeType = ChallengeType.BEFORE;
            }
            else {
                challengeType = Math.random() > 0.5 ? ChallengeType.BEFORE : ChallengeType.AFTER;
            }

            // quantities
            for ( Reactant reactant : reaction.getReactants() ) {
                reactant.setQuantity( getRandomQuantity() );
            }

            challenges[i] = new GameChallenge( challengeType, reaction );
            previousReaction = reaction;
        }
        return challenges;
    }

    /*
     * Creates a random reaction for a specified level.
     */
    private static ChemicalReaction getRandomReaction( int level, ChemicalReaction previousReaction ) {
        
        // Select a random reaction from the array for the specified level.
        int levelIndex = level - 1;
        if ( levelIndex < 0 || levelIndex > REACTIONS.length - 1 ) {
            throw new IllegalArgumentException( "unsupported level: " + level );
        }
        int reactionIndex = (int) ( Math.random() * REACTIONS[levelIndex].length );
        Class<?> reactionClass = REACTIONS[levelIndex][reactionIndex];
        
        // If same as the previous reaction, simply get the next reaction in the array.
        if ( previousReaction != null && reactionClass.equals( previousReaction.getClass() ) ) {
            reactionIndex++;
            if ( reactionIndex > REACTIONS[levelIndex].length - 1 ) {
                reactionIndex = 0;
            }
            reactionClass = REACTIONS[levelIndex][reactionIndex];
        }
        
        return instantiateReaction( REACTIONS[levelIndex][reactionIndex] );
    }

    /*
     * Uses reflection to instantiate a chemical reaction by class.
     */
    private static ChemicalReaction instantiateReaction( Class<?> c ) {
        ChemicalReaction reaction = null;
        try {
            reaction = (ChemicalReaction) c.newInstance(); //XXX eliminate cast
        }
        catch ( InstantiationException e ) {
            e.printStackTrace();
        }
        catch ( IllegalAccessException e ) {
            e.printStackTrace();
        }
        return reaction;
    }
    
    /*
     * Looks for equations that will experience a violation of the quantity range.
     * Suppose the quantity range is 0-N.  For some reactions, setting the reactant quantities 
     * to N will result in a product quantity > N.  This will result in range violations 
     * elsewhere in the application, for example in the controls used to set and display
     * quantity values.
     */
    private static void analyzeRangeViolations() {
        
        // put all reaction in a container, removing duplicates.
        ArrayList<Class<?>> reactionClasses = new ArrayList<Class<?>>();
        for ( int i = 0; i < REACTIONS.length; i++ ) {
            for ( int j = 0; j < REACTIONS[i].length; j++ ) {
                Class<?> reactionClass = REACTIONS[i][j];
                if ( !reactionClasses.contains( reactionClass ) ) {
                    reactionClasses.add( reactionClass );
                }
            }
        }
        
        // check every reaction class
        for ( Class<?> reactionClass : reactionClasses ) {
            ChemicalReaction reaction = instantiateReaction( reactionClass );
            // set all reactant quantities to their max values.
            for ( Reactant reactant : reaction.getReactants() ) {
                reactant.setQuantity( getMaxQuantity() );
            }
            // look for violations and try to fix them.
            fixQuantityRangeViolation( reaction );
        }
    }
    
    // test for range violations inherent in reactions, and verified that they are fixable.
    public static void main( String[] args ) {
        analyzeRangeViolations();
    }
}
