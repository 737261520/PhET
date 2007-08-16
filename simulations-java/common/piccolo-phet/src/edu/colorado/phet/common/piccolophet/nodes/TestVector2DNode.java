/* Copyright 2007, University of Colorado */

package edu.colorado.phet.common.piccolophet.nodes;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.colorado.phet.common.phetcommon.math.PolarCartesianConverter;
import edu.colorado.phet.common.phetcommon.view.controls.valuecontrol.LinearValueControl;
import edu.umd.cs.piccolo.PCanvas;

/**
 * TestVector2DNode is a test application for Vector2DNode.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class TestVector2DNode extends JFrame {

    private Vector2DNode _vectorNode;
    private LinearValueControl _magnitudeControl;
    private LinearValueControl _angleControl;
    
    public TestVector2DNode() {
        super();
        
        final double x = 100;
        final double y = 0;
        final double magnitude = PolarCartesianConverter.getRadius( x, y );
        final double angle = PolarCartesianConverter.getAngle( x, y );
        
        final double referenceMagnitude = 100;
        final double referenceLength = 100;
        
        _vectorNode = new Vector2DNode( x, y, referenceMagnitude, referenceLength );
        _vectorNode.setValueSpacing( 5 );
        _vectorNode.setValueVisible( true );

        PCanvas canvas = new PCanvas();
        canvas.setPreferredSize( new Dimension( 400, 300 ) );
        canvas.getLayer().addChild( _vectorNode );
        _vectorNode.setOffset( 200, 150 );
        
        _magnitudeControl = new LinearValueControl( 0, 100, "magnitude:", "##0", "" );
        _magnitudeControl.setUpDownArrowDelta( 1 );
        _magnitudeControl.setValue( magnitude );
        _magnitudeControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent event ) {
                updateVectorNode();
            }
        });
        
        _angleControl = new LinearValueControl( -720, 720, "angle:", "##0", "degrees" );
        _angleControl.setUpDownArrowDelta( 1 );
        _angleControl.setValue( angle );
        _angleControl.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent event ) {
                updateVectorNode();
            }
        });
        
        Box controlPanel = new Box( BoxLayout.Y_AXIS );
        controlPanel.add( new JSeparator() );
        controlPanel.add( _magnitudeControl );
        controlPanel.add( new JSeparator() );
        controlPanel.add( _angleControl );
        
        JPanel panel = new JPanel();
        panel.setLayout( new BorderLayout() );
        panel.add( canvas, BorderLayout.CENTER );
        panel.add( controlPanel, BorderLayout.SOUTH );
        
        setContentPane( panel );
        pack();
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }
    
    private void updateVectorNode() {
        double magnitude = _magnitudeControl.getValue();
        double angle = Math.toRadians( _angleControl.getValue() );
        _vectorNode.setMagnitudeAngle( magnitude, angle );
    }
    
    public static void main( String[] args ) {
        TestVector2DNode test = new TestVector2DNode();
        test.show();
    }
}
