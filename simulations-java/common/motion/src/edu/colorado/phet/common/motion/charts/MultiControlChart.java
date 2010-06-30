package edu.colorado.phet.common.motion.charts;

import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.umd.cs.piccolo.PNode;

import java.util.ArrayList;

/**
 * @author Sam Reid
 */
public class MultiControlChart extends PNode {
    private ArrayList<MinimizableControlChart> children = new ArrayList<MinimizableControlChart>();
    private double width;
    private double height;

    public MultiControlChart(final MinimizableControlChart[] charts) {
        final SimpleObserver updateHorizontalZoomButtonVisibility = new SimpleObserver() {
            public void update() {
                //Only show the topmost horizontal zoom control because it controls all axes and cleans up the screen to omit the extraneous controls
                for (int i = 0; i < charts.length; i++) {
                    if (charts[i].getMaximized().getValue()) {
                        setHorizontalZoomButtonVisible(i);
                        break;
                    }
                }
            }
        };
        for (MinimizableControlChart chart : charts) {
            children.add(chart);
            addChild(chart);
            chart.getMaximized().addObserver(updateHorizontalZoomButtonVisibility);
            chart.getMaximized().addObserver(new SimpleObserver() {
                public void update() {
                    updateLayout();
                }
            } );
        }
        updateHorizontalZoomButtonVisibility.update();

        for (final MinimizableControlChart chart : charts) {
            chart.getChartNode().getDataModelBounds().addObserver(new SimpleObserver() {
                public void update() {
                    for (MinimizableControlChart chart2 : charts) {
                        chart2.getChartNode().getDataModelBounds().setHorizontalRange(chart.getChartNode().getDataModelBounds().getMinX(),chart.getChartNode().getDataModelBounds().getMaxX() );
                    }
                }
            });
        }
    }

    private void updateLayout() {
        new ControlChartLayout.AlignedLayout().updateLayout(children.toArray(new MinimizableControlChart[0]), width, height);
    }

    private void setHorizontalZoomButtonVisible(int index) {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setHorizontalZoomVisible(index == i);
        }
    }

    public void setSize(double width, double height) {
        this.width=width;
        this.height=height;
        updateLayout();
    }

    public boolean setBounds(double x, double y, double width, double height) {
        setSize(width,height);
        setOffset(x,y);
        return true;
    }
}