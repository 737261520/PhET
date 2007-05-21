<?php
    include_once("sys-utils.php");
    include_once("contrib-utils.php");
    
    function web_create_random_password() {
        $chars = "abcdefghijkmnopqrstuvwxyz023456789";
        
        srand((double)microtime()*1000000);
        
        $i    = 0;
        $pass = '';

        while ($i <= 7) {
            $num  = rand() % 33;
            $tmp  = substr($chars, $num, 1);
            $pass = $pass . $tmp;
            $i++;
        }

        return $pass;
    }

    function generate_check_status($item_num, $checked_item_num) {
        if ($checked_item_num == null && $item_num == "0" || $item_num == $checked_item_num) return "checked";
    
        return " ";
    }
    
    function force_redirect($url, $timeout = 0, $die = true) { 
        print "<META http-equiv=\"Refresh\" content=\"$timeout;url=$url\">";
    }
    
    // function url_exists($url) {
    //     if (is_array(get_headers($url))) {
    //         return true;
    //     }
    //     else {
    //         return false;
    //     }
    // }
    
    function url_exists($url) {
        return true;
    }
    
    function format_for_html($string) {
        return preg_replace('/&(?!amp;)/', '&amp;', $string);
    }
    
    function format_array_values_for_html($array) {
        $clean = array();
        
        foreach($array as $key => $value) {
            $clean["$key"] = format_for_html("$value");
        }
        
        return $clean;
    }
    
    function get_script_param($param_name, $default_value = "") {
        if (isset($_REQUEST['sim_id'])) {
            return $_REQUEST['sim_id'];
        }
        
        return $default_value;
    }
    
    function gather_script_params_into_array($prefix = '') {
        $array = array();
        
        foreach($_REQUEST as $key => $value) {
            if ($prefix == '' || strstr("$key", $prefix) == "$key") {
                $array["$key"] = "$value";
            }
        }
        
        return $array;
    }
    
    function gather_array_into_globals($array) {
        foreach($array as $key => $value) {
            $GLOBALS["$key"] = format_for_html("$value");
        }
    }
    
    function gather_script_params_into_globals() {     
        gather_array_into_globals($_REQUEST);
    }
    
    function get_code_to_create_variables_from_array($array) {
        $code = '';
        
        foreach($array as $key => $value) {
            $code .= "\$$key = '$value'; ";
        }
        
        return $code;
    }
    
    function get_code_to_create_variables_from_script_params() {
        return get_code_to_create_variables_from_array($_REQUEST);
    }
    
    function print_comma_list_as_bulleted_list($comma_list) {
        print "<ul>";
        
        foreach(explode(',', $comma_list) as $item) {
            $trimmed_item = trim($item);
            
            print "<li>$trimmed_item</li>";
        }
        
        print "</ul>";
    }

    function print_editable_area($control_name, $contents, $rows = "20", $cols = "80") {
        print("<textarea name=\"$control_name\" rows=\"$rows\" cols=\"$cols\">$contents</textarea>");
    }
    
    function print_captioned_editable_area($caption, $control_name, $contents, $rows = "20", $cols = "80") {
        print("<p align=\"left\" class=\"style16\">$caption<br/>");
            
        print_editable_area($control_name, $contents, $rows, $cols);
        
        print("</p>");
    }
    
    function print_text_input($control_name, $control_value, $width = 20) {
        print <<<EO_PRINT_TEXT_INPUT
            <input type="text" name="$control_name" value="$control_value" size="$width"/>
EO_PRINT_TEXT_INPUT;

    }
    
    function print_password_input($control_name, $control_value, $width = 20) {
        print <<<EO_PRINT_PASSWORD_INPUT
            <input type="password" name="$control_name" value="$control_value" size="$width"/>
EO_PRINT_PASSWORD_INPUT;

    }
    
    function print_hidden_input($control_name, $control_value) {
        print <<<EO_PRINT_HIDDEN_INPUT
            <input type="hidden" name="$control_name" value="$control_value"/>
EO_PRINT_HIDDEN_INPUT;
        
    }
    
    function get_upload_path_prefix_from_name($name) {
        $matches = array();
        
        preg_match('/^(.+?)((_url)?)$/', $name, $matches);
        
        $path_name = $matches[1];
        
        $path_prefix = preg_replace('/_/', '/', $path_name);
        
        print "Path prefix = $path_prefix\n";
        
        return $path_prefix;
    }
    
    function print_captioned_url_upload_control($caption, $control_name, $contents, $rows = "20", $cols = "80") {
        print("<p align=\"left\" class=\"style16\">$caption<br/>");
        
        print_editable_area($control_name, $contents, $rows, $cols);
        print("<p align=\"left\" class=\"style16\">Or upload a file: <input name=\"${control_name}_file_upload\" type=\"file\" /></p>");
        
        print("</p>");
    }
    
    function process_url_upload_control($control_name, $value) {
        $files_key = "${control_name}_file_upload";
        
        if (isset($_FILES[$files_key])) {
            print ("User uploading for $control_name");

            $upload_path_prefix = get_upload_path_prefix_from_name($control_name);
            
            $file_user_name = $_FILES[$files_key]['name'];
            $file_tmp_name  = $_FILES[$files_key]['tmp_name'];
            
            // If the user uploads a file, generate a URL relative to this directory:
            $target_name = basename($file_user_name);
            $target_dir  = dirname(__FILE__)."/uploads/$upload_path_prefix"; 
            $target_path = "${target_dir}/${target_name}"; 
             
            if ($target_name !== "" && $target_name !== null) {                
                mkdirs($target_dir);
                
                print("\nTarget name = $target_name; target path = $target_path\n");
             
                if (move_uploaded_file($file_tmp_name, $target_path)) {                
                    return "$upload_path_prefix/$target_name";
                }
            }
        }
        
        return $value;
    }
    
    function resolve_url_upload($url) {
        if (preg_match('/http.*/i', $url) == 1) {
            // URL is absolute:
            return $url;
        }
        else {
            // URL is relative to this directory:        
            
            // Can't allow user to access files outside /uploads/ directory:
            $url = preg_replace('/\.+/', '.', $url);
            
            $resolved_path = dirname(__FILE__)."/uploads/${url}";
            
            return $resolved_path;
        }
    }
    
    /**
     * This function displays a randomized slideshow.
     *
     */
    function display_slideshow($thumbnails, $width, $height, $prefix = "", $delay="5000") {
        /*

        Instead of using Flash to display random slideshow, our strategy is to use PHP 
        to generate a JavaScript script that randomly cycles through the images. This 
        way, the user does not need Flash in order to correctly view the home page.

        */ 

        print <<<EO_DISPLAY_SLIDESHOW_1
            <script language="javascript">

            var delay=$delay
            var curindex=0

            var randomimages=new Array()

EO_DISPLAY_SLIDESHOW_1;

        $index = 0;

        print "\n";

        foreach($thumbnails as $thumbnail) {
            print "randomimages[$index] = \"${prefix}admin/get-upload.php?url=$thumbnail\"\n";

            $index++;
        }

        print <<<EO_DISPLAY_SLIDESHOW_2
            var preload=new Array()

            for (n=0;n<randomimages.length;n++) {
            	preload[n]=new Image()
            	preload[n].src=randomimages[n]
            }

            document.write('<img name="defaultimage" width="$width" height="$height" src="'+randomimages[Math.floor(Math.random()*(randomimages.length))]+'">')

            function rotateimage() {
                curindex=Math.floor(Math.random()*(randomimages.length))

                document.images.defaultimage.src=randomimages[curindex]
            }

            setInterval("rotateimage()", delay)

            </script>
EO_DISPLAY_SLIDESHOW_2;
    }
    
    function print_login_form($optional_message = null, $standard_message = "<p>Please enter your email and password.</p>", $username = '') {
        $script = get_self_url();
        
        print <<<EOT
            <form id="loginform" method="post" action="$script">
                <fieldset>
                    <legend>Log in</legend>
EOT;

        if ($optional_message !== null) {
            print "$optional_message";
        }

        print <<<EOT
                    $standard_message
                
                    <label for="email">
                        <input type="text"     name="username" tabindex="1" id="username" size="25" value="$username" />your email:
                    </label>
                
                    <label for="password">
                        <input type="password" name="password" tabindex="2" id="password" size="25"/>your password:
                    </label>
            
                    <label for="submit">
                        <input name="Submit" type="submit" id="submit" tabindex="4" value="Log in" />
                    </label>
                 </fieldset>
            </form>
EOT;
    }
    
    function cookie_var_clear($name) {
        setcookie("$name", '', time() - 60*60*24*365*10);
    }
    
    function cookie_var_store($name, $var) {        
        cookie_var_clear($name);
        
        setcookie("$name", $var);
    }
    
    function cookie_var_is_stored($name) {
        return isset($_COOKIE["$name"]);
    }
    
    function cookie_var_get($name) {
        if (!isset($_COOKIE["$name"])) {            
            return "";
        } 
        
        return $_COOKIE["$name"];
    }
    
    function get_self_url() {
        $url = basename($_SERVER['SCRIPT_NAME']);
        
        if (isset($_SERVER['QUERY_STRING'])) {
            $query_string = str_replace(array('&amp;', '&'), array('&', '&amp;'), $_SERVER['QUERY_STRING']);
            
            $url .= "?$query_string";
        }
        
        return $url;
    }
    
    function is_email($email) {
        $p  = '/^[a-z0-9!#$%&*+-=?^_`{|}~]+(\.[a-z0-9!#$%&*+-=?^_`{|}~]+)*';
        $p .= '@([-a-z0-9]+\.)+([a-z]{2,3}';
        $p .= '|info|arpa|aero|coop|name|museum)$/ix';
        
        return preg_match($p, $email) == 1;
    }
    
    function php_array_to_javascript_array($array, $baseName) {
        $code = ($baseName . " = new Array(); \r\n ");    

        reset ($array);

        while (list($key, $value) = each($array)) {
            if (is_numeric($key)) {
                $outKey = "[" . $key . "]";
            }
            else {
                $outKey = "['" . $key . "']";
            }

            if (is_array($value)) {
                $code .= php_array_to_javascript_array($value, $baseName . $outKey);
            } 
            else { 
                $code .= ($baseName . $outKey . " = ");      

                if (is_string($value)) {
                    $code .= ("'" . $value . "'; \r\n ");
                }
                else if ($value === false) {
                    $code .= ("false; \r\n");
                }
                else if ($value === NULL) {
                    $code .= ("null; \r\n");
                }
                else if ($value === true) {
                    $code .= ("true; \r\n");
                } 
                else {
                    $code .= ($value . "; \r\n");
                }
            }
        }
       
       return $code;
    }

    function print_multiple_selection($options_array, $selections_array = array(), $name_prefix = "ms") {
        static $has_printed_javascript = false;
        static $ms_id_num = 1;
        
        $name = "${name_prefix}_${ms_id_num}";
        
        ++$ms_id_num;
        
        $text_to_identifier = array();
        
        $options = '';
        
        foreach($options_array as $identifier => $text) {            
            $options .= "<option value=\"$identifier\">$text</option>";
            
            $text_to_identifier["$text"] = "$identifier";
        }
        
        $selections = '';
        
        static $child_id_index = 1;
        
        $list_id = "${name}_list_uid";
        
        foreach($selections_array as $text) {
            $identifier = $text_to_identifier["$text"];
            
            $child_id = "child_${name}_${child_id_index}";
            
            $selections .= "<li id=\"$child_id\">";
            $selections .= "<a href=\"javascript:void(0)\" onclick=\"ms_remove_li('$list_id', '$child_id')\">$text</a>";
            $selections .= "<input type=\"hidden\" name=\"$identifier\" value=\"1\" />";
            $selections .= "</li>";
            
            ++$child_id_index;
        }
        
        $select_id   = "${name}_uid";
        $select_name = "${name}_select";
        
        if (!$has_printed_javascript) {
            $has_printed_javascript = true;

            print <<<EOT
            <script type="text/javascript">
                /* <![CDATA[ */

                var child_id_index = $child_id_index;

                function ms_remove_li(id, child_id) {
                    var Parent = document.getElementById(id);
                    var Child  = document.getElementById(child_id);

                    Parent.removeChild(Child);

                    return false;
                }

                function ms_add_li(basename, list_id, text, name) {
                    var Parent = document.getElementById(list_id);

                    var li_children = Parent.getElementsByTagName("li");

                    for(var i = 0, li_child; li_child = li_children[i]; i++) {
                        var input_child = li_child.getElementsByTagName("input")[0];

                        if (input_child.name == name) {
                            return false;
                        }
                    }

                    var NewLI = document.createElement("li");

                    NewLI.id        = "child_" + basename + "_" + child_id_index;                    
                    NewLI.innerHTML = "<a href=\"javascript:void(0)\" onclick=\"ms_remove_li('" + list_id + "','" + NewLI.id + "')\">" + text + "</a>" +
                                      "<input type=\"hidden\" name=\"" + name + "\" value=\"1\" />";

                    Parent.appendChild(NewLI);

                    child_id_index++;
                }

                function ms_on_change(basename, list_id, dropdown) {
                	var index  = dropdown.selectedIndex;

                	var text   = dropdown.options[index].text;
                	var value  = dropdown.options[index].value;

                    ms_add_li(basename, list_id, text, value);

                	return false;
                }

                /* ]]> */
            </script>
EOT;
        }        
        
        print <<<EOT
            <ul id="$list_id">
                $selections
            </ul>
                        
            <select name="$select_name" id="$select_id" 
                        onclick="ms_on_change('$name',  '$list_id', this.form.$select_name);" 
                        onchange="ms_on_change('$name', '$list_id', this.form.$select_name);">
                $options
            </select>
EOT;
    }
    
    function print_single_selection($select_name, $value_to_text, $selected_text) {
        print <<<EOT
            <select name="$select_name" id="${select_name}_uid">
EOT;

        foreach($value_to_text as $value => $text) {
            $is_selected = ($text == $selected_text) ? "selected=\"selected\"" : "";
            
            print <<<EOT
                <option value="$value" $is_selected>$text</option>
EOT;
        }

        print <<<EOT
            </select>
EOT;
    }
    
    function print_checkbox($checkbox_name, $checkbox_text, $checkbox_value) {
        $is_checked = $checkbox_value == "1" ? "checked=\"checked\"" : "";
        
        print <<<EOT
            <input type="hidden"   name="$checkbox_name" value="0" />
            <input type="checkbox" id="${checkbox_name}_uid" name="$checkbox_name" $is_checked>$checkbox_text</input>
EOT;
    }

?>