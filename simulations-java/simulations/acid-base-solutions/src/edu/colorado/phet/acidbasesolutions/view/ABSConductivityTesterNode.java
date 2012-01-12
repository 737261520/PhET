// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.acidbasesolutions.view;

import java.awt.Color;

import edu.colorado.phet.acidbasesolutions.constants.ABSSimSharing.ParameterKeys;
import edu.colorado.phet.acidbasesolutions.constants.ABSSimSharing.UserComponents;
import edu.colorado.phet.acidbasesolutions.model.ConductivityTester;
import edu.colorado.phet.acidbasesolutions.model.SolutionRepresentation.SolutionRepresentationChangeAdapter;
import edu.colorado.phet.common.phetcommon.simsharing.Parameter;
import edu.colorado.phet.common.phetcommon.simsharing.SimSharingManager;
import edu.colorado.phet.common.phetcommon.simsharing.messages.IUserAction;
import edu.colorado.phet.common.phetcommon.simsharing.messages.IUserComponent;
import edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform;
import edu.colorado.phet.common.piccolophet.event.simsharing.NonInteractiveUserComponent;
import edu.colorado.phet.common.piccolophet.nodes.conductivitytester.ConductivityTesterNode;

/**
 * Specialization of the conductivity node for Acid-Base Solutions.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class ABSConductivityTesterNode extends ConductivityTesterNode {

    public ABSConductivityTesterNode( final ConductivityTester tester, boolean dev ) {
        super( edu.colorado.phet.common.phetcommon.simsharing.messages.UserComponents.conductivityTester, tester, ModelViewTransform.createIdentity(), Color.BLACK, Color.RED, Color.BLACK, dev );

        //Wire up so the conductivity tester is only shown when selected
        tester.addSolutionRepresentationChangeListener( new SolutionRepresentationChangeAdapter() {
            public void visibilityChanged() {
                setVisible( tester.isVisible() );
            }
        } );

        //TODO how to do this with SimSharingDragHandler?
        // sim-sharing, positive probe
//        {
//
//            getPositiveProbeDragHandler().setStartEndFunction( new SimSharingDragHandlerOld.DragFunction() {
//                public void apply( IUserAction action, Parameter xParameter, Parameter yParameter, PInputEvent event ) {
//                    sendProbeEvent( UserComponents.conductivityTesterPositiveProbe, action, tester.isPositiveProbeInSolution(), tester.isCircuitCompleted() );
//                }
//            } );
//            getPositiveProbeDragHandler().setDragFunction( new SimSharingDragHandlerOld.DragFunction() {
//                boolean inSolution = tester.isPositiveProbeInSolution();
//
//                // Send event when probe transitions between in/out of solution.
//                public void apply( IUserAction action, Parameter xParameter, Parameter yParameter, PInputEvent event ) {
//                    if ( inSolution != tester.isPositiveProbeInSolution() ) {
//                        sendProbeEvent( UserComponents.conductivityTesterPositiveProbe, action, tester.isPositiveProbeInSolution(), tester.isCircuitCompleted() );
//                    }
//                    inSolution = tester.isPositiveProbeInSolution();
//                }
//            } );
//        }
//
//        // sim-sharing, negative probe
//        {
//            getNegativeProbeDragHandler().setStartEndFunction( new SimSharingDragHandlerOld.DragFunction() {
//                public void apply( IUserAction action, Parameter xParameter, Parameter yParameter, PInputEvent event ) {
//                    sendProbeEvent( UserComponents.conductivityTesterNegativeProbe, action, tester.isNegativeProbeInSolution(), tester.isCircuitCompleted() );
//                }
//            } );
//            getNegativeProbeDragHandler().setDragFunction( new SimSharingDragHandlerOld.DragFunction() {
//                boolean inSolution = tester.isNegativeProbeInSolution();
//
//                // Send event when probe transitions between in/out of solution.
//                public void apply( IUserAction action, Parameter xParameter, Parameter yParameter, PInputEvent event ) {
//                    if ( inSolution != tester.isNegativeProbeInSolution() ) {
//                        sendProbeEvent( UserComponents.conductivityTesterNegativeProbe, action, tester.isNegativeProbeInSolution(), tester.isCircuitCompleted() );
//                    }
//                    inSolution = tester.isNegativeProbeInSolution();
//                }
//            } );
//        }

        // sim-sharing, light bulb and battery (not interactive)
        {
            getLightBulbNode().addInputEventListener( new NonInteractiveUserComponent( UserComponents.lightBulb ) );
            getBatteryNode().addInputEventListener( new NonInteractiveUserComponent( UserComponents.battery ) );
        }
    }

    private static void sendProbeEvent( IUserComponent object, IUserAction action, boolean inSolution, boolean circuitCompleted ) {
        SimSharingManager.sendUserMessage( object, action,
                                           Parameter.param( ParameterKeys.isInSolution, inSolution ).
                                                   param( ParameterKeys.isCircuitCompleted, circuitCompleted ) );
    }
}
