// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.capacitorlab.model.wire;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import edu.colorado.phet.capacitorlab.CLConstants;
import edu.colorado.phet.capacitorlab.model.Battery;
import edu.colorado.phet.capacitorlab.model.CLModelViewTransform3D;
import edu.colorado.phet.capacitorlab.model.Capacitor;
import edu.colorado.phet.capacitorlab.model.wire.WireSegment.BatteryTopWireSegment;
import edu.colorado.phet.capacitorlab.model.wire.WireSegment.CapacitorTopWireSegment;

/**
 * Wire that connects the top of a battery (B) to the tops of N parallel capacitors (c1,c2,...,cn).
 * <code>
 * |-----|------|--...--|
 * |     |      |       |
 * B     c1    c2       cn
 * </code>
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class WireBatteryTopToCapacitorTops extends Wire {

    public WireBatteryTopToCapacitorTops( CLModelViewTransform3D mvt, double thickness, Battery battery, Capacitor... capacitors ) {
        this( mvt, thickness, battery, new ArrayList<Capacitor>( Arrays.asList( capacitors ) ) );
    }

    public WireBatteryTopToCapacitorTops( final CLModelViewTransform3D mvt, double thickness, Battery battery, ArrayList<Capacitor> capacitors ) {
        super( mvt, thickness );

        // find min Y
        double minY = battery.getY() - CLConstants.WIRE_EXTENT;
        for ( Capacitor capacitor : capacitors ) {
            minY = Math.min( minY, capacitor.getLocation().getY() - 0.01 ); //TODO clean this up
        }

        // connect battery to the rightmost capacitor
        final Capacitor rightmostCapacitor = capacitors.get( capacitors.size() - 1 );
        final Point2D.Double leftCorner = new Point2D.Double( battery.getX(), minY );
        final Point2D.Double rightCorner = new Point2D.Double( rightmostCapacitor.getX(), leftCorner.getY() );
        final double t = getCornerOffset(); // for proper connection at corners with wire stroke end style
        addSegment( new BatteryTopWireSegment( battery, getEndOffset(), leftCorner ) );
        addSegment( new WireSegment( leftCorner.getX() - t, leftCorner.getY() + t, rightCorner.getX() + t, rightCorner.getY() + t ) );
        addSegment( new CapacitorTopWireSegment( rightmostCapacitor, rightCorner ) );

        // add segments for all capacitors in between the battery and rightmost capacitor
        for ( int i = 0; i < capacitors.size() - 1; i++ ) {
            Capacitor capacitor = capacitors.get( i );
            Point2D.Double startPoint = new Point2D.Double( capacitor.getX(), minY );
            addSegment( new CapacitorTopWireSegment( capacitor, startPoint ) );
        }
    }
}