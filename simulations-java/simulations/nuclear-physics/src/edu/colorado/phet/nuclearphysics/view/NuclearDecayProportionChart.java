/* Copyright 2007, University of Colorado */

package edu.colorado.phet.nuclearphysics.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;

import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.PhetPCanvas;
import edu.colorado.phet.common.piccolophet.nodes.HTMLNode;
import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.colorado.phet.common.piccolophet.nodes.PieChartNode;
import edu.colorado.phet.common.piccolophet.nodes.ShadowPText;
import edu.colorado.phet.common.piccolophet.nodes.PieChartNode.PieValue;
import edu.colorado.phet.nuclearphysics.NuclearPhysicsConstants;
import edu.colorado.phet.nuclearphysics.NuclearPhysicsStrings;
import edu.colorado.phet.nuclearphysics.model.Carbon14Nucleus;
import edu.colorado.phet.nuclearphysics.model.Uranium238Nucleus;
import edu.colorado.phet.nuclearphysics.module.alphadecay.multinucleus.MultiNucleusDecayModel;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * This class represents a chart that shows the proportion of nuclei that have
 * decayed versus time.  This is essentially the exponential display curve.
 *
 * @author John Blanco
 */
public class NuclearDecayProportionChart extends PNode {

    //------------------------------------------------------------------------
    // Class Data
    //------------------------------------------------------------------------
	
    // Constants for controlling the appearance of the chart.
    private static final Color  BORDER_COLOR = Color.DARK_GRAY;
    private static final float  BORDER_STROKE_WIDTH = 6f;
    private static final Stroke BORDER_STROKE = new BasicStroke( BORDER_STROKE_WIDTH );

    // Constants that control the proportions of the main components of the chart.
    private static final double PIE_CHART_WIDTH_PROPORTION = 0.1;
    
    //------------------------------------------------------------------------
    // Instance Data
    //------------------------------------------------------------------------

    // Time span covered by this chart, in milliseconds.
    private double _timeSpan;
    
    // Half life of primary (i.e. decaying) element.
	private double _halfLife; // Half life of decaying element, in milliseconds.
	
	// Variables that control chart appearance.
	private String _preDecayElementLabel;
	private Color _preDecayLabelColor;
	private String _postDecayElementLabel;
	private Color _postDecayLabelColor;
	private boolean _pieChartEnabled;
	private boolean _showPostDecayCurve;
	private boolean _timeMarkerLabelEnabled;

	// References to the various components of the chart.
    private PPath _borderNode;
    private ProportionsPieChartNode _pieChart;
    private GraphNode _graph;

    // Decay events that are represented on the graph.
    ArrayList _decayEvents = new ArrayList();
    
    // Parent node that will be non-pickable and will contain all of the
    // non-interactive portions of the chart.
    private PComposite _nonPickableChartNode;
    
    // Parent node that will have interactive portions of the chart.
    private PNode _pickableChartNode;
    
    // Rect that is used to keep track of the overall usable area for the chart.
    Rectangle2D _usableAreaRect = new Rectangle2D.Double();

    //------------------------------------------------------------------------
    // Constructor
    //------------------------------------------------------------------------

    public NuclearDecayProportionChart(boolean pieChartEnabled){
    	
    	_pieChartEnabled = pieChartEnabled;

    	// Many of the following initializations are arbitrary, and the chart
    	// should be set up via method calls before attempting to display
    	// anything.
    	_timeSpan = 1000;
    	_halfLife = 300;
    	_preDecayLabelColor = Color.PINK;
    	
    	// The following params don't necessarily need to be set for the chart
    	// to behave somewhat reasonably.
    	_showPostDecayCurve = false;
    	_postDecayLabelColor = Color.ORANGE;
    	_timeMarkerLabelEnabled = false;
    	
        // Set up the parent node that will contain the non-interactive
        // portions of the chart.
        _nonPickableChartNode = new PComposite();
        _nonPickableChartNode.setPickable( false );
        _nonPickableChartNode.setChildrenPickable( false );
        addChild( _nonPickableChartNode );
        
        // Set up the parent node that will contain the interactive portions
        // of the chart.
        _pickableChartNode = new PNode();
        _pickableChartNode.setPickable( true );
        _pickableChartNode.setChildrenPickable( true );
        addChild( _pickableChartNode );

        // Create the border for this chart.
        _borderNode = new PPath();
        _borderNode.setStroke( BORDER_STROKE );
        _borderNode.setStrokePaint( BORDER_COLOR );
        _borderNode.setPaint( NuclearPhysicsConstants.ALPHA_DECAY_CHART_COLOR );
        _nonPickableChartNode.addChild( _borderNode );

        // Create the graph.
        _graph = new GraphNode( this );
        addChild( _graph );
        
        // Add the pie chart (if enabled).
        if ( _pieChartEnabled ){
        	_pieChart = new ProportionsPieChartNode(this);
        	_nonPickableChartNode.addChild( _pieChart );
        }
    }

	//------------------------------------------------------------------------
    // Methods
    //------------------------------------------------------------------------
    
    public void setTimeSpan( double timeSpan ){
    	_timeSpan = timeSpan;
    	update();
    }
    
    public void setHalfLife(double life) {
		_halfLife = life;
    	update();
	}
    
	public void setPreDecayElementLabel(String decayElementLabel) {
		_preDecayElementLabel = decayElementLabel;
    	update();
	}

	public void setPreDecayLabelColor(Color decayLabelColor) {
		_preDecayLabelColor = decayLabelColor;
    	update();
	}

	public void setPostDecayElementLabel(String decayElementLabel) {
		_postDecayElementLabel = decayElementLabel;
    	update();
	}

	public void setPostDecayLabelColor(Color decayLabelColor) {
		_postDecayLabelColor = decayLabelColor;
    	update();
	}

	public void setShowPostDecayCurve(boolean postDecayCurve) {
		_showPostDecayCurve = postDecayCurve;
    	update();
	}

	public void setTimeMarkerLabelEnabled(boolean markerLabelEnabled) {
		_timeMarkerLabelEnabled = markerLabelEnabled;
    	update();
	}
	
	/**
	 * Configure the parameters
	 * @param nucleusType
	 */
	public void configureForNucleusType(int nucleusType){
    	switch(nucleusType){
    	case NuclearPhysicsConstants.NUCLEUS_ID_CARBON_14:
            _timeSpan = Carbon14Nucleus.HALF_LIFE * 3.2;
            _halfLife = Carbon14Nucleus.HALF_LIFE;
            _preDecayElementLabel = NuclearPhysicsStrings.CARBON_14_CHEMICAL_SYMBOL;
            _preDecayLabelColor = NuclearPhysicsConstants.CARBON_COLOR;
            _postDecayElementLabel = NuclearPhysicsStrings.NITROGEN_14_CHEMICAL_SYMBOL;
            _postDecayLabelColor = NuclearPhysicsConstants.NITROGEN_COLOR;
            break;
            
    	case NuclearPhysicsConstants.NUCLEUS_ID_URANIUM_238:
            _timeSpan = Uranium238Nucleus.HALF_LIFE * 3.2;
            _halfLife = Uranium238Nucleus.HALF_LIFE;
            _preDecayElementLabel = NuclearPhysicsStrings.URANIUM_238_CHEMICAL_SYMBOL;
            _preDecayLabelColor = NuclearPhysicsConstants.URANIUM_238_COLOR;
            _postDecayElementLabel = NuclearPhysicsStrings.LEAD_206_CHEMICAL_SYMBOL;
            _postDecayLabelColor = NuclearPhysicsConstants.LEAD_206_COLOR;
            break;
            
        default:
        	System.err.println(this.getClass().getName() + ": Error - Unable to configure chart for current nucleus type.");
            break;
    	}
    	
    	clear();
    	update();
	}

    /**
     * This method is called to re-scale the chart, which generally occurs
     * when the overall size of the simulation is changed.
     * 
     * @param 
     */
    private void updateBounds( Rectangle2D rect ) {
    	
    	if ( ( rect.getHeight() <= 0 ) || ( rect.getWidth() <= 0 ) ){
    		// This happens sometimes during initialization.  Don't know why,
    		// but we just ignore it if it does.
    		return;
    	}

        // Recalculate the usable area for the chart.
        _usableAreaRect.setRect( rect.getX() + BORDER_STROKE_WIDTH, rect.getY() + BORDER_STROKE_WIDTH,
        		rect.getWidth() - ( BORDER_STROKE_WIDTH * 2 ), rect.getHeight() - ( BORDER_STROKE_WIDTH * 2 ) );

        // Redraw the chart based on these recalculated values.
        update();
    }

    /**
     * Redraw the chart based on the current state.  This is basically the
     * place where the chart gets laid out.
     */
    private void update() {
    	
    	if (_usableAreaRect.getWidth() <= 0 || _usableAreaRect.getHeight() <= 0){
    		// Ignore if the size makes no sense.
    		return;
    	}
    	
        // Set up the border for the chart.
        _borderNode.setPathTo( new RoundRectangle2D.Double( _usableAreaRect.getX(), _usableAreaRect.getY(), 
        		_usableAreaRect.getWidth(), _usableAreaRect.getHeight(), 20, 20 ) );
        
        // Position the pie chart if enabled.
        double graphLeftEdge = 0;
        if ( _pieChartEnabled ){
        	_pieChart.scale( 1 );
        	_pieChart.scale( _usableAreaRect.getWidth() * PIE_CHART_WIDTH_PROPORTION 
        			/ _pieChart.getFullBoundsReference().getWidth() );
        	
        	// Position the pie chart so that it is a little ways in from the
        	// left of the chart and vertically centered.
        	_pieChart.setOffset( BORDER_STROKE_WIDTH * 2, _usableAreaRect.getCenterY() 
        			- (_pieChart.getFullBoundsReference().getHeight() / 2));
        	
        	graphLeftEdge = _pieChart.getFullBoundsReference().getMaxX();
        	
        }
        
        // Position the graph.
        _graph.update( (_usableAreaRect.getWidth() - graphLeftEdge) * 0.95, _usableAreaRect.getHeight() * 0.9 );
        _graph.setOffset( graphLeftEdge + 5, _usableAreaRect.getCenterY() - (_graph.getFullBoundsReference().height / 2 ) );
    }

    /**
     * This method causes the chart to resize itself based on the (presumably
     * different) size of the overall canvas on which it appears.
     * 
     * @param rect - Position on the canvas where this chart should appear.
     */
    public void componentResized( Rectangle2D rect ) {
        updateBounds( rect );
    }

    /**
     * Clear the chart.
     */
    public void clear() {
        _decayEvents.clear();
        _graph.clearData();
        if ( _pieChart != null ){
        	_pieChart.reset();
        }
    }
    
    /**
     * Add a data point to the chart by specifying the time and the number of
     * decayed and undecayed elements.  Note that these points are connected,
     * so the should be added in chronological order or the chart will end up
     * looking weird.
     * 
     * @param time
     * @param numUndecayed
     * @param numDecayed
     */
    public void addDataPoint(double time, int numUndecayed, int numDecayed){
		// Update the pie chart if it is present.
		if ( _pieChart != null ){
			_pieChart.setAmounts(numUndecayed, numDecayed);
		}
		
		// Update the graph.
    	Point2D decayEvent = new Point2D.Double( time, 100 * (double)numDecayed/(double)(numDecayed + numUndecayed));
		_decayEvents.add( decayEvent );
		_graph.graphDecayEvent( decayEvent );
    }
    
    /**
     * This class defines a node that consists of a pie chart and the various
     * labels needed for this chart.  It assumes only two sections exist, one
     * for the amount of pre-decay element and one for the amount of post-
     * decay.
     */
    private static class ProportionsPieChartNode extends PNode {

    	// Constants that control chart appearance and behavior.
    	private static final double INITIAL_ASPECT_RATIO = 0.75;
    	private static final double INITIAL_OVERALL_WIDTH = 100;
    	private static final double INITIAL_OVERALL_HEIGHT = INITIAL_OVERALL_WIDTH / INITIAL_ASPECT_RATIO;
    	private static final double PIE_CHART_WIDTH_PROPORTION = 0.9;
    	private static final int INITIAL_PIE_CHART_WIDTH = (int)(Math.round(INITIAL_OVERALL_WIDTH * PIE_CHART_WIDTH_PROPORTION));
    	private static final Font LABEL_FONT = new PhetFont(18);
    	
    	// The various elements of the chart.
    	private PieChartNode _pieChartNode;
    	private NuclearDecayProportionChart _chart; // The chart upon which this node resides.
    	private ShadowPText _preDecayLabel;
    	private ShadowPText _postDecayLabel;
    	private PText _numberPreDecayRemaining;
    	private PText _numberPostDecayRemaining;
    	
    	/**
    	 * Constructor.
    	 * 
    	 * @param chart
    	 */
		public ProportionsPieChartNode(NuclearDecayProportionChart chart) {
			
			_chart = chart;
			
			// Create and add the main chart.
			PieChartNode.PieValue[] pieChartValues = new PieValue[]{
	                new PieChartNode.PieValue( 100, _chart._preDecayLabelColor ),
	                new PieChartNode.PieValue( 0, _chart._postDecayLabelColor )};
	        _pieChartNode = new PieChartNode( pieChartValues, new Rectangle(INITIAL_PIE_CHART_WIDTH, INITIAL_PIE_CHART_WIDTH) );
	        _pieChartNode.setOffset(INITIAL_OVERALL_WIDTH / 2 - _pieChartNode.getFullBoundsReference().width / 2,
	        		INITIAL_OVERALL_HEIGHT / 2 - _pieChartNode.getFullBounds().height / 2);
	        addChild( _pieChartNode );
	        
	        // Create and add the labels.
	        PText dummySizingNode = new PText("9999");
	        dummySizingNode.setFont(LABEL_FONT);
	        
	        _preDecayLabel = new ShadowPText();
	        _preDecayLabel.setFont(LABEL_FONT);
	        _preDecayLabel.setOffset(0, 0);
	        addChild(_preDecayLabel);
	        _postDecayLabel = new ShadowPText();
	        _postDecayLabel.setFont(LABEL_FONT);
	        _postDecayLabel.setOffset(0, INITIAL_OVERALL_HEIGHT - dummySizingNode.getFullBoundsReference().height);
	        addChild(_postDecayLabel);
	        _numberPreDecayRemaining = new PText();
	        _numberPreDecayRemaining.setFont(LABEL_FONT);
	        _numberPreDecayRemaining.setOffset(
	        		INITIAL_OVERALL_WIDTH - dummySizingNode.getFullBoundsReference().width,
	        		0);
	        addChild(_numberPreDecayRemaining);
	        _numberPostDecayRemaining = new PText();
	        _numberPostDecayRemaining.setFont(LABEL_FONT);
	        _numberPostDecayRemaining.setOffset(
	        		INITIAL_OVERALL_WIDTH - dummySizingNode.getFullBoundsReference().width,
	        		INITIAL_OVERALL_HEIGHT - dummySizingNode.getFullBoundsReference().height);
	        addChild(_numberPostDecayRemaining);
	        
	        // TODO: temp for testing label placement.
	        _preDecayLabel.setText("#14C");
	        _postDecayLabel.setText("#14N");
	        _numberPreDecayRemaining.setText("1000");
	        _numberPostDecayRemaining.setText("0");
	        
	        // Reset the node.
	        reset();
		}
	
		/**
		 * The percentage of undecayed vs decayed nuclei.
		 * 
		 * @param percentageDecayed
		 */
		public void setDecayedPercentage( double percentageDecayed ){
			// Validate input.
			if ((percentageDecayed < 0) || (percentageDecayed > 100)){
				throw new IllegalArgumentException("Error: Percentage must be between 0 and 100.");
			}
			
			PieChartNode.PieValue[] pieChartValues = new PieValue[]{
	                new PieChartNode.PieValue( 100 - percentageDecayed, _chart._preDecayLabelColor ),
	                new PieChartNode.PieValue( percentageDecayed, _chart._postDecayLabelColor )};
			
			_pieChartNode.setPieValues(pieChartValues);
		}
		
		public void setAmounts(int numUndecayed, int numDecayed){
			
			// Set the text on the labels.
			_numberPreDecayRemaining.setText(Integer.toString(numUndecayed));
			_numberPostDecayRemaining.setText(Integer.toString(numDecayed));

			if ((numUndecayed == 0) && (numDecayed == 0)){
				// For the special case where both are zero, set undecayed to
				// be one so that the pie chart is all one color.
				numUndecayed = 1;
			}
			
			// Set the proportions of the pie chart.
			PieChartNode.PieValue[] pieChartValues = new PieValue[]{
	                new PieChartNode.PieValue( numUndecayed, _chart._preDecayLabelColor ),
	                new PieChartNode.PieValue( numDecayed, _chart._postDecayLabelColor )};
			
			_pieChartNode.setPieValues(pieChartValues);
		}
		
		/**
		 * Reset this node, which will set the proportions back to 100% non-
		 * decayed and will also reset the labels.
		 */
		public void reset(){
			
			setAmounts(0, 0);
			
			// Set the colors of the labels.
			
			_preDecayLabel.setTextPaint(_chart._preDecayLabelColor);
			_postDecayLabel.setTextPaint(_chart._postDecayLabelColor);
		}
    }
    
    /**
     * This class represents the graph portion of this chart, meaning the
     * portion with the axes, data curves, etc.
     */
    private static class GraphNode extends PNode {

    	// Constants that control the appearance of the graph.
        private static final float  THICK_AXIS_LINE_WIDTH = 2.5f;
        private static final Stroke THICK_AXIS_STROKE = new BasicStroke( THICK_AXIS_LINE_WIDTH );
        private static final float  THIN_AXIS_LINE_WIDTH = 0.75f;
        private static final Stroke THIN_AXIS_STROKE = new BasicStroke( THIN_AXIS_LINE_WIDTH );
        private static final  Color  AXES_LINE_COLOR = Color.BLACK;
        private static final double TICK_MARK_LENGTH = 3;
        private static final float  TICK_MARK_WIDTH = 2;
        private static final Stroke TICK_MARK_STROKE = new BasicStroke( TICK_MARK_WIDTH );
        private static final Color  TICK_MARK_COLOR = AXES_LINE_COLOR;
        private static final Font   PLAIN_LABEL_FONT = new PhetFont( Font.PLAIN, 14 );
        private static final Font   BOLD_LABEL_FONT = new PhetFont( Font.BOLD, 18 );
        private static final float  HALF_LIFE_LINE_STROKE_WIDTH = 2.0f;
        private static final Stroke HALF_LIFE_LINE_STROKE = new BasicStroke( HALF_LIFE_LINE_STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3.0f, 3.0f }, 0 );
        private static final Color  HALF_LIFE_LINE_COLOR = new Color (238, 0, 0);
        private static final float  DATA_CURVE_LINE_WIDTH_PROPORTION = 0.02f;

        // Constants that control other proportionate aspects of the graph.
        private static final double GRAPH_TEXT_HEIGHT_PROPORTION = 0.07;
        
        // For enabling/disabling the sizing rectangle.
        private static final boolean SIZING_RECT_VISIBLE = false;
        
        // The chart on which this graph will be appearing.
        NuclearDecayProportionChart _chart;

        // Various components that make up the graph.
    	private PPath _sizingRect;
        private ArrayList<PPath> _halfLifeLines = new ArrayList<PPath>();
        private ArrayList<PText> _halfLifeLineLabels = new ArrayList<PText>();
        private final PPath _lowerXAxisOfGraph;
        private final PText _lowerXAxisLabel;
        private final PPath _upperXAxisOfGraph;
        private final PPath _yAxisOfGraph;
        private ArrayList<PhetPPath> _xAxisTickMarks = new ArrayList<PhetPPath>();
        private ArrayList<PText> _xAxisTickMarkLabels = new ArrayList<PText>();
        private ArrayList _yAxisGridLines;
        private ArrayList<PText> _yAxisTickMarkLabels = new ArrayList<PText>();
        private final HTMLNode _yAxisLabel;
        private final PText _upperXAxisLabel;
        private final PNode _pickableGraphLayer;
        private final PNode _nonPickableGraphLayer;
        private final PNode _dataPresentationLayer;
        private PPath _preDecayProportionCurve;
        private PPath _postDecayProportionCurve;
        private Stroke _dataCurveStroke = new BasicStroke();

        // Factor for converting milliseconds to pixels.
        double _msToPixelsFactor = 1; // Arbitrary init val, updated later.

        // Rectangle that defines the size and position of the graph excluding
        // all the labels and such.
        private final Rectangle2D _graphRect = new Rectangle2D.Double();
        
        // Controls relative size of the labels.
		private double _labelScalingFactor;

		/**
		 * Constructor
		 * 
		 * @param parentChart
		 */
		public GraphNode( NuclearDecayProportionChart parentChart ) {

			_chart = parentChart;
			
			// Set up the layers (so to speak) for the graph.
			
			_nonPickableGraphLayer = new PNode();
			_nonPickableGraphLayer.setPickable(false);
			_nonPickableGraphLayer.setChildrenPickable(false);
			addChild( _nonPickableGraphLayer );
			
	        // Set up the parent node that will contain the data points.
			_dataPresentationLayer = new PNode();
			_dataPresentationLayer.setPickable( false );
			_dataPresentationLayer.setChildrenPickable( false );
	        addChild( _dataPresentationLayer );
			
			_pickableGraphLayer = new PNode();
			_pickableGraphLayer.setPickable(true);
			_pickableGraphLayer.setChildrenPickable(true);
			addChild( _pickableGraphLayer );
			
			// Create the labels at the left of the graph, since they will
			// affect the position of the origin.
			
	        _lowerXAxisOfGraph = new PPath();
	        _lowerXAxisOfGraph.setStroke( THICK_AXIS_STROKE );
	        _lowerXAxisOfGraph.setStrokePaint( AXES_LINE_COLOR );
	        _lowerXAxisOfGraph.setPaint( AXES_LINE_COLOR );
	        _nonPickableGraphLayer.addChild( _lowerXAxisOfGraph );

	        // Create the upper x axis of the graph.
	        _upperXAxisOfGraph = new PPath();
	        _upperXAxisOfGraph.setStroke( THIN_AXIS_STROKE );
	        _upperXAxisOfGraph.setStrokePaint( AXES_LINE_COLOR );
	        _upperXAxisOfGraph.setPaint( AXES_LINE_COLOR );
	        _nonPickableGraphLayer.addChild( _upperXAxisOfGraph );
	        
	        // Add the X axis label.
	        _lowerXAxisLabel = new PText();
	        _lowerXAxisLabel.setFont(BOLD_LABEL_FONT);
	        _nonPickableGraphLayer.addChild(_lowerXAxisLabel);

	        // Create the y axis of the graph.
	        _yAxisOfGraph = new PPath();
	        _yAxisOfGraph.setStroke( THIN_AXIS_STROKE );
	        _yAxisOfGraph.setStrokePaint( AXES_LINE_COLOR );
	        _yAxisOfGraph.setPaint( AXES_LINE_COLOR );
	        _nonPickableGraphLayer.addChild( _yAxisOfGraph );
	        
	        // Add the text for the Y axis tick marks.
	        PText tempText = new PText( NuclearPhysicsStrings.TWENTY_FIVE_PER_CENT );
	        tempText.setFont(BOLD_LABEL_FONT);
	        _nonPickableGraphLayer.addChild(tempText);
	        _yAxisTickMarkLabels.add(tempText);
	        tempText = new PText( NuclearPhysicsStrings.FIFTY_PER_CENT );
	        tempText.setFont(BOLD_LABEL_FONT);
	        _nonPickableGraphLayer.addChild(tempText);
	        _yAxisTickMarkLabels.add(tempText);
	        tempText = new PText( NuclearPhysicsStrings.SEVENTY_FIVE_PER_CENT );
	        tempText.setFont(BOLD_LABEL_FONT);
	        _nonPickableGraphLayer.addChild(tempText);
	        _yAxisTickMarkLabels.add(tempText);
	        tempText = new PText( NuclearPhysicsStrings.ONE_HUNDRED_PER_CENT );
	        tempText.setFont(BOLD_LABEL_FONT);
	        _nonPickableGraphLayer.addChild(tempText);
	        _yAxisTickMarkLabels.add(tempText);
	        
	        // Add the Y axis label.
	        _yAxisLabel = new HTMLNode(NuclearPhysicsStrings.DECAY_PROPORTIONS_Y_AXIS_LABEL);
	        _yAxisLabel.rotate(-Math.PI / 2);
	        _nonPickableGraphLayer.addChild(_yAxisLabel);
	        
	        // Add the label for the upper X axis.
	        _upperXAxisLabel = new PText( NuclearPhysicsStrings.HALF_LIVES_LABEL );
	        _upperXAxisLabel.setFont( BOLD_LABEL_FONT );
	        _nonPickableGraphLayer.addChild( _upperXAxisLabel );
	        
			_sizingRect = new PPath();
			_sizingRect.setStroke(THICK_AXIS_STROKE);
			_sizingRect.setStrokePaint(Color.red);
			addChild(_sizingRect);
			_sizingRect.setVisible(SIZING_RECT_VISIBLE);
		}
		
		/**
		 * Update the layout of the graph based on the supplied dimensions.
		 * 
		 * @param newWidth
		 * @param newHeight
		 */
		public void update( double newWidth, double newHeight ){
			
			double scale;
	        double graphLabelHeight = newHeight * GRAPH_TEXT_HEIGHT_PROPORTION;
	        PText dummyTextNode = new PText("Dummy");
	        dummyTextNode.setFont(BOLD_LABEL_FONT);
	        _labelScalingFactor = graphLabelHeight / dummyTextNode.getFullBoundsReference().height;
			_sizingRect.setPathTo( new Rectangle2D.Double(0, 0, newWidth, newHeight ) );

			// Set the needed vars that are based proportionately on the size
			// of the graph.
	        _dataCurveStroke = new BasicStroke( (float)(newHeight * DATA_CURVE_LINE_WIDTH_PROPORTION ),
	        		BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );

	        // Position and size the Y axis label, since it will affect the
	        // graph's origin.  There is a tweak factor in here that controls
	        // the relative size of the label.
	        _yAxisLabel.setScale(1);
	        scale = ((newHeight * 0.4) / _yAxisLabel.getFullBoundsReference().getHeight());
	        _yAxisLabel.setScale(scale);

	        _yAxisLabel.setOffset(0, newHeight / 2 - _yAxisLabel.getFullBoundsReference().width / 2);
	        
	        // Set the size of Y axis tick mark labels, since they will affect
	        // the location of the graph's origin.
	        double maxYAxisLabelWidth = 0;
	        for (PText yAxisTickMarkLabel : _yAxisTickMarkLabels){
	        	yAxisTickMarkLabel.setScale(1);
		        yAxisTickMarkLabel.setScale(_labelScalingFactor);
		        maxYAxisLabelWidth = Math.max(yAxisTickMarkLabel.getFullBoundsReference().width, maxYAxisLabelWidth);
	        }

	        // Create a rectangle that defines where the graph itself is,
	        // excluding all labels and such.  There are some "fudge factors"
	        // in this calculation to account for the fact that there is often
	        // some spacing between the labels and the graphs.

	        _graphRect.setRect(
	        		_yAxisLabel.getFullBoundsReference().width + maxYAxisLabelWidth,
	        		graphLabelHeight * 1.2, 
	        		newWidth - _yAxisLabel.getFullBoundsReference().getWidth() - maxYAxisLabelWidth,
	        		newHeight - 3.6 * graphLabelHeight);
	        
	        // Reposition the Y axis label now that we know the vertical size
	        // and position of the graph.
	        _yAxisLabel.setOffset(_yAxisLabel.getOffset().getX(), 
	        		_graphRect.getCenterY() + _yAxisLabel.getFullBoundsReference().height / 2);
	        
	        // Position the y-axis tick mark labels now that we know the
	        // vertical size of the graph.  Note that this assumes that there
	        // is no label for zero and that the labels are in increasing order.
	        int yAxisTickMarkCount = 1;
	        double yAxisTickMarkSpacing = _graphRect.getHeight() / (_yAxisTickMarkLabels.size());
	        for (PText yAxisTickMarkLabel : _yAxisTickMarkLabels){
	        	yAxisTickMarkLabel.setOffset( 
	        			_yAxisLabel.getFullBoundsReference().getWidth() + maxYAxisLabelWidth 
	        				- yAxisTickMarkLabel.getFullBoundsReference().width - 3,
	        			_graphRect.getMaxY() - yAxisTickMarkCount * yAxisTickMarkSpacing 
	        				- yAxisTickMarkLabel.getFullBoundsReference().height / 2);
	        	yAxisTickMarkCount++;
	        }

	        // Update the multiplier used for converting from pixels to
	        // milliseconds.  Use the multiplier to tweak the span of the x axis.
	        _msToPixelsFactor = _graphRect.getWidth() / _chart._timeSpan;	        

	        // Create the graph axes.
	        
			_lowerXAxisOfGraph.setPathTo( new Line2D.Double(_graphRect.getX(), _graphRect.getMaxY(), 
					_graphRect.getMaxX(), _graphRect.getMaxY() ) );
	        
	        _upperXAxisOfGraph.setPathTo( new Line2D.Double(_graphRect.getX(), _graphRect.getY(), 
	        		_graphRect.getMaxX(), _graphRect.getY() ) );
	        
	        _yAxisOfGraph.setPathTo( new Line2D.Double(_graphRect.getX(), _graphRect.getY(), 
	        		_graphRect.getX(), _graphRect.getMaxY() ) ); 

	        // Position and size labels for the upper X axis.
	        _upperXAxisLabel.setScale( 1 );
	        _upperXAxisLabel.setScale(_labelScalingFactor);
	        _upperXAxisLabel.setOffset(_graphRect.getX() + 5, _graphRect.getY() - 
	        		_upperXAxisLabel.getFullBoundsReference().height - 5 );
	        
	        // Add the X axis tick marks and labels.  Note that this won't
	        // handle all values of time span, so add more if needed.
	        updateXAxisTickMarksAndLabels();
	        
	        // Update the half life lines.
	        updateHalfLifeLines();
	        
	        // Reposition the data curve(s)
	        updateDecayCurves();
	        
	        // Position the tick marks and their labels on the X axis.
	        // TODO: Position tick marks and labels.

	        // Update the text for the Y axis labels.
	        // Set text for Y axis.
	        
	        // Position the labels for the axes.
	        // TODO: Lower X axis label
	        // TODO: Y axis label
	        // TODO: Upper axis label
		}
		
		public void clearData(){
	        _dataPresentationLayer.removeAllChildren();
	        _preDecayProportionCurve = null;
	        _postDecayProportionCurve = null;
		}
		
	    /**
	     * Add the vertical lines and the labels that depict the half life
	     * intervals to the chart.  This does some sanity testing to make sure
	     * that there isn't a ridiculous number of half life lines on the
	     * graph.
	     */
	    private void updateHalfLifeLines(){
	    	
	    	int numHalfLifeLines = (int)Math.floor( _chart._timeSpan / _chart._halfLife );
	    	
	    	if ( numHalfLifeLines != _halfLifeLines.size() ){
	    		// Either this is the first time through, or something has changed
	    		// that requires the half life lines to be reallocated.  First,
	    		// remove the existing lines.
	    		for ( Iterator it = _halfLifeLines.iterator(); it.hasNext(); ){
	    			PNode halfLifeLine = (PNode)it.next();
	    			_nonPickableGraphLayer.removeChild( halfLifeLine );
	    			it.remove();
	    		}
	    		
	    		// Remove the existing labels.
	    		for ( Iterator it = _halfLifeLineLabels.iterator(); it.hasNext(); ){
	    			PNode halfLifeLabel = (PNode)it.next();
	    			_nonPickableGraphLayer.removeChild( halfLifeLabel );
	    			it.remove();
	    		}
	    		
	    		// Allocate the correct number of new lines and labels.
	    		for ( int i = 0; i < numHalfLifeLines; i++ ){
	    			PPath halfLifeLine = new PPath();
	    			halfLifeLine.setStroke(HALF_LIFE_LINE_STROKE);
	    			halfLifeLine.setStrokePaint(HALF_LIFE_LINE_COLOR);
	    			_nonPickableGraphLayer.addChild( halfLifeLine );
	    			_halfLifeLines.add( halfLifeLine );
	    			PText halfLifeLabel = new PText(Integer.toString(i+1));
	    			halfLifeLabel.setFont(BOLD_LABEL_FONT);
	    			halfLifeLabel.setScale(_labelScalingFactor);
	    			_halfLifeLineLabels.add( halfLifeLabel );
	    			_nonPickableGraphLayer.addChild(halfLifeLabel);
	    		}
	    	}
	    	
	    	// Set the size and location for each of lines and labels.
			for ( int i = 0; i < _halfLifeLines.size(); i++ ){
				PPath halfLifeLine = (PPath)_halfLifeLines.get(i);
				halfLifeLine.setPathTo( new Line2D.Double(0, 0, 0, _graphRect.getHeight() ) );
				double xPos = (i + 1) * _chart._halfLife * _msToPixelsFactor + _graphRect.getX();
				halfLifeLine.setOffset( xPos, _graphRect.getY() );
				PText halfLifeLabel = (PText)_halfLifeLineLabels.get(i);
				halfLifeLabel.setOffset(xPos - halfLifeLabel.getFullBoundsReference().width/2, 
						_graphRect.getY() - halfLifeLabel.getFullBoundsReference().height);
			}
	    }
	    
	    /**
	     * Add the tick marks and labels to the X axis, which represents time.
	     * Note that this won't handle all time spans, so add more if needed.
	     */
	    private void updateXAxisTickMarksAndLabels(){
	    	
	    	// Remove the existing tick marks and labels.
	    	for (PNode tickMark : _xAxisTickMarks){
	    		_nonPickableGraphLayer.removeChild(tickMark);
	    	}
	    	for (PNode tickMarkLabel : _xAxisTickMarkLabels){
	    		_nonPickableGraphLayer.removeChild(tickMarkLabel);
	    	}
	    	_xAxisTickMarks.clear();
	    	_xAxisTickMarkLabels.clear();
	    	
	    	// Add the label for zero time.
			PText tickMarkLabel = new PText();
			tickMarkLabel.setText("0.0");  // Note: Not making this translatable since other labels aren't.
			tickMarkLabel.setFont(BOLD_LABEL_FONT);
			tickMarkLabel.setScale(_labelScalingFactor);
			tickMarkLabel.setOffset(_graphRect.getX() - tickMarkLabel.getFullBoundsReference().width / 2,
					_graphRect.getMaxY() + (tickMarkLabel.getFullBoundsReference().height * 0.1));
			_nonPickableGraphLayer.addChild(tickMarkLabel);
			_xAxisTickMarkLabels.add(tickMarkLabel);
	    	
	    	int numTickMarks = 0;
	    	if (_chart._timeSpan < MultiNucleusDecayModel.convertYearsToMs(1E9)){
	    		// Tick marks are 5000 yrs apart.  This is generally used for
	    		// the Carbon 14 range.
	    		numTickMarks = (int)(_chart._timeSpan / MultiNucleusDecayModel.convertYearsToMs(5000));
	    		
	    		for (int i = 0; i < numTickMarks; i++){
	    			addXAxisTickMark((i + 1) * MultiNucleusDecayModel.convertYearsToMs(5000),
	    					Integer.toString((i + 1) * 5000));
	    		}
	    	}
	    	else{
	    		// Space the tick marks four billion years apart.
	    		numTickMarks = (int)(_chart._timeSpan / MultiNucleusDecayModel.convertYearsToMs(4E9));
	    		
	    		for (int i = 0; i < numTickMarks; i++){
	    			addXAxisTickMark((i + 1) * MultiNucleusDecayModel.convertYearsToMs(4E9),
	    					String.format("%.1f", (float)((i + 1) * 4)));
	    		}
	    	}
	    	
	        // Position and size the label for the lower X axis.
	    	double unitsLabelYPos = _graphRect.getMaxY() + 5;
	    	if (_xAxisTickMarkLabels.size() > 0){
	    		unitsLabelYPos = _xAxisTickMarkLabels.get(0).getFullBoundsReference().getMaxY() + 5;
	    	}
	        _lowerXAxisLabel.setText(getXAxisUnitsText());
	        _lowerXAxisLabel.setFont(BOLD_LABEL_FONT);
	        _lowerXAxisLabel.setScale(1);
	        _lowerXAxisLabel.setScale(_labelScalingFactor);
	        _lowerXAxisLabel.setOffset(_graphRect.getCenterX() - _lowerXAxisLabel.getFullBoundsReference().height / 2,
	        		unitsLabelYPos);
	    }
	    
	    /**
	     * Convenience method for adding tick marks and their labels.
	     * 
	     * @param time
	     * @param label
	     */
	    private void addXAxisTickMark(double time, String label){
			PhetPPath tickMark = new PhetPPath(TICK_MARK_COLOR);
			tickMark.setPathTo(new Line2D.Double(0, 0, 0, -TICK_MARK_LENGTH));
			tickMark.setStroke(TICK_MARK_STROKE);
			tickMark.setOffset(_graphRect.getX() + (time * _msToPixelsFactor), _graphRect.getMaxY());
			_nonPickableGraphLayer.addChild(tickMark);
			_xAxisTickMarks.add(tickMark);
			PText tickMarkLabel = new PText();
			tickMarkLabel.setText(label);
			tickMarkLabel.setFont(BOLD_LABEL_FONT);
			tickMarkLabel.setScale(_labelScalingFactor);
			tickMarkLabel.setOffset(
					tickMark.getOffset().getX() - tickMarkLabel.getFullBoundsReference().width / 2,
					_graphRect.getMaxY() + (tickMarkLabel.getFullBoundsReference().height * 0.1));
			_nonPickableGraphLayer.addChild(tickMarkLabel);
			_xAxisTickMarkLabels.add(tickMarkLabel);
	    }
	    
	    private void updateDecayCurves(){
	    	
	    	// Get rid of the existing curves.
	    	_dataPresentationLayer.removeAllChildren();
	    	_preDecayProportionCurve = null;
	    	_postDecayProportionCurve = null;
	    	
	    	// Add new nodes in the correct positions.
	    	for ( Iterator it = _chart._decayEvents.iterator(); it.hasNext();){
	    		graphDecayEvent((Point2D)it.next());
	    	}
	    }
	    
	    /**
	     * Add the specified location to the graph of decay events.
	     *
	     * @param decayEventLocation - x represents time, y represents percentage
	     * of element remaining after this event.
	     */
	    private void graphDecayEvent( Point2D decayEventLocation ){
	    	
	    	float xPos = (float)(_msToPixelsFactor * decayEventLocation.getX() + _graphRect.getX());
	    	float yPosPreDecay = (float)( _graphRect.getMaxY() - ( ( ( 100 - decayEventLocation.getY() ) / 100 ) 
	    			* _graphRect.getHeight() ) );
	    	float yPosPostDecay = (float)( _graphRect.getMaxY() - ( ( decayEventLocation.getY() / 100 ) 
	    			* _graphRect.getHeight() ) );
	    	
	    	if ( _preDecayProportionCurve == null ){
	    		// Curve doesn't exist - create it.
	    		_preDecayProportionCurve = new PPath();
	    		_preDecayProportionCurve.moveTo( xPos, yPosPreDecay );
	    		_preDecayProportionCurve.setStroke( _dataCurveStroke );
	    		_preDecayProportionCurve.setStrokePaint( _chart._preDecayLabelColor );
	        	_dataPresentationLayer.addChild( _preDecayProportionCurve );
	    	}
	    	else{
	    		// Add the next segment to the curve.
	    		_preDecayProportionCurve.lineTo(xPos, yPosPreDecay);
	    	}
	    	
	    	if ( _postDecayProportionCurve == null ){
	    		// Curve doesn't exist - create it.
	    		_postDecayProportionCurve = new PPath();
	    		_postDecayProportionCurve.moveTo( xPos, yPosPostDecay );
	    		_postDecayProportionCurve.setStroke( _dataCurveStroke );
	    		_postDecayProportionCurve.setStrokePaint( _chart._postDecayLabelColor );
	        	_dataPresentationLayer.addChild( _postDecayProportionCurve );
	        	_postDecayProportionCurve.setVisible(_chart._showPostDecayCurve);
	    	}
	    	else{
	    		// Add the next segment to the curve.
	    		_postDecayProportionCurve.lineTo(xPos, yPosPostDecay);
	    	}
	    }
	    
	    /**
	     * Get the units string for the x axis label.  Note that this does not
	     * handle all ranges of time.  Feel free to add new ranges as needed.
	     */
	    private String getXAxisUnitsText(){
	    	
	    	String unitsText;
	    	if (_chart._timeSpan > MultiNucleusDecayModel.convertYearsToMs(100000)){
	    		unitsText = NuclearPhysicsStrings.DECAY_PROPORTIONS_TIME_UNITS_BILLION_YEARS;
	    	}
	    	else{
	    		unitsText = NuclearPhysicsStrings.DECAY_PROPORTIONS_TIME_UNITS_YEARS;
	    	}
	    	
	    	return unitsText;
	    }
    }

    /**
     * Main routine for stand alone testing.
     * 
     * @param args
     */
    public static void main(String [] args){
    	
        final NuclearDecayProportionChart proportionsChart = new NuclearDecayProportionChart(true); 

        proportionsChart.setTimeSpan(Carbon14Nucleus.HALF_LIFE * 3.2);
        proportionsChart.setHalfLife(Carbon14Nucleus.HALF_LIFE);
        proportionsChart.setPreDecayElementLabel(NuclearPhysicsStrings.CARBON_14_CHEMICAL_SYMBOL);
        proportionsChart.setPreDecayLabelColor(NuclearPhysicsConstants.CARBON_COLOR);
        proportionsChart.setShowPostDecayCurve(false);
        proportionsChart.setTimeMarkerLabelEnabled(false);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        PhetPCanvas canvas = new PhetPCanvas();
        frame.setContentPane( canvas );
        canvas.addScreenChild( proportionsChart );
        frame.setSize( 1000, 600 );
        proportionsChart.componentResized(frame.getBounds());
        frame.setVisible( true );
        
        for (int i = 0; i < 5; i++){
        	// Put some data points on the chart to test its behavior.
        	proportionsChart.addDataPoint((Carbon14Nucleus.HALF_LIFE / 10) * i, i, 5 - i);
        }
        
        canvas.addComponentListener( new ComponentListener(){

			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				proportionsChart.componentResized(e.getComponent().getBounds());
			}

			public void componentShown(ComponentEvent e) {
			}
        	
        });
    }
}
