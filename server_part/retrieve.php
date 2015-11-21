<?php
require_once("ServerController.class.php");

$master = new ServerController();
$ret = $master->doRetrieve();

print $ret;

#print_r($_FILES);
//print_r($_SERVER);
//print_r($_SESSION);
#print_r($_POST);
//print_r($_COOKIE);

//print "</pre>";

?>
