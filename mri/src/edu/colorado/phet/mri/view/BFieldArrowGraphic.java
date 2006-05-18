/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.mri.view;

import edu.colorado.phet.mri.model.Electromagnet;
import edu.colorado.phet.mri.model.GradientElectromagnet;
import edu.colorado.phet.mri.model.MriModel;
import edu.colorado.phet.piccolo.PhetPCanvas;
import edu.colorado.phet.piccolo.nodes.RegisterablePNode;
import edu.umd.cs.piccolox.pswing.PSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;

/**
 * BFieldArrowGraphic
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class BFieldArrowGraphic extends PhetPCanvas {
    private RegisterablePNode indicatorGraphic;
    private double maxArrowFractionOfHeight;
    private JTextField readout;
    private DecimalFormat readoutFormat = new DecimalFormat( "0.00" );
    private Point2D samplePoint = new Point2D.Double();

    /**
     * Constructor
     *
     * @param lowerMagnet
     * @param minLength
     */
    public BFieldArrowGraphic( MriModel model, double minLength ) {
//    public BFieldArrowGraphic( GradientElectromagnet lowerMagnet, final GradientElectromagnet upperMagnet, double minLength ) {

        setPreferredSize( new Dimension( 180, 80 ) );
//        setPreferredSize( new Dimension( 150, 150 ) );
        maxArrowFractionOfHeight = 0.9;
        final BFieldIndicator indicator = new BFieldIndicator( model,
                                                               samplePoint,
                                                               getPreferredSize().getHeight() * maxArrowFractionOfHeight,
                                                               new Color( 80, 80, 180 ) );
//        final BFieldIndicator indicator = new BFieldIndicator( lowerMagnet,
//                                                               getPreferredSize().getHeight() * maxArrowFractionOfHeight,
//                                                               new Color( 80, 80, 180 ),
//                                                               0 );
        indicator.setMinLength( minLength );
        indicatorGraphic = new RegisterablePNode( indicator );
        addWorldChild( indicatorGraphic );

        // Text readout for field
        readout = new JTextField( 6 );
        readout.setHorizontalAlignment( JTextField.CENTER );
        final PSwing readoutPSwing = new PSwing( this, readout );
        readoutPSwing.setOffset( getWidth() - 70, getHeight() / 2 );
//        readoutPSwing.setOffset( getWidth() - 50, getHeight() / 2 );
        addWorldChild( readoutPSwing );
        updateReadout( model );
//        updateReadout( lowerMagnet, upperMagnet );

        // When the panel is resized (or first displayed) update the placement of the arrow
        addComponentListener( new ComponentAdapter() {
            public void componentResized( ComponentEvent e ) {
                updateRegistrationPoint();
                indicator.setMaxLength( getHeight() * maxArrowFractionOfHeight );
                readoutPSwing.setOffset( getWidth() - 70, getHeight() / 2 );
//                readoutPSwing.setOffset( getWidth() - 70, getHeight() / 2 );
            }
        } );

        // Hook up listeners to the model
        model.addListener( new MriModel.ChangeAdapter() {
            public void fieldChanged( MriModel model ) {
                updateReadout( model );
            }
        } );

        samplePoint.setLocation( model.getBounds().getCenterX(), model.getBounds().getCenterY() );

//        lowerMagnet.addChangeListener( new Electromagnet.ChangeListener() {
//            public void stateChanged( Electromagnet.ChangeEvent event ) {
//                updateRegistrationPoint();
//                updateReadout( event.getElectromagnet(), upperMagnet );
//            }
//        } );
    }

    private void updateReadout( MriModel model ) {
//    private void updateReadout( Electromagnet magnet, Electromagnet upperMagnet ) {
//        samplePoint.setLocation( model.getSample().getBounds().getCenterX(),
//                                        model.getSample().getBounds().getCenterY());
        double fieldStrength = model.getTotalFieldStrengthAt( samplePoint );
//        double fieldStrength = magnet.getFieldStrength() + upperMagnet.getFieldStrength();
        String valueStr = readoutFormat.format( fieldStrength );
        readout.setText( valueStr + " Tesla" );
    }

    private void updateRegistrationPoint() {
        indicatorGraphic.setOffset( getWidth() / 2, getHeight() / 2 );
        indicatorGraphic.setRegistrationPoint( indicatorGraphic.getWidth() / 2, indicatorGraphic.getHeight() / 2 );
    }
}
