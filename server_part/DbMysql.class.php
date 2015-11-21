<?php
class DbMysql
{
    private $__servername = 'localhost';
    private $__dbname = 'test';
    private $__user = 'team08_15_db';
    private $__passwd = '' ;
    private $__mysql;

    //private $__servername = 'localhost';
    //private $__dbname = 'dash_db';
    //private $__user = 'root';
    //private $__passwd = 'pluto888' ;
    //private $__mysql;


    function __construct()
    {

    }

    public function connect()
    {
        $this->__mysql = new mysqli($this->__servername, 
                $this->__user, 
                $this->__passwd,
                $this->__dbname);   

        if ($this->__mysql->connect_error)
            return FALSE;
        return TRUE;
    }
    public function close()
    {
        $this->__mysql->close();
    }

    public function insertNewVideo($id, $sn, $name, $duration)
    {
        $sqlstr = "INSERT INTO videos_tbl (id, name, duration, curSn, nConverted, status) 
            VALUES ('{$id}', '{$name}', '{$duration}', '{$sn}', '-1', '0')";
        return $this->__mysql->query($sqlstr);
    }

    public function updateNumSegs($id, $sn)
    {
        $sqlstr = "UPDATE videos_tbl SET curSn = $sn WHERE id = '$id'";
        return $this->__mysql->query($sqlstr);
    }

    public function updateNumConverted($id, $sn)
    {
        $sqlstr = "UPDATE videos_tbl SET nConverted = $sn WHERE id = '$id'";
        return $this->__mysql->query($sqlstr);
    }

    public function updateStatus($id, $status)
    {
        $sqlstr = "UPDATE videos_tbl SET status='$status' WHERE id = '$id'";
        return $this->__mysql->query($sqlstr);
    }

    public function geSgmsCount($id)
    {
        $sqlstr = "SELECT * FROM videos_tbl WHERE id = $id";
        $result = $this->__mysql->query($sqlstr);
        
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            return $row["curSn"];
        }
        return -1;
    }

    public function getNumConverted($id)
    {
        $sqlstr = "SELECT * FROM videos_tbl WHERE id = $id";
        $result = $this->__mysql->query($sqlstr);
        
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc(); 
            return $row["nConverted"];
        }
        return -1;
    }

    public function isConverted($id, $sn) 
    {
        $sqlstr = "SELECT * FROM videos_tbl WHERE id = $id";
        $result = $this->__mysql->query($sqlstr);
        
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc(); 
            if ($sn <= $row["nConverted"])
                return TRUE;
        }
        return FALSE;
    
    }

    public function isVideoExist($id)
    {
        $sqlstr = "SELECT * FROM videos_tbl WHERE id = '{$id}'";
        $result = $this->__mysql->query($sqlstr);

        if ($result->num_rows > 0) {
            return TRUE;
        }
        return FALSE;
    }

    public function isSegmentExist($id, $sn)
    {
        $sqlstr = "SELECT * FROM videos_tbl WHERE (id = '$id' and sn = $sn)";
        $result = $this->__mysql->query($sqlstr);

        if ($result->num_rows > 0) {
            return TRUE;
        }
        return FALSE;
    }

    public function getList() 
    {
        $list = "####";
        $live = "";
        $sqlstr = "SELECT * FROM videos_tbl";
        $result = $this->__mysql->query($sqlstr);

        if ($result->num_rows > 0) {
            while($row = $result->fetch_assoc()) {
                if($row["status"] == 0)
                    $live = "Live";
                else
                    $live = "";
                $list = $list . $row["id"] . '@' . $live . '#';
            }
        }
        $list = $list . '@@@@';
        return $list;
    }
}
?>
