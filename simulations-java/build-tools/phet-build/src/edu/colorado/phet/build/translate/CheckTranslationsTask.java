package edu.colorado.phet.build.translate;

import edu.colorado.phet.build.AbstractPhetTask;
import org.apache.tools.ant.BuildException;

import java.io.IOException;

public class CheckTranslationsTask extends AbstractPhetTask {
    private boolean verbose;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void execute() throws BuildException {
        super.execute();
        try {
            new CheckTranslations(verbose).checkTranslations(getBaseDir());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
