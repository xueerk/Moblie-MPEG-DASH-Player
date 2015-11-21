<?php
$cmd = "rm -rf /home/cs5248-15/team08/public_html/video_repo/*";
$ret = shell_exec($cmd);
print "rlt:" . $ret;
?>
