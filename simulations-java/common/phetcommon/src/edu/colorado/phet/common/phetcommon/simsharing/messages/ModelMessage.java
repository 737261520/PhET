// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.common.phetcommon.simsharing.messages;

import edu.colorado.phet.common.phetcommon.simsharing.IMessageSource;
import edu.colorado.phet.common.phetcommon.simsharing.IMessageType;
import edu.colorado.phet.common.phetcommon.simsharing.Parameter;
import edu.colorado.phet.common.phetcommon.simsharing.SimSharingMessage;

/**
 * @author Sam Reid
 */
public class ModelMessage extends SimSharingMessage<ModelObject, ModelAction> {
    public ModelMessage( IMessageSource source, IMessageType messageType, ModelObject object, ModelAction action, Parameter... parameters ) {
        super( source, messageType, object, action, parameters );
    }
}
