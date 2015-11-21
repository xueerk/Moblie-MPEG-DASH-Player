<?php

class DashFile
{
    private static $__uploadRootDir = 'video_repo/';
    private static $__allowedExts = array("mp4", "jpg", "jpeg");

    private $__basename;
    private $__name;
    private $__tmpname;
    private $__newname;
    private $__extension;
    private $__savedir;
    private $__savepath;
    private $__fileid;
    private $__sn;

    function __construct($id, $sn)
    {
     $this->__fileid = $id;
      $this->__sn = $sn;
      $this->__basename = $_FILES['userfile']['name'];  
      $this->__tmpname = $_FILES['userfile']['tmp_name'];
      $this->__extension = pathinfo($this->__basename, PATHINFO_EXTENSION);
      $this->__name = pathinfo($this->__basename, PATHINFO_FILENAME);
      $this->__newname = "$sn.{$this->__extension}";
      $this->__savedir = self::$__uploadRootDir . "$id/";
      $this->__savepath = "{$this->__savedir}{$this->__newname}";
   }

    public function isValid() 
    {   
        if (empty($_FILES)) 
            return FALSE;

        if (($_FILES["userfile"]["error"] == 0)  
                && ($_FILES["userfile"]["size"] < 800000000) 
                && (in_array($this->__extension, self::$__allowedExts))) 
            return TRUE;
        return FALSE;
    }   

    public function getBasename() 
    {   
        return $this->__basename;
    }   

    public function getExtension() 
    {   
        return $this->__extension;
    }   

    public function getName()
    {   
        return $this->__name;
    }   

    public function getId()
    {
        return $this->__fileid;
    }

    public function getNewname()
    {
        return $this->__newname;
    }

    public function getSavedir()
    {
        return $this->__savedir;
    }

    public function getSavepath()
    {
        return $this->__savepath;
    }

    public function getSn()
    {
        return $this->__sn;
    }

    public function saveFile()
    {
        if (!file_exists(self::$__uploadRootDir)) {
            if (!mkdir(self::$__uploadRootDir)) {
                return FALSE;
            }
        }

        var_dump($this->__savedir);
        var_dump($this->__savepath);

        if (!file_exists($this->__savedir)) {
            if (!mkdir($this->__savedir)) {
                return FALSE;
            }
        }

        if (move_uploaded_file($this->__tmpname, $this->__savepath)) {
            return TRUE;
        }
        return FALSE;
    }
}

?>
