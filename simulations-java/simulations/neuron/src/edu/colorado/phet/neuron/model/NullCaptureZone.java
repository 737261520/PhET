/* Copyright 2010, University of Colorado */

package edu.colorado.phet.neuron.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Class that defines a capture zone that contains nothing.  This is useful
 * when wanting to avoid having to do a bunch of null checks.
 * 
 * @author John Blanco
 */
public class NullCaptureZone extends AbstractCaptureZone {

	@Override
	Shape getShape() {
		return new Ellipse2D.Double(0, 0, 0, 0);
	}

	@Override
	boolean isPointInZone(Point2D pt) {
		return false;
	}

	@Override
	void setCenterPoint(Point2D centerPoint) {
		// Does nothing.
	}

	@Override
	void setRotationalAngle(double angle) {
		// Does nothing.
	}
}
