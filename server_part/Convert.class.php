<?php

class Convert
{
    private $__file;
    private $__filename;
    private $__savedir;
    private $__savepath;
    private $__nohang = '> /dev/null 2>&1 &';
    private $__ffmpeg = '/usr/local/bin/ffmpeg';

    function __construct($file)
    {
        $this->__file = $file;
        $this->__filename = $this->__file->getSn();
        $this->__savedir = $this->__file->getSavedir();
        $this->__savepath = $this->__file->getSavepath();
    }

    private function convertToLow()
    {
        $option = " -map 0 -codec:v libx264 -codec:a libfaac ";

        $source_240 = "{$this->__savedir}{$this->__filename}_240x160.mp4";
        $file240 = $this->__savedir.$this->__filename . "_240x160.ts";
        $newFilePath = $this->__savedir . $this->__filename . '_240x160.mp4';
        $tempPath = $this->__savedir . 'tmp_' . $this->__filename . '_240x160.mp4';

        $cmd = "$this->__ffmpeg -i $this->__savepath -vf scale=240:160 -strict -2 -y $tempPath";
        $ret = shell_exec($cmd);

        $cmd = "mv $tempPath $newFilePath";
        shell_exec($cmd);
 
        $cmd = "nohup $this->__ffmpeg -i $source_240 $option $file240 $this->__nohang";
        shell_exec($cmd);
        return TRUE;
    }

    private function convertToMid()
    {
        $option = " -map 0 -codec:v libx264 -codec:a libfaac ";

        $source_480 = "{$this->__savedir}{$this->__filename}_480x320.mp4";
        $file480 = $this->__savedir.$this->__filename . "_480x320.ts";
        $newFilePath = $this->__savedir . $this->__filename . '_480x320.mp4';
        $tempPath = $this->__savedir . 'tmp_'. $this->__filename . '_480x320.mp4';

        $cmd = "$this->__ffmpeg -i $this->__savepath -vf scale=480:320 -strict -2 -y $tempPath";
        shell_exec($cmd);
        $cmd = "mv $tempPath $newFilePath";
        shell_exec($cmd);
        $cmd = "nohup $this->__ffmpeg -i $source_480 $option $file480 $this->__nohang";
        shell_exec($cmd);
        return TRUE;
    }

    private function convertToHigh()
    {
        $option = " -map 0 -codec:v libx264 -codec:a libfaac ";
        $source_720 = "{$this->__savedir}{$this->__filename}_720x480.mp4";
        $file720 = $this->__savedir.$this->__filename . "_720x480.ts";
        $newFilePath = $this->__savedir . $this->__filename . '_720x480.mp4';
        $tempPath = $this->__savedir . 'tmp_' . $this->__filename . '_720x480.mp4';

        $cmd = "$this->__ffmpeg -i $this->__savepath -vf scale=720:480 -strict -2 -y $tempPath";
        shell_exec($cmd);

        $cmd = "mv $tempPath $newFilePath";
        shell_exec($cmd);
 
        $cmd = "nohup $this->__ffmpeg -i $source_720 $option $file720 $this->__nohang";
        shell_exec($cmd);
        return TRUE;
    }

    private function ConvertToTs() 
    {
        $option = " -map 0 -codec:v libx264 -codec:a libfaac ";

        $source_720 = "{$this->__savedir}{$this->__filename}_720x480.mp4";
        $source_480 = "{$this->__savedir}{$this->__filename}_480x320.mp4";
        $source_240 = "{$this->__savedir}{$this->__filename}_240x160.mp4";
        
        $file720 = $this->__savedir.$this->__filename . "_720x480.ts";
        $file480 = $this->__savedir.$this->__filename . "_480x320.ts";
        $file240 = $this->__savedir.$this->__filename . "_240x160.ts";

        //var_dump($this->__savedir . $this->__filename . '_720x480.mp4');
        //var_dump($file720);


        $cmd = "nohup $this->__ffmpeg -i $source_720 $option $file720 $this->__nohang";
        shell_exec($cmd);
        $cmd = "nohup $this->__ffmpeg -i $source_480 $option $file480 $this->__nohang";
        shell_exec($cmd);
        $cmd = "nohup $this->__ffmpeg -i $source_240 $option $file240 $this->__nohang";
        shell_exec($cmd);
        return TRUE;
    }
    public function doConvert()
    {
        $this->convertToLow();
        $this->convertToMid();
        $this->convertToHigh();
        //$this->ConvertToTs();
        return TRUE;
    }

}

?>
