// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.lightreflectionandrefraction.modules.intro;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.colorado.phet.common.phetcommon.model.Property;
import edu.colorado.phet.common.phetcommon.util.Function1;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.phetcommon.view.util.SwingUtils;
import edu.colorado.phet.common.piccolophet.PhetPCanvas;
import edu.colorado.phet.lightreflectionandrefraction.model.LRRModel;
import edu.colorado.phet.lightreflectionandrefraction.model.MediumState;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.pswing.PComboBox;
import edu.umd.cs.piccolox.pswing.PSwing;

import static edu.colorado.phet.lightreflectionandrefraction.modules.intro.ControlPanelNode.labelFont;

/**
 * @author Sam Reid
 */
public class MediumControlPanel extends PNode {

    MediumState CUSTOM = new MediumState( "Custom", LRRModel.MYSTERY_B.index + 1.2 ) {
        public boolean isCustom() {
            return true;
        }
    };

    public MediumControlPanel( final PhetPCanvas phetPCanvas, final Property<Medium> medium, final Property<Function1<Double, Color>> colorMappingFunction ) {
        final MediumState initialMediumState = medium.getValue().getMediumState();
        final PText materialLabel = new PText( "Material:" ) {{
            setFont( labelFont );
        }};
        addChild( materialLabel );
        final Object[] mediumStates = new Object[] {
                LRRModel.AIR,
                LRRModel.WATER,
                LRRModel.GLASS,
                LRRModel.MYSTERY_A,
                LRRModel.MYSTERY_B,
                CUSTOM,
        };
        final PComboBox comboBox = new PComboBox( mediumStates ) {
            {
                addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        MediumState selected = (MediumState) getSelectedItem();
                        if ( !selected.isCustom() ) {
                            setMediumState( selected, medium, colorMappingFunction );
                        }
                    }
                } );
                updateComboBox();
                medium.addObserver( new SimpleObserver() {
                    public void update() {
                        updateComboBox();
                    }
                } );
                setFont( labelFont );
                setMediumState( initialMediumState, medium, colorMappingFunction );
            }

            private void updateComboBox() {
                int selected = -1;
                for ( int i = 0; i < mediumStates.length; i++ ) {
                    MediumState mediumState = (MediumState) mediumStates[i];
                    if ( mediumState.index == medium.getValue().getIndexOfRefraction() ) {
                        selected = i;
                    }
                }
                if ( selected != -1 ) {
                    setSelectedIndex( selected );
                }
                else {
                    setSelectedItem( CUSTOM );
                }
            }
        };
        final PSwing comboBoxPSwing = new PSwing( comboBox ) {{
            comboBox.setEnvironment( this, phetPCanvas );
            setOffset( materialLabel.getFullBounds().getMaxX() + 10, materialLabel.getFullBounds().getCenterY() - getFullBounds().getHeight() / 2 + 1 );
        }};
        addChild( comboBoxPSwing );

        final PSwing slider = new PSwing( new IndexOfRefractionSlider( medium, colorMappingFunction, "" ) {{
            SwingUtils.setBackgroundDeep( this, new Color( 0, 0, 0, 0 ) );
            getTextField().setBackground( Color.white );
            getTextField().setFont( labelFont );
        }} ) {{
            medium.addObserver( new SimpleObserver() {
                public void update() {
                    setVisible( !medium.getValue().isMystery() );
                }
            } );
            setOffset( 0, materialLabel.getFullBounds().getMaxY() );
        }};
        addChild( slider );

        final PText unknown = new PText( "n=?" ) {{
            setFont( labelFont );
            centerFullBoundsOnPoint( slider.getFullBounds().getCenterX(), slider.getFullBounds().getCenterY() );
            medium.addObserver( new SimpleObserver() {
                public void update() {
                    setVisible( medium.getValue().isMystery() );
                }
            } );
        }};
        addChild( unknown );

        final PText indexOfRefractionLabel = new PText( "Index of Refraction (n)" ) {{
            setFont( labelFont );
            setOffset( 0, slider.getFullBounds().getMaxY() );
        }};
        addChild( indexOfRefractionLabel );
    }

    private void setMediumState( MediumState mediumState, Property<Medium> medium, Property<Function1<Double, Color>> colorMappingFunction ) {
        medium.setValue( new Medium( medium.getValue().getShape(), mediumState, colorMappingFunction.getValue().apply( mediumState.index ) ) );
    }
}