<?php
require_once("ServerController.class.php");
    error_reporting(-1);
    ini_set('display_errors', 'On');
    //print "<pre>";
    $master = new ServerController();
    $ret = $master->doUpload();

    
    print $ret."\n";
    //print_r($_FILES);
    //print_r($_POST);
    //print "</pre>";

?>
