// Copyright 2002-2012, University of Colorado
package edu.colorado.phet.fractionsintro.matchinggame.model;

import fj.F;
import fj.F2;
import fj.data.List;
import lombok.Data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.colorado.phet.common.phetcommon.simsharing.messages.IUserComponent;
import edu.colorado.phet.fractions.util.immutable.Vector2D;
import edu.colorado.phet.fractionsintro.intro.model.Fraction;
import edu.colorado.phet.fractionsintro.intro.view.FractionNode;
import edu.colorado.phet.fractionsintro.matchinggame.model.Pattern.Grid;
import edu.colorado.phet.fractionsintro.matchinggame.model.Pattern.PlusSigns;
import edu.colorado.phet.fractionsintro.matchinggame.model.Pattern.Polygon;
import edu.colorado.phet.fractionsintro.matchinggame.model.Pattern.Pyramid;
import edu.colorado.phet.fractionsintro.matchinggame.view.fractions.FilledPattern;
import edu.colorado.phet.fractionsintro.matchinggame.view.fractions.IColor;
import edu.colorado.phet.fractionsintro.matchinggame.view.fractions.PatternNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.nodes.PComposite;

import static edu.colorado.phet.common.phetcommon.simsharing.messages.UserComponentChain.chain;
import static edu.colorado.phet.fractionsintro.FractionsIntroSimSharing.Components.matchingGameFraction;
import static edu.colorado.phet.fractionsintro.common.view.Colors.LIGHT_BLUE;
import static edu.colorado.phet.fractionsintro.common.view.Colors.LIGHT_GREEN;
import static edu.colorado.phet.fractionsintro.matchinggame.model.Motions.MoveToCell;
import static edu.colorado.phet.fractionsintro.matchinggame.model.MovableFraction.nextID;
import static edu.colorado.phet.fractionsintro.matchinggame.model.RepresentationType.*;
import static edu.colorado.phet.fractionsintro.matchinggame.view.fractions.FilledPattern.randomFill;
import static edu.colorado.phet.fractionsintro.matchinggame.view.fractions.FilledPattern.sequentialFill;
import static fj.data.List.*;

/**
 * Levels for the matching game, declared as functions below that return the MovableFraction instances (and hence their representations) in a level.
 *
 * @author Sam Reid
 */
public class Levels {

    public static final Random random = new Random();

    //Pattern for filling in shapes sequentially
    private static final F2<Pattern, Integer, FilledPattern> SEQUENTIAL = new F2<Pattern, Integer, FilledPattern>() {
        @Override public FilledPattern f( final Pattern pattern, final Integer numFilled ) {
            return sequentialFill( pattern, numFilled );
        }
    };

    //Pattern for filling in shapes randomly with the specified random seed
    public static F2<Pattern, Integer, FilledPattern> RANDOM() { return RANDOM( random.nextLong() ); }

    public static F2<Pattern, Integer, FilledPattern> RANDOM( final long seed ) {
        return new F2<Pattern, Integer, FilledPattern>() {
            @Override public FilledPattern f( final Pattern pattern, final Integer numFilled ) {
                return randomFill( pattern, numFilled, seed );
            }
        };
    }

    public static F<Fraction, ArrayList<RepresentationType>> representationFunction( final List<RepresentationType> r ) {
        return new F<Fraction, ArrayList<RepresentationType>>() {
            @Override public ArrayList<RepresentationType> f( Fraction fraction ) {
                return createRepresentations( fraction, r );
            }
        };
    }

    private static final boolean debug = false;

    //Singleton, use Levels instance
    private Levels() {
    }

    public static final F<Fraction, Boolean> all = new F<Fraction, Boolean>() {
        @Override public Boolean f( final Fraction fraction ) {
            return true;
        }
    };

    final static RepresentationType numeric = singleRepresentation( "numeric", all,
                                                                    new F<Fraction, PNode>() {
                                                                        @Override public PNode f( Fraction f ) {
                                                                            return new FractionNode( f, 0.3 );
                                                                        }
                                                                    } );


    static RepresentationType scaledNumeric( final int scale ) {
        return singleRepresentation( "numeric", all,
                                     new F<Fraction, PNode>() {
                                         @Override public PNode f( Fraction f ) {
                                             return new FractionNode( new Fraction( f.numerator * scale, f.denominator * scale ), 0.3 );
                                         }
                                     } );
    }

    final RepresentationType horizontalBars4 = createPatterns( "horizontal bars", 4, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.horizontalBars( 4 );
        }
    }, SEQUENTIAL );
    final RepresentationType horizontalBars3 = createPatterns( "horizontal bars", 3, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.horizontalBars( 3 );
        }
    }, SEQUENTIAL );
    final RepresentationType horizontalBars2 = createPatterns( "horizontal bars", 2, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.horizontalBars( 2 );
        }
    }, SEQUENTIAL );
    final RepresentationType horizontalBars1 = createPatterns( "horizontal bars", 1, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.horizontalBars( 1 );
        }
    }, SEQUENTIAL );

    final RepresentationType verticalBars4 = createPatterns( "vertical bars", 4, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.verticalBars( 4 );
        }
    }, SEQUENTIAL );
    final RepresentationType verticalBars3 = createPatterns( "vertical bars", 3, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.verticalBars( 3 );
        }
    }, SEQUENTIAL );
    final RepresentationType verticalBars2 = createPatterns( "vertical bars", 2, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.verticalBars( 2 );
        }
    }, SEQUENTIAL );

    final RepresentationType pies4 = createPatterns( "pies", 4, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.pie( 4 );
        }
    }, SEQUENTIAL );
    final RepresentationType pies3 = createPatterns( "pies", 3, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.pie( 3 );
        }
    }, SEQUENTIAL );
    final RepresentationType pies2 = createPatterns( "pies", 2, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.pie( 2 );
        }
    }, SEQUENTIAL );
    final RepresentationType pies1 = createPatterns( "pies", 1, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.pie( 1 );
        }
    }, SEQUENTIAL );

    public RepresentationType polygon( final int numSides, F2<Pattern, Integer, FilledPattern> fill ) {
        return createPatterns( numSides + " polygon", numSides, 80, new F<Integer, Pattern>() {
            @Override public Pattern f( final Integer integer ) {
                return Polygon.create( 80, numSides );
            }
        }, fill );
    }

    public RepresentationType elShapedPairs( final int numPairs, F2<Pattern, Integer, FilledPattern> fill ) {
        return createPatterns( numPairs + " L-shaped diagonal", numPairs, 60, new F<Integer, Pattern>() {
            @Override public Pattern f( final Integer integer ) {
                return Pattern.letterLShapedDiagonal( 14, numPairs );
            }
        }, fill );
    }

    public RepresentationType tetrisPiece( F2<Pattern, Integer, FilledPattern> fill ) {
        return createPatterns( "tetris piece", 4, 50, new F<Integer, Pattern>() {
            @Override public Pattern f( final Integer length ) {
                return Pattern.tetrisPiece( length );
            }
        }, fill );
    }

    private RepresentationType makePlusses( final int numPlusses, F2<Pattern, Integer, FilledPattern> order ) {
        return createPatterns( numPlusses + " plusses", numPlusses, 10, new F<Integer, Pattern>() {
            @Override public Pattern f( final Integer integer ) {
                return new PlusSigns( numPlusses );
            }
        }, order );
    }

    final RepresentationType sixFlowerSEQUENTIAL = createPatterns( "six flower sequential", 6, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pattern.sixFlower( 25 );
        }
    }, SEQUENTIAL );
    final RepresentationType sixFlowerRANDOM = createPatterns( "six flower sequential", 6, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pattern.sixFlower( 25 );
        }
    }, RANDOM() );

    final RepresentationType interleavedLShapeSEQUENTIAL = createPatterns( "interleaved L shape, sequential", 8, 80, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.interleavedLShape( 35, 2, 2 );
        }
    }, SEQUENTIAL );
    final RepresentationType interleavedLShapeRANDOM = createPatterns( "interleaved L shape, sequential", 8, 80, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer integer ) {
            return Pattern.interleavedLShape( 35, 2, 2 );
        }
    }, RANDOM() );

    final RepresentationType fourGridSEQUENTIAL = createPatterns( "four grid", 4, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return new Grid( 2 );
        }
    }, SEQUENTIAL );
    final RepresentationType nineGridSEQUENTIAL = createPatterns( "nine grid", 9, 50, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return new Grid( 3 );
        }
    }, SEQUENTIAL );
    final RepresentationType fourGridRANDOM = createPatterns( "four grid", 4, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return new Grid( 2 );
        }
    }, RANDOM() );
    final RepresentationType nineGridRANDOM = createPatterns( "nine grid", 9, 50, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return new Grid( 3 );
        }
    }, RANDOM() );

    final RepresentationType onePyramidSEQUENTIAL = createPatterns( "one pyramid", 1, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pyramid.single( length );
        }
    }, SEQUENTIAL );
    final RepresentationType fourPyramidSEQUENTIAL = createPatterns( "four pyramid", 4, 50, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pyramid.four( length );
        }
    }, SEQUENTIAL );
    final RepresentationType ninePyramidSEQUENTIAL = createPatterns( "nine pyramid", 9, 30, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pyramid.nine( length );
        }
    }, SEQUENTIAL );
    final RepresentationType onePyramidRANDOM = createPatterns( "one pyramid", 1, 100, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pyramid.single( length );
        }
    }, RANDOM() );
    final RepresentationType fourPyramidRANDOM = createPatterns( "four pyramid", 4, 50, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pyramid.four( length );
        }
    }, RANDOM() );
    final RepresentationType ninePyramidRANDOM = createPatterns( "nine pyramid", 9, 30, new F<Integer, Pattern>() {
        @Override public Pattern f( final Integer length ) {
            return Pyramid.nine( length );
        }
    }, RANDOM() );

    private RepresentationType createPatterns( String name, final int max, final int length, final F<Integer, Pattern> pattern, final F2<Pattern, Integer, FilledPattern> fill ) {
        return twoComposites( name, new F<Fraction, Boolean>() {
                                  @Override public Boolean f( final Fraction fraction ) {
                                      return fraction.denominator == max;
                                  }
                              },
                              new F<Fraction, PNode>() {
                                  @Override public PNode f( Fraction f ) {
                                      return new PatternNode( fill.f( pattern.f( length ), f.numerator ), LIGHT_GREEN );
                                  }
                              },
                              new F<Fraction, PNode>() {
                                  @Override public PNode f( Fraction f ) {
                                      return new PatternNode( fill.f( pattern.f( length ), f.numerator ), LIGHT_BLUE );
                                  }
                              }
        );
    }

    public static Levels Levels = new Levels();

    @SuppressWarnings("unchecked")
    final List<RepresentationType> easyRepresentations = list( numeric, horizontalBars1, horizontalBars2, horizontalBars3, horizontalBars4, verticalBars2, verticalBars3, verticalBars4, pies1, pies2, pies3, pies4 );
    final List<RepresentationType> mediumRepresentationsSequential = list( makePlusses( 2, SEQUENTIAL ), makePlusses( 3, SEQUENTIAL ), makePlusses( 4, SEQUENTIAL ), makePlusses( 5, SEQUENTIAL ), makePlusses( 6, SEQUENTIAL ),
                                                                           fourGridSEQUENTIAL, nineGridSEQUENTIAL, onePyramidSEQUENTIAL, fourPyramidSEQUENTIAL, ninePyramidSEQUENTIAL,
                                                                           polygon( 4, SEQUENTIAL ), polygon( 5, SEQUENTIAL ), polygon( 6, SEQUENTIAL ), polygon( 7, SEQUENTIAL ), polygon( 8, SEQUENTIAL ),
                                                                           tetrisPiece( SEQUENTIAL ), elShapedPairs( 2, SEQUENTIAL ), elShapedPairs( 3, SEQUENTIAL ),
                                                                           sixFlowerSEQUENTIAL, interleavedLShapeSEQUENTIAL
    );
    final List<RepresentationType> mediumRepresentationsRandom = list( makePlusses( 2, RANDOM() ), makePlusses( 3, RANDOM() ), makePlusses( 4, RANDOM() ), makePlusses( 5, RANDOM() ), makePlusses( 6, RANDOM() ),
                                                                       fourGridRANDOM, nineGridRANDOM, onePyramidRANDOM, fourPyramidRANDOM, ninePyramidRANDOM,
                                                                       polygon( 4, RANDOM() ), polygon( 5, RANDOM() ), polygon( 6, RANDOM() ), polygon( 7, RANDOM() ), polygon( 8, RANDOM() ),
                                                                       tetrisPiece( RANDOM() ), elShapedPairs( 2, RANDOM() ), elShapedPairs( 3, RANDOM() ),
                                                                       sixFlowerRANDOM, interleavedLShapeRANDOM
    );
    final List<RepresentationType> easyScaledNumeric = list( scaledNumeric( 2 ), scaledNumeric( 3 ) );
    final List<RepresentationType> difficultScaledNumeric = list( scaledNumeric( 4 ), scaledNumeric( 5 ) );

    private ResultPair createPair( ArrayList<Fraction> fractions, ArrayList<Cell> cells, F<Fraction, ArrayList<RepresentationType>> representationPool, final List<ResultPair> alreadySelected ) {

        //choose a fraction
        final Fraction fraction = fractions.get( random.nextInt( fractions.size() ) );

        //Sampling is without replacement, so remove the old fraction.
        fractions.remove( fraction );
        ArrayList<RepresentationType> representations = representationPool.f( fraction );

        //Don't allow duplicate representations for fractions
        //Find all representations for the given fraction
        List<Result> previouslySelected = alreadySelected.map( new F<ResultPair, Result>() {
            @Override public Result f( final ResultPair r ) {
                return r.a;
            }
        } ).append( alreadySelected.map( new F<ResultPair, Result>() {
            @Override public Result f( final ResultPair r ) {
                return r.b;
            }
        } ) );
        List<Result> same = previouslySelected.filter( new F<Result, Boolean>() {
            @Override public Boolean f( final Result r ) {
                return r.fraction.fraction().equals( fraction ) && numeric.contains( r.representation );
            }
        } );
        representations.removeAll( same.map( new F<Result, F<Fraction, PNode>>() {
            @Override public F<Fraction, PNode> f( final Result result ) {
                return result.representation;
            }
        } ).toCollection() );

        //create 2 representation for it
        RepresentationType representationSetA = representations.get( random.nextInt( representations.size() ) );
        final Cell cellA = cells.get( random.nextInt( cells.size() ) );
        final F<Fraction, PNode> representationA = representationSetA.chooseOne();
        MovableFraction fractionA = fraction( fraction, cellA, representationA, createUserComponent( fraction, representationSetA ) );

        //Don't use the same representation for the 2nd one, and put it in a new cell
        while ( representations.contains( representationSetA ) ) {
            representations.remove( representationSetA );
        }
        cells.remove( cellA );

        final Cell cellB = cells.get( random.nextInt( cells.size() ) );
        RepresentationType representationSetB = representations.get( random.nextInt( representations.size() ) );
        final F<Fraction, PNode> representationB = representationSetB.chooseOne();
        MovableFraction fractionB = fraction( fraction, cellB, representationB, createUserComponent( fraction, representationSetB ) );

        cells.remove( cellB );

        return new ResultPair( new Result( fractionA, representationSetA, representationA ), new Result( fractionB, representationSetB, representationB ) );
    }

    private IUserComponent createUserComponent( final Fraction fraction, final RepresentationType representationType ) {
        return chain( chain( matchingGameFraction, fraction.numerator + "/" + fraction.denominator ), representationType.name );
    }

    private static MovableFraction fraction( Fraction fraction, Cell cell, final F<Fraction, PNode> node, IUserComponent userComponent ) {
        return fraction( fraction.numerator, fraction.denominator, cell, node, userComponent );
    }

    //Create a MovableFraction for the given fraction at the specified cell
    private static MovableFraction fraction( int numerator, int denominator, Cell cell, final F<Fraction, PNode> node, IUserComponent userComponent ) {

        //Find the color for the node.
        //TODO: consider creating class ColorNode extends PNode and use F<Fraction,ColorNode> everywhere.
        PNode n = node.f( new Fraction( numerator, denominator ) );
        Color color = n instanceof FractionNode ? Color.black :
                      n instanceof IColor ? ( (IColor) n ).getColor() :
                      Color.green;
        //Cache nodes as images to improve performance
        //TODO: could this put the same node at 2 places in the scene graph?  If so, what problems would that cause?
        return new MovableFraction( nextID(), new Vector2D( cell.rectangle.getCenter() ), numerator, denominator, false, cell, 1.0,
//                                    new Cache<Fraction, PNode>(
                                    new F<Fraction, PNode>() {
                                        @Override public PNode f( final Fraction fraction ) {
                                            return new PComposite() {{ addChild( node.f( fraction ) ); }};
                                        }
                                    }
//        )
                ,
                                    MoveToCell( cell ), false, userComponent, color );
    }

    private List<MovableFraction> createLevel( F<Fraction, ArrayList<RepresentationType>> representations, List<Cell> _cells, Fraction[] a ) {
        assert _cells.length() % 2 == 0;
        ArrayList<Fraction> fractions = new ArrayList<Fraction>( Arrays.asList( a ) );

        ArrayList<MovableFraction> list = new ArrayList<MovableFraction>();

        //Use mutable collection so it can be removed from for drawing without replacement
        ArrayList<Cell> cells = new ArrayList<Cell>( _cells.toCollection() );

        //Keep track of all so that we don't replicate representations
        ArrayList<ResultPair> all = new ArrayList<ResultPair>();
        while ( list.size() < _cells.length() ) {
            ResultPair pair = createPair( fractions, cells, representations, iterableList( all ) );
            all.add( pair );
            list.add( pair.a.fraction );
            list.add( pair.b.fraction );
        }

        //make sure no representation type used twice
        if ( debug ) {
            makeSureNoRepresentationTypeUsedTwiceForTheSameFraction( iterableList( all ) );
        }

        return iterableList( list );
    }

    private F<Fraction, ArrayList<RepresentationType>> getRepresentationPool( final int level ) {
        return level == 1 ? representationFunction( easyRepresentations ) :
               level >= 2 && level <= 3 ? representationFunction( easyRepresentations.append( mediumRepresentationsSequential ) ) :
               level == 4 ? representationFunction( mediumRepresentationsRandom ) :
               level == 5 ? representationFunction( mediumRepresentationsRandom.append( easyScaledNumeric ) ) :
               level == 6 ? representationFunction( mediumRepresentationsRandom.append( mediumRepresentationsRandom.append( difficultScaledNumeric ) ) ) :
               matchError( "level not found: " + level );
    }

    private F<Fraction, ArrayList<RepresentationType>> matchError( final String s ) { throw new RuntimeException( s ); }

    private void makeSureNoRepresentationTypeUsedTwiceForTheSameFraction( final List<ResultPair> all ) {
        List<Fraction> fractions = all.bind( new F<ResultPair, List<Fraction>>() {
            @Override public List<Fraction> f( final ResultPair resultPair ) {
                return single( resultPair.a.fraction.fraction() ).snoc( resultPair.b.fraction.fraction() );
            }
        } );
        for ( final Fraction fraction : fractions ) {
            List<Result> allResultsForFraction = all.bind( new F<ResultPair, List<Result>>() {
                @Override public List<Result> f( final ResultPair resultPair ) {
                    return single( resultPair.a ).snoc( resultPair.b );
                }
            } ).filter( new F<Result, Boolean>() {
                @Override public Boolean f( final Result result ) {
                    return result.fraction.fraction().equals( fraction );
                }
            } );
            List<RepresentationType> types = allResultsForFraction.map( new F<Result, RepresentationType>() {
                @Override public RepresentationType f( final Result result ) {
                    return result.representationType;
                }
            } );
            List<RepresentationType> unique = types.nub();
            System.out.println( "types.length() = " + types.length() + ", unique.length = " + unique.length() );
            if ( types.length() != unique.length() ) {
                System.out.println( "unique = " + unique.map( _name ) + ", list = " + types.map( _name ) );
            }
            assert types.length() == unique.length();
        }
    }

    public static final @Data class Result {
        public final MovableFraction fraction;
        public final RepresentationType representationType;
        public final F<Fraction, PNode> representation;
    }

    public static final @Data class ResultPair {
        public final Result a;
        public final Result b;
    }

    //Create the default representations that will be used in all levels
    private static ArrayList<RepresentationType> createRepresentations( final Fraction fraction, List<RepresentationType> allRepresentations ) {

        //Find the representations that could be used to show the given fraction
        List<RepresentationType> applicableRepresentations = allRepresentations.filter( new F<RepresentationType, Boolean>() {
            @Override public Boolean f( final RepresentationType r ) {
                return r.appliesTo.f( fraction );
            }
        } );

        //Count the non-numeric representations
        int nonNumeric = applicableRepresentations.filter( new F<RepresentationType, Boolean>() {
            @Override public Boolean f( final RepresentationType representationSet ) {
                return representationSet != numeric;
            }
        } ).length();

        //Count the numeric representations
        int n = applicableRepresentations.filter( new F<RepresentationType, Boolean>() {
            @Override public Boolean f( final RepresentationType representationSet ) {
                return representationSet == numeric;
            }
        } ).length();

        //Add one "numerical" representation for each graphical one, so that on average there will be about 50% numerical
        int numToAdd = nonNumeric - n;
        if ( numToAdd > 0 ) {
            applicableRepresentations = applicableRepresentations.cons( numeric );
        }
        return new ArrayList<RepresentationType>( applicableRepresentations.toCollection() );
    }

    /**
     * Level 1
     * No mixed numbers
     * Only “exact” matches will be present.  So for instance if there is a 3/6  and a pie with 6 divisions and 3 shaded slices, there will not be a ½  present .  In other words, the numerical representation on this level will exactly match the virtual manipulative.
     * Only numbers/representations  ≦ 1 possible on this level
     * “Easy” shapes on this level (not some of the more abstract representations)
     */
    final Fraction[] level1Fractions = {
            new Fraction( 1, 3 ),
            new Fraction( 2, 3 ),
            new Fraction( 1, 4 ),
            new Fraction( 3, 4 ),
            new Fraction( 1, 2 ),
            new Fraction( 1, 1 ) };
    public F<List<Cell>, List<MovableFraction>> Level1 = new F<List<Cell>, List<MovableFraction>>() {
        @Override public List<MovableFraction> f( List<Cell> cells ) { return createLevel( getRepresentationPool( 1 ), cells, level1Fractions ); }

        //Test for just creating one fraction
//        @Override public List<MovableFraction> f( List<Cell> cells ) { return single( createLevel( getRepresentationPool( 1 ), cells, level1Fractions ).head() ); }
    };

    /**
     * Level 2
     * Reduced fractions possible on this level.  So, for instance 3/6 and ½  could both be present.  Or a virtual representation of 3/6 could have the numerical of ½ be its only possible match
     * Still only numbers/representations  ≦ 1 possible
     * More shapes can be introduced
     */
    final Fraction[] level2Fractions = {
            new Fraction( 1, 2 ),
            new Fraction( 2, 4 ),
            new Fraction( 3, 4 ),
            new Fraction( 1, 3 ),
            new Fraction( 2, 3 ),
            new Fraction( 3, 6 ),
            new Fraction( 2, 6 ) };
    public F<List<Cell>, List<MovableFraction>> Level2 = new F<List<Cell>, List<MovableFraction>>() {
        @Override public List<MovableFraction> f( List<Cell> cells ) {
            return createLevel( getRepresentationPool( 2 ), cells, level2Fractions );
        }
    };

    /**
     * Level 3:
     * Reduced fractions possible on this level.  So, for instance 3/6 and ½  could both be present.  Or a virtual representation of 3/6 could have the numerical of ½ be its only possible match
     * Still only numbers/representations  ≦ 1 possible
     * More shapes can be introduced
     */
    final Fraction[] level3Fractions = {
            new Fraction( 3, 2 ),
            new Fraction( 4, 3 ),
            new Fraction( 6, 3 ),
            new Fraction( 4, 2 ),
            new Fraction( 7, 6 ),
            new Fraction( 4, 5 ),
            new Fraction( 7, 4 ),
            new Fraction( 5, 4 ),
            new Fraction( 6, 4 ),
            new Fraction( 5, 6 ),
            new Fraction( 4, 6 ),
            new Fraction( 3, 6 ),
            new Fraction( 2, 6 ),
            new Fraction( 3, 8 ),
            new Fraction( 4, 8 ),
            new Fraction( 5, 8 ),
            new Fraction( 6, 8 ),
            new Fraction( 7, 8 ),
    };
    public F<List<Cell>, List<MovableFraction>> Level3 = new F<List<Cell>, List<MovableFraction>>() {
        @Override public List<MovableFraction> f( List<Cell> cells ) {
            return createLevel( getRepresentationPool( 3 ), cells, level3Fractions );
        }
    };

    /**
     * Level 4:
     * All representations possible as well as complicated mixed/improper numbers
     */
    final Fraction[] level4Fractions = {
            new Fraction( 13, 7 ),
            new Fraction( 13, 7 ),
            new Fraction( 14, 8 ),
            new Fraction( 9, 5 ),
            new Fraction( 6, 3 ),
            new Fraction( 9, 8 ),
            new Fraction( 8, 9 ),
            new Fraction( 6, 9 ),
            new Fraction( 4, 9 ),
            new Fraction( 3, 9 ),
            new Fraction( 2, 9 ),
            new Fraction( 9, 7 ) };
    public F<List<Cell>, List<MovableFraction>> Level4 = new F<List<Cell>, List<MovableFraction>>() {
        @Override public List<MovableFraction> f( List<Cell> cells ) {
            return createLevel( getRepresentationPool( 4 ), cells, level4Fractions );
        }
    };

    /**
     * Level 5:
     * All representations possible as well as complicated mixed/improper numbers
     */
    final Fraction[] level5Fractions = {
            new Fraction( 13, 7 ),
            new Fraction( 13, 7 ),
            new Fraction( 14, 8 ),
            new Fraction( 9, 5 ),
            new Fraction( 6, 3 ),
            new Fraction( 9, 8 ),
            new Fraction( 8, 9 ),
            new Fraction( 6, 9 ),
            new Fraction( 4, 9 ),
            new Fraction( 3, 9 ),
            new Fraction( 2, 9 ),
            new Fraction( 9, 7 ) };
    public F<List<Cell>, List<MovableFraction>> Level5 = new F<List<Cell>, List<MovableFraction>>() {
        @Override public List<MovableFraction> f( List<Cell> cells ) {
            return createLevel( getRepresentationPool( 5 ), cells, level5Fractions );
        }
    };

    /**
     * Level 6:
     * All representations possible as well as complicated mixed/improper numbers
     */
    final Fraction[] level6Fractions = {
            new Fraction( 9, 5 ),
            new Fraction( 8, 5 ),
            new Fraction( 7, 5 ),
            new Fraction( 6, 5 ),
            new Fraction( 7, 6 ),
            new Fraction( 8, 6 ),
            new Fraction( 9, 6 ),
            new Fraction( 9, 7 ),
            new Fraction( 10, 7 ),
            new Fraction( 13, 7 ),
            new Fraction( 9, 8 ),
            new Fraction( 10, 8 ),
            new Fraction( 11, 8 ),
            new Fraction( 14, 8 ),
            new Fraction( 4, 9 ),
            new Fraction( 6, 9 ),
            new Fraction( 8, 9 ),
            new Fraction( 10, 9 ),
            new Fraction( 11, 9 ),
    };
    public F<List<Cell>, List<MovableFraction>> Level6 = new F<List<Cell>, List<MovableFraction>>() {
        @Override public List<MovableFraction> f( List<Cell> cells ) {
            return createLevel( getRepresentationPool( 6 ), cells, level6Fractions );
        }
    };

    public F<List<Cell>, List<MovableFraction>> get( int level ) {
        return level == 1 ? Level1 :
               level == 2 ? Level2 :
               level == 3 ? Level3 :
               level == 4 ? Level4 :
               level == 5 ? Level5 :
               Level6;
    }
}