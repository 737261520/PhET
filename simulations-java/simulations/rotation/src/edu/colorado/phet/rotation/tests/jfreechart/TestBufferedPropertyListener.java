package edu.colorado.phet.rotation.tests.jfreechart;

import edu.colorado.phet.common.jfreechartphet.piccolo.JFreeChartNode;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * User: Sam Reid
 * Date: Jan 31, 2007
 * Time: 5:53:10 PM
 *
 */

public class TestBufferedPropertyListener {
    public static void main( String[] args ) {
        JFreeChartNode node = new JFreeChartNode( new JFreeChart( new CategoryPlot() ) );
        node.addBufferedPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent evt ) {
                System.out.println( "evt = " + evt );
            }
        } );
        System.out.println( "setting buffered: true" );
        node.setBuffered( true );
        System.out.println( "setting buffered: false" );
        node.setBuffered( false );
        System.out.println( "setting buffered: false" );
        node.setBuffered( false );
        System.out.println( "setting buffered: true" );
        node.setBuffered( true );
    }
}
