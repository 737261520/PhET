<?php

    include_once("../admin/global.php");
    include_once("../admin/sys-utils.php");
    include_once("../admin/contrib-utils.php");
    include_once("../admin/site-utils.php");
    
    include_once("../teacher_ideas/user-login.php");

    $sim_id             = $_REQUEST['sim_id'];
    $contribution_title = $_REQUEST['contribution_title'];
    
    if (isset($_FILES['contribution_file_url'])) {
        $file = $_FILES['contribution_file_url'];
    }
    else {
        $file = $_FILES['MF__F_0_0'];
    }
    
    $name     = $file['name'];
    $type     = $file['type'];
    $tmp_name = $file['tmp_name'];
    $size     = $file['size'];
    $error    = $file['error'] !== 0;
    
    if ($contribution_title == '') {
        $contribution_title = remove_file_extension(basename($name));
    }
    
    $contribution_id = contribution_add_new_contribution($contribution_title, $contributor_id, $tmp_name, $name);
    
    for ($i = 1; true; $i++) {
        $file_key = "MF__F_0_$i";
        
        if (!isset($_FILES[$file_key])) {            
            break;
        }
        else {
            $file = $_FILES[$file_key];

            $name     = $file['name'];
            $type     = $file['type'];
            $tmp_name = $file['tmp_name'];
            $size     = $file['size'];
            $error    = $file['error'] !== 0;
            
            if (!$error){                
                contribution_add_new_file_to_contribution($contribution_id, $tmp_name, $name);
            }
            else {
                // Some error occurred during file upload
            }
        }
    }
    
    if (is_numeric($sim_id)) {
        contribution_associate_contribution_with_simulation($contribution_id, $sim_id);
    }
    
    $sims_page    = "../simulations/sims.php?sim_id=$sim_id";    
    $edit_contrib = "$prefix/teacher_ideas/edit-contribution.php?contribution_id=$contribution_id&amp;referrer=$sims_page;sim_id=$sim_id";
    
    // Redirect to contribution editing page:
    force_redirect("$edit_contrib", 7);
    
    function print_content() {
        global $edit_contrib;
        
        print <<<EOT
        <h1>New Contribution</h1>
        
        <p>Your contribution has been successfully submitted to PhET.</p>
        
        <p>On the next page, you will have the chance to edit your contribution or specify additional information.</p>
        
        <p><a href="$edit_contrib">Proceed to the next page.</a></p>
EOT;
    }
    
    print_site_page('print_content', 2);
?>