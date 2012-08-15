package edu.colorado.phet.functions.intro;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.colorado.phet.common.phetcommon.model.Resettable;
import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.nodes.HTMLImageButtonNode;
import edu.colorado.phet.common.piccolophet.nodes.ResetAllButtonNode;
import edu.colorado.phet.common.piccolophet.nodes.layout.HBox;
import edu.colorado.phet.functions.buildafunction2.AbstractFunctionsCanvas;
import edu.colorado.phet.functions.buildafunction2.BuildAFunctionPrototype2Canvas;
import edu.colorado.phet.functions.buildafunction2.UnaryNumberFunctionNode;
import edu.colorado.phet.functions.buildafunction2.ValueContext;
import edu.colorado.phet.functions.buildafunction2.ValueNode;
import edu.umd.cs.piccolo.util.PDimension;

import static edu.colorado.phet.functions.FunctionModule.radioButton;

/**
 * @author Sam Reid
 */
public class IntroCanvas extends AbstractFunctionsCanvas implements ValueContext {

    private Scene scene;
    private final ResetAllButtonNode resetAllButtonNode;

    public IntroCanvas() {

        setBackground( BuildAFunctionPrototype2Canvas.BACKGROUND_COLOR );
        addScreenChild( new HBox( radioButton( "Shapes", false ), radioButton( "Text", true ), radioButton( "Numbers", false ) ) {{
            setOffset( INSET, INSET );
        }} );
        resetAllButtonNode = new ResetAllButtonNode( new Resettable() {
            public void reset() {
            }
        }, this, new PhetFont( 18, true ), Color.black, Color.orange ) {{
            setOffset( 1024 - this.getFullWidth() - INSET - INSET - INSET, 768 - this.getFullHeight() - INSET - 40 - 40 - 10 - INSET );
        }};
        addScreenChild( resetAllButtonNode );

        scene = new Scene1( this );
        addChild( scene );
    }

    @Override public void mouseDragged( final ValueNode valueNode, final PDimension delta ) {
        scene.mouseDragged( valueNode, delta );
    }

    @Override public void mouseReleased( final ValueNode valueNode ) {
        scene.mouseReleased( valueNode );
    }

    public void showNextButton() {
        final HTMLImageButtonNode nextButton = new HTMLImageButtonNode( "Next" ) {{
            setFont( resetAllButtonNode.getFont() );
            setBackground( Color.green );
            setOffset( resetAllButtonNode.getFullBounds().getX(), resetAllButtonNode.getFullBounds().getMinY() - getFullBounds().getHeight() - 10 );
        }};
        nextButton.addActionListener( new ActionListener() {
            @Override public void actionPerformed( final ActionEvent e ) {
                IntroCanvas.this.removeChild( scene );
                scene = new Scene2();
                IntroCanvas.this.addChild( scene );
                IntroCanvas.this.removeChild( nextButton );
            }
        } );
        addChild( nextButton );
    }

    private class Scene2 extends Scene {
        private Scene2() {
            addChild( new UnaryNumberFunctionNode( "\u27152", false ) {{
                setOffset( 390.72378138847836, 294.298375184638 );
            }} );

            addChild( new UnaryNumberFunctionNode( "+1", false ) {{
                setOffset( 390.72378138847836, 444.298375184638 );
            }} );

            addChild( new ValueNode( IntroCanvas.this, 3, new BasicStroke( 1 ), Color.white, Color.black, Color.black ) {{
                setOffset( 84.37223042836038, 315.3914327917278 );
            }} );

            addChild( new ValueNode( IntroCanvas.this, 6, new BasicStroke( 1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 10, 10 }, 0 ), new Color( 0, 0, 0, 0 ), Color.gray, Color.gray ) {{
                setOffset( 903.9881831610056, 318.4047267355978 );
            }} );
        }

        @Override public void mouseDragged( final ValueNode valueNode, final PDimension delta ) {
        }

        @Override public void mouseReleased( final ValueNode valueNode ) {
        }
    }
}