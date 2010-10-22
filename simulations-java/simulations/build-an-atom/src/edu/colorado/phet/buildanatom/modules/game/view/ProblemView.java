package edu.colorado.phet.buildanatom.modules.game.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.colorado.phet.buildanatom.BuildAnAtomDefaults;
import edu.colorado.phet.buildanatom.modules.game.model.BuildAnAtomGameModel;
import edu.colorado.phet.buildanatom.modules.game.model.GuessResult;
import edu.colorado.phet.buildanatom.modules.game.model.Problem;
import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.nodes.GradientButtonNode;
import edu.umd.cs.piccolo.nodes.PText;

/**
* @author Sam Reid
*/
public class ProblemView extends StateView {

    PText text=new PText( "hello");
    // TODO: i18n
    private final GradientButtonNode checkButton = new GradientButtonNode( "Check", GameCanvas.BUTTONS_FONT_SIZE, GameCanvas.BUTTONS_COLOR ) {{
        addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                GuessResult guessResult=getModel().processGuess();
                text.setText( guessResult.toString() );
            }
        } );
    }};
    private final PText problemNumberDisplay;

    Problem problem;

    ProblemView( BuildAnAtomGameModel model, GameCanvas gameCanvas, Problem problem) {
        super( model, problem, gameCanvas );
        this.problem = problem;
        problemNumberDisplay = new PText( "Problem " + (model.getProblemIndex(problem)+1) + " of " + model.getNumberProblems()) {{
            setFont( new PhetFont( 20, true ) );
        }};
        problemNumberDisplay.setOffset( 30, 30 );
    }

    @Override
    public void init() {
        checkButton.setOffset( 700, 500 );
        getScoreboard().setOffset(
                BuildAnAtomDefaults.STAGE_SIZE.width / 2 - getScoreboard().getFullBoundsReference().width / 2,
                BuildAnAtomDefaults.STAGE_SIZE.height - ( 1.3 * getScoreboard().getFullBoundsReference().height ) );
        addChild( checkButton );
        addChild( getScoreboard() );
        addChild( problemNumberDisplay );
        addChild( text );
    }

    @Override
    public void teardown() {
        removeChild( getScoreboard() );
        removeChild( checkButton );
        removeChild( problemNumberDisplay );
        removeChild( text );
    }
}
