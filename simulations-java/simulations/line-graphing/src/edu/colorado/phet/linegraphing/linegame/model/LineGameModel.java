// Copyright 2002-2012, University of Colorado
package edu.colorado.phet.linegraphing.linegame.model;

import edu.colorado.phet.common.games.GameSettings;
import edu.colorado.phet.common.games.GameTimer;
import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.util.IntegerRange;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.common.phetcommon.util.logging.LoggingUtils;
import edu.colorado.phet.linegraphing.common.LGConstants;
import edu.colorado.phet.linegraphing.common.model.Line;

/**
 * Model for the "Line Game" module. Responsibilities include:
 * <ul>
 * <li>creation of challenges</li>
 * <li>management of game state</li>
 * <li>management of game results</li>
 * </ul>
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class LineGameModel {

    private static final java.util.logging.Logger LOGGER = LoggingUtils.getLogger( LineGameModel.class.getCanonicalName() );

    private static final int CHALLENGES_PER_GAME = 4;
    private static final int MAX_POINTS_PER_CHALLENGE = 2;
    private static final IntegerRange LEVELS_RANGE = new IntegerRange( 1, 6 );

    public final GameSettings settings;
    public final GameTimer timer;
    public final GameResults results;
    public final Property<GamePhase> phase;
    public final Property<PlayState> state;

    public final Property<IChallenge> challenge; // the current challenge
    private IChallenge[] challenges = new IChallenge[CHALLENGES_PER_GAME];
    private int challengeIndex;

    // Default is a graph with uniform quadrants.
    public LineGameModel() {

        settings = new GameSettings( LEVELS_RANGE, true /* soundEnabled */, true /* timerEnabled */ );
        timer = new GameTimer();
        results = new GameResults( LEVELS_RANGE );

        // initial value is meaningless, but must be non-null
        challenge = new Property<IChallenge>( new GTL_Challenge( Line.createSlopeIntercept( 1, 1, 1 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.SLOPE,
                                                                 LGConstants.X_AXIS_RANGE, LGConstants.Y_AXIS_RANGE ) );

        phase = new Property<GamePhase>( GamePhase.SETTINGS ) {

            // Update fields so that they are accurate before property observers are notified.
            @Override public void set( GamePhase phase ) {
                LOGGER.info( "game phase = " + phase );
                if ( phase == GamePhase.SETTINGS ) {
                    state.set( PlayState.NONE );
                    timer.stop();
                }
                else if ( phase == GamePhase.PLAY ) {
                    initChallenges();
                    state.set( PlayState.FIRST_CHECK );
                    results.score.set( 0 );
                    timer.start();
                }
                else if ( phase == GamePhase.RESULTS ) {
                    state.set( PlayState.NONE );
                    timer.stop();
                    updateBestTime();
                }
                else {
                    throw new UnsupportedOperationException( "unsupported game phase = " + phase );
                }
                super.set( phase );
            }
        };

        initChallenges();

        state = new Property<PlayState>( PlayState.NONE ) {{
            addObserver( new VoidFunction1<PlayState>() {
                public void apply( PlayState state ) {
                    LOGGER.info( "play state = " + state );
                    if ( state == PlayState.FIRST_CHECK ) {
                        if ( challengeIndex == challenges.length ) {
                            // game has been completed
                            phase.set( GamePhase.RESULTS );
                        }
                        else {
                            // next challenge
                            challenge.set( challenges[challengeIndex] );
                            challengeIndex++;
                        }
                    }
                    else if ( state == PlayState.NEXT ) {
                        challenge.get().setAnswerVisible( true );
                    }
                }
            } );
        }};
    }

    private void initChallenges() {
        //TODO replace with random challenge generation
        final IntegerRange xRange = LGConstants.X_AXIS_RANGE;
        final IntegerRange yRange = LGConstants.Y_AXIS_RANGE;
        int index = 0;
        if ( settings.level.get() == 1 ) {
            challenges[index++] = new GTL_Challenge( Line.createSlopeIntercept( 1, 1, -2 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.INTERCEPT, xRange, yRange );
            challenges[index++] = new GTL_Challenge( Line.createSlopeIntercept( 5, 1, 1 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.SLOPE, xRange, yRange );
            challenges[index++] = new GTL_Challenge( Line.createSlopeIntercept( 4, 2, 3 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.SLOPE_INTERCEPT, xRange, yRange );
            challenges[index++] = new GTL_Challenge( Line.createSlopeIntercept( 3, 3, -3 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.POINTS, xRange, yRange);
        }
        else if ( settings.level.get() == 2 ) {
            challenges[index++] = new GTL_Challenge( Line.createPointSlope( 2, 1, 1, 2 ), LineForm.POINT_SLOPE, ManipulationMode.SLOPE, xRange, yRange );
            challenges[index++] = new GTL_Challenge( Line.createPointSlope( 1, -3, 1, 3 ), LineForm.POINT_SLOPE, ManipulationMode.POINT, xRange, yRange );
            challenges[index++] = new GTL_Challenge( Line.createPointSlope( -2, 1, -4, 3 ), LineForm.POINT_SLOPE, ManipulationMode.POINT_SLOPE, xRange, yRange );
            challenges[index++] = new GTL_Challenge( Line.createPointSlope( 5, 4, 3, 2 ), LineForm.POINT_SLOPE, ManipulationMode.POINTS, xRange, yRange );
        }
        else if ( settings.level.get() == 3 ) {
            challenges[index++] = new MTE_Challenge( Line.createSlopeIntercept( 1, 1, -2 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.INTERCEPT, xRange, yRange );
            challenges[index++] = new MTE_Challenge( Line.createSlopeIntercept( 5, 1, 1 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.SLOPE, xRange, yRange );
            challenges[index++] = new MTE_Challenge( Line.createSlopeIntercept( 4, 2, 3 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.SLOPE_INTERCEPT, xRange, yRange );
            challenges[index++] = new MTE_Challenge( Line.createSlopeIntercept( 3, 3, -3 ), LineForm.SLOPE_INTERCEPT, ManipulationMode.SLOPE_INTERCEPT, xRange, yRange );
        }
        else if ( settings.level.get() == 4 ) {
            challenges[index++] = new MTE_Challenge( Line.createPointSlope( 2, 1, 1, 2 ), LineForm.POINT_SLOPE, ManipulationMode.SLOPE, xRange, yRange );
            challenges[index++] = new MTE_Challenge( Line.createPointSlope( 1, -3, 1, 3 ), LineForm.POINT_SLOPE, ManipulationMode.POINT, xRange, yRange );
            challenges[index++] = new MTE_Challenge( Line.createPointSlope( -2, 1, -4, 3 ), LineForm.POINT_SLOPE, ManipulationMode.POINT_SLOPE, xRange, yRange );
            challenges[index++] = new MTE_Challenge( Line.createPointSlope( 5, 4, 3, 2 ), LineForm.POINT_SLOPE, ManipulationMode.POINT_SLOPE, xRange, yRange );
        }
        else if ( settings.level.get() == 5 ) {
            challenges[index++] = new P3P_Challenge( Line.createSlopeIntercept( 1, 1, -2 ), LineForm.SLOPE_INTERCEPT, xRange, yRange );
            challenges[index++] = new P3P_Challenge( Line.createSlopeIntercept( 5, 1, 1 ), LineForm.SLOPE_INTERCEPT, xRange, yRange );
            challenges[index++] = new P3P_Challenge( Line.createSlopeIntercept( 4, 2, 3 ), LineForm.SLOPE_INTERCEPT, xRange, yRange );
            challenges[index++] = new P3P_Challenge( Line.createSlopeIntercept( 3, 3, -3 ), LineForm.SLOPE_INTERCEPT, xRange, yRange );
        }
        else if ( settings.level.get() == 6 ) {
            challenges[index++] = new P3P_Challenge( Line.createPointSlope( 2, 1, 1, 2 ), LineForm.POINT_SLOPE, xRange, yRange );
            challenges[index++] = new P3P_Challenge( Line.createPointSlope( 1, -3, 1, 3 ), LineForm.POINT_SLOPE, xRange, yRange );
            challenges[index++] = new P3P_Challenge( Line.createPointSlope( -2, 1, -4, 3 ), LineForm.POINT_SLOPE, xRange, yRange );
            challenges[index++] = new P3P_Challenge( Line.createPointSlope( 5, 4, 3, 2 ), LineForm.POINT_SLOPE, xRange, yRange );
        }
        else {
            throw new IllegalArgumentException( "unsupported level: " + settings.level.get() );
        }
        assert ( challenges.length == CHALLENGES_PER_GAME );
        challengeIndex = 0;
    }

    public boolean isPerfectScore() {
        return results.score.get() == getPerfectScore();
    }

    // Gets the number of points in a perfect score (ie, correct answers for all challenges on the first try)
    public int getPerfectScore() {
        return CHALLENGES_PER_GAME * computePoints( 1 );
    }

    // Updates the best time for the current level, at the end of a timed game with a perfect score.
    private void updateBestTime() {
        assert ( !timer.isRunning() );
        if ( settings.timerEnabled.get() && results.score.get() == getPerfectScore() ) {
            results.updateBestTime( settings.level.get(), timer.time.get() );
        }
    }

    // Compute points to be awarded for a correct answer.
    public int computePoints( int attempts ) {
        return Math.max( 0, MAX_POINTS_PER_CHALLENGE - attempts + 1 );
    }
}