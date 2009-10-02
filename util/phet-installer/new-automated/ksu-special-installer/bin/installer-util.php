<?php

    //========================================================================
    // This file contains contains the functions for creating and building
    // the installers.
    //========================================================================

    //--------------------------------------------------------------------------
    // Inclusions of other PHP files.
    //--------------------------------------------------------------------------

    require_once("config.php");
    require_once("global.php");
    require_once("file-util.php");
    require_once("web-util.php");
    require_once("jar-util.php");
    require_once("jnlp.php");
    require_once("autorun.php");
    require_once("xml-util.php");

    //--------------------------------------------------------------------------
    // Function for building local mirror installers (as opposed to an
    // installer that can be used to host a web site) for all of the supported
    // platforms.
    //--------------------------------------------------------------------------
    function installer_build_local_mirror_installers() {

        flushing_echo("Building all local mirror installers...");

        // Create output directory:
        file_mkdirs(OUTPUT_DIR);

        // Make the autorun file for Windows CD-ROM (this copies installer stuff):
        autorun_create_autorun_file(basename(BITROCK_DIST_SRC_WINNT));

        // Build Windows, Linux, Mac installers:
        installer_build_installers("all");

        // Build CD-ROM distribution:
        installer_build_cd_rom_distribution();

        // Clean up autorun files:
        autorun_cleanup_files();
    }

    //--------------------------------------------------------------------------
    // Function for creating the marker file that will be used by the tracking
    // code to determine whether a simulation was run from full installation
    // versus some other means (such as a single downloaded JAR file).  See
    // unfuddle ticket #875 for more information.
    //--------------------------------------------------------------------------
    function installer_create_marker_file(){
        $marker_file_name = RIPPED_WEBSITE_SIMS_PARENT_DIR.PHET_SIMS_SUBDIR.MARKER_FILE_NAME;
        flushing_echo("Marker file name = $marker_file_name");
        $contents = "# DO NOT DELETE THIS FILE.\n# This file identifies this installation of PhET.\n\n";
        file_put_contents_anywhere($marker_file_name, $contents);
    }

    //--------------------------------------------------------------------------
    // Insert the creation time of the installer into the marker file and into
    // the HTML files that launch the Flash simulations.  The marker file and
    // flash simulations must be modified at the same time so that the time
    // stamp will be the same for both.
    //--------------------------------------------------------------------------
    function installer_insert_installer_creation_time(){

        // Get the value of the time stamp.
        $time = time();
        date_default_timezone_set("America/Denver");
        $date = date('l F jS Y h:i:s A e');  // More human-readable representation.
        flushing_echo( "Creation Timestamp = $time" );
 
        // Add the time stamp to the marker file, used by the Java sims.
        $marker_file_name = RIPPED_WEBSITE_SIMS_PARENT_DIR.PHET_SIMS_SUBDIR.MARKER_FILE_NAME;
        if ( !( $fp = fopen( $marker_file_name, 'a' ) ) ) {
            flushing_echo('Error: Cannot open marker file, time stamp not added.');
        }
        else{
            fwrite( $fp, "installer.creation.date.epoch.seconds=".$time."\n" );
            flushing_echo( 'Successfully added timestamp marker to '.MARKER_FILE_NAME );
        }

        // Add the timestamp to each of the HTML files used to launch Flash 
        // sims.  If the timestamp is already present, update it.
        $html_file_names = glob( RIPPED_WEBSITE_SIMS_PARENT_DIR.PHET_SIMS_SUBDIR."*/*.html" );
        foreach ( $html_file_names as $html_file_name ) {
            $html_file_contents = file_get_contents( $html_file_name );
            if ( strstr( $html_file_contents, '@@INSTALLER_CREATION_TIMESTAMP@@' ) != false ){
                // The timestamp has never been inserted into this file, so insert it.
                $html_file_contents = preg_replace( '/@@INSTALLER_CREATION_TIMESTAMP@@/', $time, $html_file_contents );
            }
            else{
                // The timestamp must have already been replaced, meaning that
                // the sim to which this HTML file corresponds was not
                // re-downloaded for this build of the installers.  In this
                // case, insert a new timestamp value over the old one.  NOTE:
                // The approach used here assumes a 10-digit timestamp, and
                // will only work until Nov of the year 2286.  If, God forbid,
                // this is still in use at that time, a new solution will need
                // to be devised.
                $html_file_contents = preg_replace( "/installerCreationTimestamp=........../", 'installerCreationTimestamp='.$time, $html_file_contents );
            }
            // Write the updated file back to the original location.
            file_put_contents_anywhere( $html_file_name, $html_file_contents );
        }
        flushing_echo( "Processed ".sizeof ( $html_file_names )." HTML files for possible timestamp insertion." );

        // Write the timestamp into an HTML file that can be used to determine
        // the version of the sim installed.
        $version_info_file_name = RIPPED_TRANSLATED_WEBSITE_ROOT.VERSION_INFO_FILE_NAME;
        $version_info_html = "<html>\n<body>\n\n<p>Timestamp: ".$time."</p>\n<p>Date: ".$date."</p>\n\n</body>\n</html>";
        file_put_contents_anywhere( $version_info_file_name, $version_info_html );
        
        // Write the timestamp to a temporary file so that we can use it later
        // if needed (such as for putting the creation timestamp into the DB).
        if ( !( $fp = fopen(CREATION_TIMESTAMP_FILE_NAME, 'w' ) ) ) {
            flushing_echo( 'Error: Unable to open temporary timestamp file.' );
        }
        else{
            fwrite( $fp, $time );
            flushing_echo( 'Successfully created timestamp file '.CREATION_TIMESTAMP_FILE_NAME );
        }
    }

    //-------------------------------------------------------------------------
    // Function for inserting the distribution tag in all simulations, i.e.
    // both Java and Flash sims.  The distribution tag is reported with the
    // statistics that are sent by the sim to the statistics collection
    // server.
    //-------------------------------------------------------------------------
    function installer_insert_distribution_tag($distribution_tag){
        flushing_echo("--Entered installer_insert_distribution_tag, tag value = ".$distribution_tag);

        installer_insert_distribution_tag_flash($distribution_tag);
        installer_insert_distribution_tag_java($distribution_tag);

        flushing_echo("--Exited installer_insert_distribution_tag");
    }

    //-------------------------------------------------------------------------
    // Insert the distribution tag into the flash sims, which means that it
    // goes into all HTML files and into the JAR files that can be
    // individually downloaded.  Note that the JAR files must be re-signed
    // once they are changed.
    //-------------------------------------------------------------------------
    function installer_insert_distribution_tag_flash($distribution_tag){

        flushing_echo("---Entered installer_insert_distribution_tag_flash");

        // Replace the distribution tag in all of the HTML files.
        $html_file_names = glob( RIPPED_WEBSITE_SIMS_PARENT_DIR.PHET_SIMS_SUBDIR."*/*.html" );
        foreach ( $html_file_names as $html_file_name ) {
            $html_file_contents = file_get_contents( $html_file_name );
            if ( strstr( $html_file_contents, '@@DISTRIBUTION_TAG@@' ) != false ){
                // Insert the distribution tag.
                $html_file_contents = preg_replace( '/@@DISTRIBUTION_TAG@@/', $distribution_tag, $html_file_contents );
            }
            else{
                // This is unexpected, so issue a warning.
                flushing_echo("Warning: No distribution tag marker found in file ".$html_file_name);
            }
            // Write the updated file back to the original location.
            file_put_contents_anywhere( $html_file_name, $html_file_contents );
        }
        flushing_echo( "Processed ".sizeof ( $html_file_names )." HTML files for distribution tag insertion." );

        // Insert the distribution tag in each of the JAR files associated
        // with each flash sim project.
        $sim_dir_contents = glob( RIPPED_WEBSITE_SIMS_PARENT_DIR.PHET_SIMS_SUBDIR."*" );
        foreach ( $sim_dir_contents as $dir_or_file_name ) {
            if (is_dir($dir_or_file_name) && installer_is_flash_sim($dir_or_file_name)){
                // Extract the name of the sim project.
                $sim_project_name_pos = strripos($dir_or_file_name, "/") + 1;
                $sim_project_name = substr($dir_or_file_name, $sim_project_name_pos);
                // For each JAR file in the project...
                $flash_jar_file_names = glob( $dir_or_file_name."/*.jar" );
                foreach ( $flash_jar_file_names as $flash_jar_file_name ) {
                    flushing_echo( "Adding dist tag to Flash JAR file: ".$flash_jar_file_name );
                    // Extract the properties file.
                    $properties_file_name = $sim_project_name.'.properties';
                    extract_file_from_jar( $flash_jar_file_name, $properties_file_name, TEMP_DIR );
                    // Remove any existing tag.
                    file_remove_line_matching_pattern(TEMP_DIR.$properties_file_name, 'distribution.tag');
                    // Add the new distribution tag.
                    file_append_line_to_file(TEMP_DIR.$properties_file_name, 'distribution.tag='.DISTRIBUTION_TAG);
                    // Replace the properties file in the JAR with the updated version.
                    $original_dir = getcwd();
                    chdir(TEMP_DIR);
                    add_file_to_jar($flash_jar_file_name, $properties_file_name);
                    chdir($original_dir);
                    // Delete the modified properties file, just to be neat.
                    unlink(TEMP_DIR.$properties_file_name);
                    // Sign the JAR.
                    sign_jar( $flash_jar_file_name );
                }
            }
        }

        flushing_echo("---Exited installer_insert_distribution_tag_flash");
    }

    //-------------------------------------------------------------------------
    // Insert the distribution tag into the Java sims, which means that it
    // must be put into the JAR files.
    //-------------------------------------------------------------------------
    function installer_insert_distribution_tag_java($distribution_tag){
        flushing_echo("---Entered installer_insert_distribution_tag_java");
        flushing_echo("---Exited installer_insert_distribution_tag_java");
    }

    //-------------------------------------------------------------------------
    // Function for determining if the given simulation is a flash sim.  This
    // is done by looking for the expected file types.
    //-------------------------------------------------------------------------
    function installer_is_flash_sim( $sim_directory ) {

        // See if there are any SWF files present.
        $swf_file_list = file_list_in_directory( $sim_directory, "*.swf" );

        if ( count($swf_file_list ) > 0 ){
            // Assume that this is a Flash sim due to the presence of SWF files.
            return true;
        }
        else{
            // No SWF files, must not be a Flash sim.
            return false;
        }
    }

    //-------------------------------------------------------------------------
    // Function for determining if the given simulation is a java sim.  This
    // is done by looking for the expected file types.
    //-------------------------------------------------------------------------
    function installer_is_java_sim( $sim_directory ) {

        // See if there are any JNLP files present.
        $jnlp_file_list = file_list_in_directory( $sim_directory, "*.jnlp" );

        if ( count($jnlp_file_list ) > 0 ){
            // Assume that this is a Java sim due to the presence of JNLP files.
            return true;
        }
        else{
            // No JNLP files, must not be a Java sim.
            return false;
        }
    }

    //--------------------------------------------------------------------------
    // Deploy the installers to the production web site by copying them to the
    // appropriate directory.
    //--------------------------------------------------------------------------
    function deploy_installers() {

        // Verify that the installer files exist.
        flushing_echo("Attempting to deploy the installers.");
        if ( !file_exists( file_cleanup_local_filename( OUTPUT_DIR ) ) ) {
            flushing_echo("Error: Directory containing installers not found, aborting deployment.");
            return;
        }
        else {
            flushing_echo("Found the installer files.");
        }

        // Create a back of the existing files.
        // Note: This invokes an existing shell script.  At some point, it may
        // make sense to incorporate the functionality of this script into the
        // PHP code.
        exec( DEPLOY_DIR."create-backup.sh ".DEPLOY_DIR, $output, $return );
        flushing_echo( $output[1] );
        if ( $return == 0 ){
            flushing_echo( "Backup of previous installers succeeded." );
        }
        else{
            flushing_echo( "WARNING: Backup of previous installers failed." );
        }

        // Copy the files to the distribution directory.
        if ( copy( OUTPUT_DIR.LINUX_INSTALLER_FILE_NAME, DEPLOY_DIR.LINUX_INSTALLER_FILE_NAME ) ){
            flushing_echo( "Linux installer successfully copied to: ".DEPLOY_DIR );
        }
        else {
            flushing_echo( "Error: Unable to successfully copy linux installer to: ".DEPLOY_DIR );
        }
        if ( copy( OUTPUT_DIR.WINDOWS_INSTALLER_FILE_NAME, DEPLOY_DIR.WINDOWS_INSTALLER_FILE_NAME) ){
            flushing_echo( "Windows installer successfully copied to: ".DEPLOY_DIR );
        }
        else {
            flushing_echo( "Error: Unable to successfully copy windows installer to: ".DEPLOY_DIR );
        }
        if ( copy( OUTPUT_DIR.OSX_INSTALLER_FILE_NAME, DEPLOY_DIR.OSX_INSTALLER_FILE_NAME ) ){
            flushing_echo( "OSX installer successfully copied to: ".DEPLOY_DIR );
        }
        else {
            flushing_echo( "Error: Unable to successfully copy OSX installer to: ".DEPLOY_DIR );
        }
        if ( copy( OUTPUT_DIR.CD_ROM_INSTALLER_FILE_NAME, DEPLOY_DIR.CD_ROM_INSTALLER_FILE_NAME ) ){
            flushing_echo( "CD ROM installer successfully copied to: ".DEPLOY_DIR );
        }
        else {
            flushing_echo( "Error: Unable to successfully copy CD ROM installer to: ".DEPLOY_DIR );
        }
    }

    function installer_get_full_dist_name($dist) {
        return "PhET-dist-$dist";
    }

    function installer_get_zipped_mac_bundle_name($distfile) {
        return file_remove_extension($distfile).".zip";
    }

    function installer_build_installers($dist, $macro_map = array()) {
        global $g_bitrock_dists;

        $build_prefix  = installer_get_full_dist_name($dist);
        $buildfile_ext = file_get_extension(BITROCK_LOCAL_MIRROR_BUILDFILE);

        $new_buildfile = BITROCK_BUILDFILE_DIR."${build_prefix}.${buildfile_ext}";

        file_create_parents_of_file($new_buildfile);

        if (!copy(BITROCK_LOCAL_MIRROR_BUILDFILE, $new_buildfile)) {
            return false;
        }

        $macro_map['VERSION'] = BITROCK_PRODUCT_VERSION;

        if (!file_replace_macros_in_file($new_buildfile, $macro_map)) {
            return false;
        }

        $return_var = 0;

        $cwd = getcwd();

        $exe_dir = file_with_local_separator(BITROCK_EXE_DIR);

        // Change working directory to location of EXE:
        chdir($exe_dir);

        foreach ($g_bitrock_dists as $platform => $distfile) {
            $cmd_line = $exe_dir.BITROCK_EXE.BITROCK_PRE_ARGS.'"'."$new_buildfile".'" '.$platform;

            $cmd_line = file_with_local_separator($cmd_line);

            flushing_echo("Executing $cmd_line");

            system($cmd_line, $return_var);

            if ($return_var != 0) {
                flushing_echo("BitRock failed to build installer $distfile for configuration $dist on platform $platform.");
            }

            if ($platform == BITROCK_PLATFORM_OSX) {
                // OSX requires special treatment - the .app directory must be bundled into a zip file.
                $zipped_bundle_name = installer_get_zipped_mac_bundle_name($distfile);

                flushing_echo("Zipping Mac OS X distribution $distfile to $zipped_bundle_name");

                file_native_zip($distfile, $zipped_bundle_name);
                flushing_echo("Done");

                // Remove application bundle:
                file_remove_all($distfile);
            }
        }

        chdir($cwd);

        flushing_echo("Copying installers to ".OUTPUT_DIR);

        // Now move everything in the BitRock directory to the output directory:
        file_dircopy(BITROCK_DIST_DIR, OUTPUT_DIR, true);

        return true;
    }
    
    function installer_build_linux_web_mirror_installer($macro_map = array()) {

        global $g_bitrock_dists;

        $build_prefix  = "PhET-web-mirror-linux";
        $buildfile_ext = file_get_extension(BITROCK_WEB_MIRROR_BUILDFILE);

        $new_buildfile = BITROCK_BUILDFILE_DIR."${build_prefix}.${buildfile_ext}";

        file_create_parents_of_file($new_buildfile);

        if (!copy(BITROCK_WEB_MIRROR_BUILDFILE, $new_buildfile)) {
            return false;
        }

        $macro_map['VERSION'] = BITROCK_PRODUCT_VERSION;

        if (!file_replace_macros_in_file($new_buildfile, $macro_map)) {
            return false;
        }

        $return_var = 0;

        $cwd = getcwd();

        $exe_dir = file_with_local_separator(BITROCK_EXE_DIR);

        // Change working directory to location of EXE:
        chdir($exe_dir);

        $platform = BITROCK_PLATFORM_LINUX;

        $cmd_line = $exe_dir.BITROCK_EXE.BITROCK_PRE_ARGS.'"'."$new_buildfile".'" '.BITROCK_PLATFORM_LINUX;

        $cmd_line = file_with_local_separator($cmd_line);

        flushing_echo("Executing $cmd_line");

        system($cmd_line, $return_var);

        if ($return_var != 0) {
            flushing_echo("BitRock failed to build web mirror installer for ".BITROCK_PLATFORM_LINUX.".");
        }

        chdir($cwd);

        // Now move the resulting installer from the BitRock directory to the
        // output directory.
        flushing_echo("Copying web mirror installer to ".OUTPUT_DIR);
        rename(BITROCK_DIST_DIR.BITROCK_WEB_MIRROR_INSTALLER_NAME, OUTPUT_DIR.BITROCK_WEB_MIRROR_INSTALLER_NAME);

        return true;
    }

    function installer_build_cd_rom_distribution() {
        flushing_echo("Creating CD-ROM distribution ".CDROM_FILE_DEST);

        // Now make CD-ROM bundle:
        file_native_zip(
            array(
                BITROCK_DIST_DEST_WINNT,
                installer_get_zipped_mac_bundle_name(BITROCK_DIST_DEST_Darwin),
                BITROCK_DIST_DEST_Linux,
                AUTORUN_ICON_DEST,
                AUTORUN_FILE_DEST
            ),
            CDROM_FILE_DEST
        );
        flushing_echo("Done");
    }
?>
