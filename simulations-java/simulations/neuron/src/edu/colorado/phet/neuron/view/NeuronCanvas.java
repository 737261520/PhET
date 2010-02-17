/* Copyright 2009, University of Colorado */

package edu.colorado.phet.neuron.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform2D;
import edu.colorado.phet.common.piccolophet.PhetPCanvas;
import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.colorado.phet.neuron.NeuronConstants;
import edu.colorado.phet.neuron.model.MembraneChannel;
import edu.colorado.phet.neuron.model.AxonModel;
import edu.colorado.phet.neuron.model.Particle;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * Canvas on which the neuron simulation is depicted.
 *
 * @author John Blanco
 */
public class NeuronCanvas extends PhetPCanvas {

    //----------------------------------------------------------------------------
    // Class Data
    //----------------------------------------------------------------------------

	// Initial size of the reference coordinates that are used when setting up
	// the canvas transform strategy.  These were empirically determined to
	// roughly match the expected initial size of the canvas.
    private static final int INITIAL_INTERMEDIATE_COORD_WIDTH = 786;
    private static final int INITIAL_INTERMEDIATE_COORD_HEIGHT = 786;
    private static final Dimension INITIAL_INTERMEDIATE_DIMENSION = new Dimension( INITIAL_INTERMEDIATE_COORD_WIDTH,
    		INITIAL_INTERMEDIATE_COORD_HEIGHT );
    
    // Size of the chart the depicts the membrane potential.
    private static final Dimension2D POTENTIAL_CHART_SIZE = new PDimension(INITIAL_INTERMEDIATE_COORD_WIDTH,
    		INITIAL_INTERMEDIATE_COORD_HEIGHT * 0.3);
    
    // For debug: Enable and disable nodes that can help with debug of layout.
    private static final boolean SHOW_PARTICLE_BOUNDS = false;
    private static final boolean SHOW_CENTER_CROSS_HAIR = false;
    
    //----------------------------------------------------------------------------
    // Instance Data
    //----------------------------------------------------------------------------

    // Model
    private AxonModel model;
    
    // Model-view transform.
    private ModelViewTransform2D mvt;
    
    // Layers for the canvas.
    private PNode atomLayer;
    private PNode axonCrossSectionLayer;
    private PNode chartLayer;
    
    // Chart and voltmeter for showing membrane potential.
    private MembranePotentialChart membranePotentialChart;
    private MembraneVoltmeter voltmeter;
    
    // For debug: Shows center of zoom.
    private CrossHairNode crossHairNode;
    private PNode myWorldNode=new PNode();

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    public NeuronCanvas( final AxonModel model ) {

    	this.model = model;

    	// Set up the canvas-screen transform.
    	setWorldTransformStrategy(new PhetPCanvas.CenteringBoxStrategy(this, INITIAL_INTERMEDIATE_DIMENSION));
    	
    	// Set up the model-canvas transform.
        mvt = new ModelViewTransform2D(
        		new Point2D.Double(0, 0), 
        		new Point(INITIAL_INTERMEDIATE_COORD_WIDTH / 2, 
        				(int)Math.round(INITIAL_INTERMEDIATE_COORD_HEIGHT * 0.55 )),
        		7,  // Scale factor - smaller numbers "zoom out", bigger ones "zoom in".
        		true);

        // Register for events from the model.
        this.model.addListener(new AxonModel.Adapter() {
			public void channelAdded(MembraneChannel channel) {
				addChannelNode(channel);
			}
			public void particleAdded(Particle particle) {
				addAtom(particle);
			}
			public void potentialChartVisibilityChanged(){
				membranePotentialChart.setVisible(model.isPotentialChartVisible());
				if (!model.isPotentialChartVisible()){
					setZoomFactor(1);
				}
				else{
					setZoomFactor(0.65);
				}
			}
		});
        
        setBackground( NeuronConstants.CANVAS_BACKGROUND );

        // Create the node that will be the root for all the world children on
        // this canvas.  This is done to make it easier to zoom in and out on
        // the world without affecting screen children.
        myWorldNode = new PNode();
        addWorldChild(myWorldNode);

        // Create the layers in the desired order.
        axonCrossSectionLayer = new PNode();
        atomLayer = new PNode();

        myWorldNode.addChild(axonCrossSectionLayer);
        myWorldNode.addChild(atomLayer);

        chartLayer = new PNode();
        addScreenChild(chartLayer);
        
        // Add the axon cross section.
        AxonMembraneNode axonMembraneNode = new AxonMembraneNode(model.getAxonMembrane(), mvt);
        axonCrossSectionLayer.addChild(axonMembraneNode);
        
        // Add the atoms.
        for (Particle atom : model.getParticles()){
        	addAtom(atom);
        }
        
        // Add the channels.
        for (MembraneChannel channel : model.getMembraneChannels()){
        	addChannelNode(channel);
        }
        
        // Add the membrane potential chart.
        membranePotentialChart = new MembranePotentialChart(POTENTIAL_CHART_SIZE, "Membrane Potential", model, "ms");
        membranePotentialChart.setVisible(false);
        chartLayer.addChild(membranePotentialChart);
        
        // Add the voltmeter.
        voltmeter = new MembraneVoltmeter(model);
        voltmeter.setVisible(false);
        chartLayer.addChild(voltmeter);
        
        // Add the depiction of the particle motion bounds, if enabled.
        if (SHOW_PARTICLE_BOUNDS){
        	PhetPPath particleMotionBounds = new PhetPPath(mvt.createTransformedShape(model.getParticleMotionBounds()),
        			new BasicStroke(3), Color.red);
        	atomLayer.addChild(particleMotionBounds);
        }
        
        if (SHOW_CENTER_CROSS_HAIR){
        	// Add the crosshair, used for debugging zoom.
        	crossHairNode = new CrossHairNode();
        	crossHairNode.setOffset(mvt.modelToViewDouble(new Point2D.Double(0, 0)));
        	chartLayer.addChild(crossHairNode);
        }
        
        // Update the layout.
        updateLayout();
    }
    
    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    //----------------------------------------------------------------------------
    // Canvas layout
    //----------------------------------------------------------------------------
    
    /**
     * Updates the layout of stuff on the canvas.
     */
    protected void updateLayout() {

        Dimension2D worldSize = getWorldSize();
        Dimension2D screenSize = getScreenSize();
        Rectangle bounds = getBounds();
        Dimension size = getSize();
        System.out.println("bounds = " + bounds + ", size = " + size + " + world size = " + worldSize + ", screen size = " + screenSize);
        if ( worldSize.getWidth() <= 0 || worldSize.getHeight() <= 0 ) {
            // canvas hasn't been sized, blow off layout
            return;
        }
        else {
            double rightEdgeX = (INITIAL_INTERMEDIATE_COORD_WIDTH + worldSize.getWidth()) / 2;
            double centerX = getScreenSize().getWidth() / 2;
            
            // Set the membrane potential chart such that it is centered in
            // the play area and just a bit up from the bottom.
            membranePotentialChart.setOffset(
            		centerX - membranePotentialChart.getFullBoundsReference().width / 2,
            		screenSize.getHeight() - membranePotentialChart.getFullBoundsReference().height - 5);
            
            voltmeter.setOffset(rightEdgeX - voltmeter.getFullBoundsReference().width - 5,
            		worldSize.getHeight() - voltmeter.getFullBounds().height - 5);
        }
    }
    
    public void setVoltmeterVisible(boolean isVisible){
    	voltmeter.setVisible(isVisible);
    }
    
    public void setZoomFactor(double zoomFactor){
    	myWorldNode.setTransform(new AffineTransform());
        myWorldNode.scaleAboutPoint(zoomFactor, INITIAL_INTERMEDIATE_COORD_WIDTH / 2, 0);
    }
    
    private void addAtom(Particle atomToBeAdded){
    	atomLayer.addChild(new ParticleNode(atomToBeAdded, mvt));
    }
    
    private void addChannelNode(MembraneChannel channelToBeAdded){
    	final MembraneChannelNode channelNode = new MembraneChannelNode(channelToBeAdded, mvt);
    	axonCrossSectionLayer.addChild(channelNode);
    	channelToBeAdded.addListener(new MembraneChannel.Adapter() {
			public void removed() {
				axonCrossSectionLayer.removeChild(channelNode);
			}
		});
    }
    
    private static class CrossHairNode extends PNode {

    	private static final double LINE_LENGTH = 10;
    	private static final Stroke CROSS_HAIR_STROKE = new BasicStroke(3);
    	private static final Color CROSS_HAIR_COLOR = Color.RED;
    	
		public CrossHairNode() {
			PhetPPath verticalLine = new PhetPPath(new Line2D.Double(0, -LINE_LENGTH, 0, LINE_LENGTH),
					CROSS_HAIR_STROKE, CROSS_HAIR_COLOR);
			addChild(verticalLine);
			PhetPPath horizontalLine = new PhetPPath(new Line2D.Double(-LINE_LENGTH, 0, LINE_LENGTH, 0),
					CROSS_HAIR_STROKE, CROSS_HAIR_COLOR);
			addChild(horizontalLine);
		}
    }
}
