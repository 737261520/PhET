<?php

    include_once("../admin/global.php");

    include_once(SITE_ROOT."teacher_ideas/user-login.php");  
    
    include_once(SITE_ROOT."admin/contrib-utils.php");    
    include_once(SITE_ROOT."admin/site-utils.php");   
    include_once(SITE_ROOT."admin/web-utils.php");
    include_once(SITE_ROOT."teacher_ideas/referrer.php");  
    
    function handle_action($action) {
        eval(get_code_to_create_variables_from_array($_REQUEST));
        
        
    }

    function print_content() {
        global $referrer, $contribution_id;

        print_r($GLOBALS['contribution']);
        
        ?>
        
        <h1>Edit Contribution</h1>
        
        <?php
        
        contribution_print_full_edit_form($contribution_id, "edit-contribution.php", $referrer);        
        
        print "<p><a href=\"$referrer\">cancel</a></p>";
    }
    
    if (isset($_REQUEST['sim_id'])) {
        $sim_id = $_REQUEST['sim_id'];
    }
    
    $contribution_id = $_REQUEST['contribution_id'];
    
    if (isset($_REQUEST['action'])) {
        handle_action($_REQUEST['action']);
    }
    
    $contribution = gather_script_params_into_array('contribution_');    
    
    print_site_page('print_content', 3);

?>