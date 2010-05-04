package edu.colorado.phet.buildtools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.colorado.phet.buildtools.util.ProcessOutputReader;

public class SVNStatusChecker {
    public SVNStatusChecker() {
    }

    public boolean isUpToDate( PhetProject project ) {
        AuthenticationInfo auth = BuildLocalProperties.getInstance().getRespositoryAuthenticationInfo();
        ArrayList args = new ArrayList();
        args.add( "svn" );
        args.add( "status" );
        args.add( "-u" );//checks with server, without this flag check is only local
        args.add( "--non-interactive" ); // don't have it ask for input
        args.add( "--username" );
        args.add( auth.getUsername() );
        args.add( "--password" );
        args.add( auth.getPassword() );
        PhetProject[] projects = project.getAllDependencies();
        for ( int i = 0; i < projects.length; i++ ) {
            args.add( projects[i].getProjectDir().getAbsolutePath() );
        }
        try {
            String[] cmd = (String[]) args.toArray( new String[0] );

            // for security reasons, don't print the username / password out
            //System.out.println( "exec'ing: " + toString( cmd ) );
            Process p = Runtime.getRuntime().exec( cmd );
            ProcessOutputReader pop = new ProcessOutputReader( p.getInputStream() );
            pop.start();
            ProcessOutputReader poe = new ProcessOutputReader( p.getErrorStream() );
            poe.start();
            try {
                p.waitFor();
            }
            catch( InterruptedException e ) {
                e.printStackTrace();
            }
            String out = pop.getOutput();
            String err = poe.getOutput();
            StringTokenizer t = new StringTokenizer( out, "\n" );
            boolean ok = true;
            while ( t.hasMoreTokens() ) {
                String token = t.nextToken();
                // patched because SVN seems to be having some problems with this. Still looking for the workaround
                boolean acceptableToken = token.startsWith( "Status against revision:" ) || token.contains( "libgssapi_krb5.so.2" );
                int tokenLength = token.trim().length();
                if ( tokenLength > 0 && !acceptableToken ) {
                    System.out.println( "Failure on token: \"" + token.trim() + "\"" );
                    ok = false;
                    break;
                }
            }
            if ( ok && err.trim().length() == 0 ) {
                return true;
            }
            else {
                System.out.println( "out=" + out + ", err=" + err );
                System.out.println( "Out of sync with SVN" );
                return false;
            }

        }
        catch( IOException e ) {
            e.printStackTrace();
            return false;
        }

    }

    private String toString( String[] cmd ) {
        String s = "";
        for ( int i = 0; i < cmd.length; i++ ) {
            String s1 = cmd[i];
            s = s + " " + s1;
        }
        return s;
    }
}
