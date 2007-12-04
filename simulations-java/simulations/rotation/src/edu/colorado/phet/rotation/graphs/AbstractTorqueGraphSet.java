package edu.colorado.phet.rotation.graphs;

import java.awt.*;

import org.jfree.data.Range;

import edu.colorado.phet.common.jfreechartphet.piccolo.dynamic.DynamicJFreeChartNode;
import edu.colorado.phet.common.motion.graphs.ControlGraph;
import edu.colorado.phet.common.motion.graphs.ControlGraphSeries;
import edu.colorado.phet.common.motion.graphs.JFreeChartSliderNode;
import edu.colorado.phet.common.phetcommon.math.MathUtil;
import edu.colorado.phet.common.piccolophet.PhetPCanvas;
import edu.colorado.phet.rotation.RotationStrings;
import edu.colorado.phet.rotation.model.AngleUnitModel;
import edu.colorado.phet.rotation.model.RotationModel;
import edu.colorado.phet.rotation.torque.TorqueModel;
import edu.colorado.phet.rotation.util.UnicodeUtil;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * User: Sam Reid
 * Date: Dec 28, 2006
 * Time: 8:23:39 AM
 */

public class AbstractTorqueGraphSet extends AbstractRotationGraphSet {
    private TorqueModel tm;
    private final String ANG_MOM = RotationStrings.getString( "angular.momentum" );
    private final String ANG_MOM_VARNAME = RotationStrings.getString( "l" );
    private final String MOMENT_OF_INERTIA = RotationStrings.getString( "moment.of.inertia" );
    private final String MOMENT_OF_INERTIA_VARNAME = RotationStrings.getString( "i" );
    private final String APPLIED_TORQUE = RotationStrings.getString( "applied.torque" );
    private final String BRAKE_TORQUE = RotationStrings.getString( "brake.torque" );
    private final String NET_TORQUE = RotationStrings.getString( "net.torque" );
    private final String BRAKE_RADIUS = RotationStrings.getString( "brake.radius" );
    private final String APPLIED_FORCE = RotationStrings.getString( "applied.force" );
    private final String BRAKE_FORCE = RotationStrings.getString( "brake.force" );
    private final String NET_FORCE = RotationStrings.getString( "net.force" );
    private final String UNITS_GENERIC = RotationStrings.getString( "units");
    private final String TORQUE = RotationStrings.getString( "torque" );
    private final String APPLIED = RotationStrings.getString( "applied" );
    private final String BRAKE = RotationStrings.getString( "brake" );
    private final String NET = RotationStrings.getString( "net" );
    private final String FORCE_RADIUS = RotationStrings.getString( "radius.of.force" );
    private final String F = RotationStrings.getString( "f" );
    private final String N = RotationStrings.getString( "n" );
    private final String MOM_INERTIA_UNITS = RotationStrings.getString( "kg.m.2" );
    private final String TORQUE_UNITS = RotationStrings.getString( "n.m" );
    private final String RADIUS_VARNAME = RotationStrings.getString( "r" );
    private final String RADIUS_UNITS = RotationStrings.getString( "m");
    private final String FORCE = RotationStrings.getString( "force" );

    public AbstractTorqueGraphSet( PhetPCanvas pSwingCanvas, final TorqueModel tm, AngleUnitModel angleUnitModel ) {
        super( pSwingCanvas, tm, angleUnitModel );
        this.tm = tm;
        initFinished();
    }

    protected void initFinished() {
        addSeriesSelectionPanels();
        updateBody1Series();
    }

    protected RotationMinimizableControlGraph createAngMomGraph() {
        PhetPCanvas pSwingCanvas = super.getCanvas();
        return new RotationMinimizableControlGraph( ANG_MOM_VARNAME, new RotationGraph(
                pSwingCanvas, new ControlGraphSeries( ANG_MOM, Color.red, ANG_MOM_VARNAME, UNITS_GENERIC, new BasicStroke( 2 ), null, tm.getAngularMomentumTimeSeries() ),
                ANG_MOM_VARNAME, ANG_MOM, UNITS_GENERIC, -0.1, 0.1,
                tm, false, tm.getTimeSeriesModel(), null, RotationModel.MAX_TIME, tm.getRotationPlatform() ) );
    }

    protected RotationMinimizableControlGraph createMomentGraph() {
        PhetPCanvas pSwingCanvas = super.getCanvas();
        return new RotationMinimizableControlGraph( MOMENT_OF_INERTIA_VARNAME, new RotationGraph(
                pSwingCanvas, new ControlGraphSeries( MOMENT_OF_INERTIA, Color.green, MOMENT_OF_INERTIA_VARNAME, MOM_INERTIA_UNITS, new BasicStroke( 2 ), null, tm.getMomentOfInertiaTimeSeries() ),
                MOMENT_OF_INERTIA_VARNAME, MOMENT_OF_INERTIA, UNITS_GENERIC, -5, 5,
                tm, false, tm.getTimeSeriesModel(), null, RotationModel.MAX_TIME, tm.getRotationPlatform() ) );
    }

    protected RotationMinimizableControlGraph createTorqueGraph() {
        PhetPCanvas pSwingCanvas = super.getCanvas();
        RotationMinimizableControlGraph torqueGraph = new RotationMinimizableControlGraph( UnicodeUtil.TAU, new RotationGraph(
                pSwingCanvas, new ControlGraphSeries( APPLIED_TORQUE, Color.blue, UnicodeUtil.TAU, UNITS_GENERIC, new BasicStroke( 4 ), APPLIED, tm.getAppliedTorqueTimeSeries() ),
                UnicodeUtil.TAU, TORQUE, UNITS_GENERIC, -10, 10,
                tm, false, tm.getTimeSeriesModel(), null, RotationModel.MAX_TIME, tm.getRotationPlatform() ) );

        ControlGraphSeries brakeTorqueSeries = new ControlGraphSeries( BRAKE_TORQUE, Color.red, UnicodeUtil.TAU, TORQUE_UNITS, new BasicStroke( 2 ), false, BRAKE, tm.getBrakeTorque() );
        torqueGraph.addSeries( brakeTorqueSeries );
        ControlGraphSeries netTorqueSeries = new ControlGraphSeries( NET_TORQUE, Color.black, UnicodeUtil.TAU, TORQUE_UNITS, new BasicStroke( 2 ), false, NET, tm.getNetTorque() );
        torqueGraph.addSeries( netTorqueSeries );
        torqueGraph.addControl( new SeriesJCheckBox( brakeTorqueSeries ) );
        torqueGraph.addControl( new SeriesJCheckBox( netTorqueSeries ) );
        return torqueGraph;
    }

    protected RotationMinimizableControlGraph createRadiusGraph() {
        PhetPCanvas pSwingCanvas = super.getCanvas();
        RotationMinimizableControlGraph radiusGraph = new RotationMinimizableControlGraph( RADIUS_VARNAME, new RotationGraph(
                pSwingCanvas, new ControlGraphSeries( FORCE_RADIUS, Color.green, RADIUS_VARNAME, RADIUS_UNITS, new BasicStroke( 2 ), true, APPLIED, tm.getRadiusSeries() ),
                RADIUS_VARNAME, FORCE_RADIUS, RADIUS_UNITS, 0, 4.5,
                tm, true, tm.getTimeSeriesModel(), tm.getForceDriven(), RotationModel.MAX_TIME, tm.getRotationPlatform() ) {
            protected Range getVerticalRange( double zoomValue ) {
                Range range = super.getVerticalRange( zoomValue );
                return new Range( 0, range.getUpperBound() );
            }

            protected JFreeChartSliderNode createSliderNode( PNode thumb ) {
                return new JFreeChartSliderNodeForRadius( getDynamicJFreeChartNode(), thumb == null ? new PPath() : thumb );//todo: better support for non-controllable graphs
            }

            protected void handleValueChanged() {
                notifyValueChanged( getModelValue() );
            }
        } );
        radiusGraph.getControlGraph().addListener( new ControlGraph.Adapter() {
            public void valueChanged( double value ) {
                tm.setAppliedForceRadius( MathUtil.clamp( tm.getRotationPlatform().getInnerRadius(), value, tm.getRotationPlatform().getRadius() ) );
            }
        } );
        radiusGraph.addSeries( new ControlGraphSeries( BRAKE_RADIUS, Color.red, RADIUS_VARNAME, RADIUS_UNITS, new BasicStroke( 3 ), false, BRAKE, tm.getBrakeRadiusSeries() ) );
        return radiusGraph;
    }

    protected RotationMinimizableControlGraph createForceGraph() {
        PhetPCanvas pSwingCanvas = super.getCanvas();
        final RotationMinimizableControlGraph forceGraph = new RotationMinimizableControlGraph( F, new RotationGraph(
                pSwingCanvas, new ControlGraphSeries( APPLIED_FORCE, Color.blue, F, N, new BasicStroke( 4 ), true, APPLIED, tm.getAppliedForceVariable() ),
                F, FORCE, UNITS_GENERIC, -2.5, 2.5,
                tm, true, tm.getTimeSeriesModel(), tm.getForceDriven(), RotationModel.MAX_TIME, tm.getRotationPlatform() ) );
        forceGraph.getControlGraph().addListener( new ControlGraph.Adapter() {
            public void valueChanged( double value ) {
                tm.setAppliedForce( tm.getAppliedForceRadius(), value );
            }
        } );

        ControlGraphSeries brakeForceSeries = new ControlGraphSeries( BRAKE_FORCE, Color.red, F, N, new BasicStroke( 3 ), false, BRAKE, tm.getBrakeForceMagnitudeVariable() );
        forceGraph.addSeries( brakeForceSeries );
        ControlGraphSeries netForceSeries = new ControlGraphSeries( NET_FORCE, Color.black, F, N, new BasicStroke( 2 ), false, NET, tm.getNetForce() );
        forceGraph.getControlGraph().addSeries( netForceSeries );

        forceGraph.getControlGraph().addControl( new SeriesJCheckBox( brakeForceSeries ) );
        forceGraph.getControlGraph().addControl( new SeriesJCheckBox( netForceSeries ) );
        return forceGraph;
    }

    private class JFreeChartSliderNodeForRadius extends JFreeChartSliderNode {
        public JFreeChartSliderNodeForRadius( DynamicJFreeChartNode dynamicJFreeChartNode, PNode pNode ) {
            super( dynamicJFreeChartNode, pNode );
        }

        protected double getMaxRangeValue() {
            return Math.min( 4, super.getMaxRangeValue() );
        }
    }
}
