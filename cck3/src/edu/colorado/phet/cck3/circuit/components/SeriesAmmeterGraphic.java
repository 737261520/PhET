/** Sam Reid*/
package edu.colorado.phet.cck3.circuit.components;

import edu.colorado.phet.cck3.CCK3Module;
import edu.colorado.phet.cck3.circuit.IComponentGraphic;
import edu.colorado.phet.cck3.circuit.kirkhoff.KirkhoffSolutionListener;
import edu.colorado.phet.cck3.common.LineSegment;
import edu.colorado.phet.cck3.common.phetgraphics.CompositePhetGraphic;
import edu.colorado.phet.cck3.common.phetgraphics.PhetShapeGraphic;
import edu.colorado.phet.cck3.common.phetgraphics.PhetTextGraphic;
import edu.colorado.phet.cck3.common.phetgraphics.PhetTransformGraphic;
import edu.colorado.phet.common.math.AbstractVector2D;
import edu.colorado.phet.common.math.Vector2D;
import edu.colorado.phet.common.util.SimpleObserver;
import edu.colorado.phet.common.view.graphics.transforms.ModelViewTransform2D;
import edu.colorado.phet.common.view.graphics.transforms.TransformListener;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * User: Sam Reid
 * Date: May 29, 2004
 * Time: 12:57:37 AM
 * Copyright (c) May 29, 2004 by Sam Reid
 */
public class SeriesAmmeterGraphic extends CompositePhetGraphic implements IComponentGraphic {
    private SeriesAmmeter component;
    private ModelViewTransform2D transform;
    private CCK3Module module;
    private Stroke stroke = new BasicStroke( 5 );
    private Font font = new Font( "Lucida Sans", Font.BOLD, 17 );
    private Shape shape;
    private String text = "Ammeter";
    private String fixedMessage;
    private SimpleObserver simpleObserver;
    private TransformListener transformListener;
    private KirkhoffSolutionListener kirkhoffSolutionListener;
    private int numWindows = 3;

    private PhetShapeGraphic highlightRegion;
    private PhetShapeGraphic blackGraphic;
    private PhetShapeGraphic[] windowGraphics = new PhetShapeGraphic[numWindows];
    private PhetShapeGraphic areaGraphic;
    private PhetTransformGraphic textTX;
    private PhetTextGraphic textGraphic;

    public SeriesAmmeterGraphic( Component parent, final SeriesAmmeter component, ModelViewTransform2D transform, CCK3Module module, String fixedMessage ) {
        this( parent, component, transform, module );
        this.fixedMessage = fixedMessage;
    }

    public SeriesAmmeterGraphic( Component parent, final SeriesAmmeter component, ModelViewTransform2D transform, final CCK3Module module ) {
        super( parent );
        highlightRegion = new PhetShapeGraphic( parent, new Area(), Color.yellow );
        this.component = component;
        this.transform = transform;
        this.module = module;

        simpleObserver = new SimpleObserver() {
            public void update() {
                changed();
            }
        };
        component.addObserver( simpleObserver );
        transformListener = new TransformListener() {
            public void transformChanged( ModelViewTransform2D mvt ) {
                changed();
            }
        };
        transform.addTransformListener( transformListener );
        kirkhoffSolutionListener = new KirkhoffSolutionListener() {
            public void finishedKirkhoff() {
                DecimalFormat df = module.getDecimalFormat();
                String form = df.format( Math.abs( component.getCurrent() ) );
                text = "" + form + " Amps";
                changed();
            }
        };
        module.getKirkhoffSolver().addSolutionListener( kirkhoffSolutionListener );
        blackGraphic = new PhetShapeGraphic( parent, new Area(), stroke, Color.black );

        addGraphic( highlightRegion );
        addGraphic( blackGraphic );
        areaGraphic = new PhetShapeGraphic( parent, new Area(), Color.black );
        addGraphic( areaGraphic );
        for( int i = 0; i < windowGraphics.length; i++ ) {
            windowGraphics[i] = new PhetShapeGraphic( parent, new Area(), new BasicStroke( 1.2f ), Color.black );
            addGraphic( windowGraphics[i] );
        }
        textGraphic = new PhetTextGraphic( parent, font, "", Color.black, 0, 0 );
        textGraphic.setVisible( true );
        textTX = new PhetTransformGraphic( textGraphic, new AffineTransform() );
        addGraphic( textTX );
        changed();
        setVisible( true );
    }

    public void setFont( Font font ) {
        textGraphic.setFont( font );
    }

    private void changed() {
        double newHeight = transform.modelToViewDifferentialY( component.getHeight() );
        Point2D start = transform.modelToView( component.getStartJunction().getPosition() );
        Point2D end = transform.modelToView( component.getEndJunction().getPosition() );
        this.shape = LineSegment.getSegment( start, end, newHeight );
        BasicStroke stroke = new BasicStroke( 12 );
        highlightRegion.setShape( stroke.createStrokedShape( shape ) );
        highlightRegion.setVisible( component.isSelected() );

        blackGraphic.setShape( shape );

        Vector2D dir = new Vector2D.Double( start, end ).normalize();
        AbstractVector2D north = dir.getNormalVector();

        double angle = new Vector2D.Double( start, end ).getAngle();
        Rectangle r = shape.getBounds();

        Area area = new Area( shape );
        double windowHeightFraction = .3;
        int windowHeight = transform.modelToViewDifferentialY( component.getHeight() * windowHeightFraction );
        double length = start.distance( end );
        double windowWidth = length / ( numWindows + 1.0 );
        double spacingWidth = ( length - windowWidth * numWindows ) / ( numWindows + 1 );
        double x = 0;
        north = north.getInstanceOfMagnitude( windowHeight / 2 ).getScaledInstance( -1 );
        ArrayList windows = new ArrayList();
        for( int i = 0; i < numWindows; i++ ) {
            x += spacingWidth;
            Point2D a = dir.getInstanceOfMagnitude( x ).getDestination( start );
            a = north.getDestination( a );
            x += windowWidth;
            Point2D b = dir.getInstanceOfMagnitude( x ).getDestination( start );
            b = north.getDestination( b );
            Shape seg = LineSegment.getSegment( a, b, windowHeight );
            windows.add( seg );
            area.subtract( new Area( seg ) );
            windowGraphics[i].setShape( seg );
        }

        Point a = r.getLocation();
        Point b = new Point( (int)( a.getX() + r.getWidth() ), (int)( a.getY() + r.getHeight() ) );
        Color startColor = new Color( 255, 230, 250 );
        Color endColor = new Color( 230, 255, 230 );
        areaGraphic.setPaint( new GradientPaint( a, startColor, b, endColor ) );
        areaGraphic.setShape( area );

        Point2D textLoc = north.getScaledInstance( -2.9 ).getDestination( start );
        textLoc = dir.getInstanceOfMagnitude( 2 ).getDestination( textLoc );

        String msg = text;
        if( fixedMessage != null ) {
            msg = fixedMessage;
        }
        textGraphic.setText( msg );
        textGraphic.setPosition( (int)textLoc.getX(), (int)textLoc.getY() );
        AffineTransform at = AffineTransform.getRotateInstance( angle, textLoc.getX(), textLoc.getY() );
        textTX.setTransform( at );
    }

    public ModelViewTransform2D getModelViewTransform2D() {
        return transform;
    }

    public CircuitComponent getCircuitComponent() {
        return component;
    }

    public void delete() {
        component.removeObserver( simpleObserver );
        transform.removeTransformListener( transformListener );
        module.getKirkhoffSolver().removeSolutionListener( kirkhoffSolutionListener );
    }

}
