<?php
require_once("DashPlaylist.class.php");
require_once("DbMysql.class.php");
require_once("DashFile.class.php");
require_once("Convert.class.php");

class ServerController
{
    public function doUpload()
    {
        $id = $_POST['id'];
        $duration = $_POST['duration'];
        $sn = $_POST['sn'];
        $isLast = $_POST['last'];
        $mysql = new DbMysql();
        $dashfile = new DashFile($id, $sn);
        $convert = new Convert($dashfile);

       if ($isLast == 1) {
            if(!$mysql->connect())
                return "ERROR: Sql server connect error!\n";
            if (!$mysql->updateStatus($id, '1')) {
                return "ERROR: Sql INSERT error\n";
            }
            $segCount = $mysql->geSgmsCount($id); 
            $dpl = new DashPlaylist($dashfile);
            $dpl->generateMPD($duration, 0, $segCount);
            $dpl->generateM3U8($segCount);
            $mysql->close();
        } else {
            if (!$dashfile->isValid()) {
                return "ERROR: File is invalid!\n";
            }

            if ($dashfile->saveFile()) {
                if(!$mysql->connect())
                    return "ERROR: Sql server connect error!\n";
 
                if ($sn == 0) {
                    $mysql->insertNewVideo($id, $sn, $id, $duration);
                } else {
                    $mysql->updateNumSegs($id, $sn); 
                }
                $mysql->close();
            } else {
                return "ERROR: File save error!\n";
            }

            if ($convert->doConvert()) {
                if(!$mysql->connect())
                    return "ERROR: Sql server connect error!\n";
                if (!$mysql->updateNumConverted($id, $sn)) {
                    return "ERROR: Sql UPDATE error\n";
                }
                $mysql->close();
            }

            $start = $sn - 1;
            //if ($sn >= 3) {
            //    $start = $sn - 2; 
            //}

            $dpl = new DashPlaylist($dashfile);
            $dpl->generateMPD($duration, $start, $sn);
       }

        return "SUCCESS";
    }

    public function doRetrieve()
    {
        $mysql = new DbMysql();
        if(!$mysql->connect())
            return "ERROR: Sql server connect error!\n";

        $ret = $mysql->getList();
        return  $ret; 
    }
}
?>
