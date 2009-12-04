/* Copyright 2009, University of Colorado */

package edu.colorado.phet.genenetwork.model;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import edu.colorado.phet.common.phetcommon.math.Vector2D;
import edu.colorado.phet.common.phetcommon.view.util.DoubleGeneralPath;


/**
 * Class that represents LacZ, which is the model element that breaks up the
 * lactose.
 * 
 * @author John Blanco
 */
public abstract class SimpleSugar extends SimpleModelElement {
	
	public static double HEIGHT = 2;
	
	private static double sideLength;
	
	public SimpleSugar(IGeneNetworkModelControl model, Point2D initialPosition, Paint paint) {
		super(model, createShape(), initialPosition, paint, false, Double.POSITIVE_INFINITY);
	}
	
	private static Shape createShape(){
		// Create a hexagon shape.
		DoubleGeneralPath path = new DoubleGeneralPath();
		sideLength = HEIGHT / 2 / Math.sin(Math.PI/3);
		path.moveTo(-sideLength / 2, -HEIGHT / 2);
		
		double angle = Math.PI;
		
		for (int i = 0; i < 6; i++){
			angle = lineToRelative(path, sideLength, angle);
		}

        return AffineTransform.getTranslateInstance(-path.getGeneralPath().getBounds2D().getCenterX(), 
        		-path.getGeneralPath().getBounds2D().getCenterY()).createTransformedShape(path.getGeneralPath());
	}
	
	public static double getSideLength(){
		return sideLength;
	}
	
	public static double getWidth(){
		return (getSideLength() * (1 + 2 * Math.cos(Math.PI/3)));

	}

	private static double lineToRelative(DoubleGeneralPath path, double length, double angle) {
		path.lineToRelative(Vector2D.Double.parseAngleAndMagnitude(length, angle));
		return angle + Math.PI / 3;
	}
}
