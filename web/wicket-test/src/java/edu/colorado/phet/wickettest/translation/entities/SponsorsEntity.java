package edu.colorado.phet.wickettest.translation.entities;

import edu.colorado.phet.wickettest.panels.PhetPanel;
import edu.colorado.phet.wickettest.panels.SponsorsPanel;
import edu.colorado.phet.wickettest.translation.PhetPanelFactory;
import edu.colorado.phet.wickettest.translation.TranslationEntity;
import edu.colorado.phet.wickettest.util.PageContext;
import edu.colorado.phet.wickettest.util.PhetRequestCycle;

public class SponsorsEntity extends TranslationEntity {
    public SponsorsEntity() {
        addString( "language.dir" );
        addString( "sponsors.principalSponsors" );
        addString( "sponsors.hewlett" );
        addString( "sponsors.nsf" );
        addString( "sponsors.ksu" );
        addPreview( new PhetPanelFactory() {
            public PhetPanel getNewPanel( String id, PageContext context, PhetRequestCycle requestCycle ) {
                return new SponsorsPanel( id, context );
            }
        }, "default" );
    }
}
