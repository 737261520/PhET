package edu.colorado.phet.acidbasesolutions.view.beaker;

import edu.colorado.phet.acidbasesolutions.model.AqueousSolution;
import edu.colorado.phet.acidbasesolutions.view.moleculecounts.MoleculeCountsNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.nodes.PComposite;


public class BeakerNode extends PComposite {
    
    private static double MAX_VOLUME = 1; // liters
    
    private final MoleculeCountsNode moleculeCountsNode;
    private final BeakerLabelNode beakerLabelNode;
    
    public BeakerNode( PDimension vesselSize, AqueousSolution solution ) {
        
        VesselNode vesselNode = new VesselNode( vesselSize, MAX_VOLUME );
        
        double probeHeight = vesselSize.getHeight() + 55;
        PHProbeNode probeNode = new PHProbeNode( probeHeight, solution );
        
        SolutionNode solutionNode = new SolutionNode( vesselSize );
        
        moleculeCountsNode = new MoleculeCountsNode( solution );
        
        PDimension labelSize = new PDimension( 0.9 * vesselSize.getWidth(), 0.1 * vesselSize.getHeight() );
        beakerLabelNode = new BeakerLabelNode( labelSize, solution );
        
        // rendering order
        addChild( solutionNode );
        addChild( probeNode );
        addChild( vesselNode );
        addChild( moleculeCountsNode );
        addChild( beakerLabelNode );
        
        // layout
        vesselNode.setOffset( 0, 0 );
        solutionNode.setOffset( vesselNode.getOffset() );
        // molecule counts inside the vessel
        double xOffset = vesselNode.getXOffset() + 40;
        double yOffset = vesselNode.getYOffset() + 20;
        moleculeCountsNode.setOffset( xOffset, yOffset );
        // label at bottom of vessel
        PBounds vfb = vesselNode.getFullBoundsReference();
        xOffset = vfb.getMinX() + ( vfb.getWidth() - beakerLabelNode.getFullBoundsReference().getWidth() ) / 2;
        yOffset = vfb.getMaxY() - beakerLabelNode.getFullBoundsReference().getHeight() - 20;
        beakerLabelNode.setOffset( xOffset, yOffset );
        // probe at right side of vessel, tip of probe at bottom of vessel
        xOffset = vfb.getMaxX() - probeNode.getFullBoundsReference().getWidth() - 100;
        yOffset = vfb.getMaxY() - probeNode.getFullBoundsReference().getHeight();
        probeNode.setOffset( xOffset, yOffset );
    }
    
    public void setDisassociatedRatioComponentsVisible( boolean visible ) {
        //XXX
    }
    
    public boolean isDisassociatedRatioComponentsVisible() {
        return false; //XXX
    }
    
    public void setHydroniumHydroxideRatioVisible( boolean visible ) {
        //XXX
    }
    
    public boolean istHydroniumHydroxideRatioVisible() {
        return false; //XXX
    }
    
    public void setMoleculeCountsVisible( boolean visible ) {
        moleculeCountsNode.setVisible( visible );
    }
    
    public boolean isMoleculeCountsVisible() {
        return moleculeCountsNode.getVisible();
    }
    
    public void setBeakerLabelVisible( boolean visible ) {
        beakerLabelNode.setVisible( visible );
    }
    
    public boolean isBeakerLabelVisible() {
        return beakerLabelNode.getVisible();
    }
}
