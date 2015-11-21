<?php

class DashPlaylist 
{
    private $_name;
    private $_nSegs;
    private $_duration;
    private $__file;

    function __construct($file)
    {
        $this->__file = $file;
    }

    public function getPlaylist()
    {
        $ret = "I am play list";
        return $ret;
    }

    public function generateMPD($duration, $nStart, $nSegs)
    {
        
        $id = $this->__file->getId();
        $savePath = $this->__file->getSavedir() . "{$id}.mpd";
        $xml = new XMLWriter();
        
        if (file_exists($savePath)) {
           unlink($savePath); 
        }

        $xml->openURI($savePath);
        $xml->setIndent(TRUE);

        $xml->startDocument('1.0', 'UTF-8');
        $xml->startElement('MPD');
        $xml->writeAttribute('xmlns', 'urn:mpeg:dash:schema:mpd:2011');
        $xml->writeAttribute('minBufferTime', 'PT1.500000S');
        $xml->writeAttribute('type', 'static');
        $xml->writeAttribute('mediaPresentationDuration', 'PT0H9M56.50S');
        $xml->writeAttribute('profiles', 'urn:mpeg:dash:profile:isoff-main:2011');
        $xml->writeElement('BaseURL', 'http://pilatus.d1.comp.nus.edu.sg/');
        //$xml->writeElement('BaseURL', 'http://pluto.comp.nus.edu.sg/');
        $xml->StartElement('Period');
        $xml->writeAttribute('duration', "PT{$duration[0]}H{$duration[1]}M{$duration[2]}S");
        $xml->writeElement('BaseURL', "~team08/video_repo/$id/" );
        //$xml->writeElement('BaseURL', "dash/video_repo/$id/" );
        $xml->StartElement('AdaptationSet');
        $xml->writeAttribute('segmentAlignment', 'true');
        $xml->writeAttribute('maxWidth', '720');
        $xml->writeAttribute('maxHeight', '480');
        $xml->writeAttribute('maxFrameRate', '30');
        $xml->writeAttribute('par', '3:2');
        $xml->StartElement('ContentComponent');
        $xml->writeAttribute('id', '1');
        $xml->writeAttribute('contentType', 'video');
        $xml->EndElement();
        $xml->StartElement('ContentComponent');
        $xml->writeAttribute('id', '2');
        $xml->writeAttribute('contentType', 'audio');
        $xml->EndElement();
        $xml->StartElement('Representation');
        $xml->writeAttribute('id', 'HIGH');
        $xml->writeAttribute('mimeType', 'video/mp4');
        $xml->writeAttribute('codecs', 'avc1,mp4a');
        $xml->writeAttribute('width', '720');
        $xml->writeAttribute('height', '480');
        $xml->writeAttribute('frameRate', '30');
        $xml->writeAttribute('sar', '1:1');
        $xml->writeAttribute('audioSamplingRate', '48000');
        $xml->writeAttribute('bandwidth', '3000000');
        $xml->StartElement('SegmentList');
        $xml->writeAttribute('duration', '3');
        for ($i = $nStart; $i <= $nSegs; $i++) {
            $xml->StartElement('SegmentURL');
            $xml->writeAttribute('media', "{$i}_720x480.mp4");
            $xml->EndElement();
        }
        $xml->EndElement();
        $xml->EndElement();
        $xml->StartElement('Representation');
        $xml->writeAttribute('id', 'MEDIUM');
        $xml->writeAttribute('mimeType', 'video/mp4');
        $xml->writeAttribute('codecs', 'avc1,mp4a');
        $xml->writeAttribute('width', '480');
        $xml->writeAttribute('height', '320');
        $xml->writeAttribute('frameRate', '30');
        $xml->writeAttribute('sar', '1:1');
        $xml->writeAttribute('audioSamplingRate', '48000');
        $xml->writeAttribute('bandwidth', '768000');
        $xml->StartElement('SegmentList');
        $xml->writeAttribute('duration', '3');
        for ($i = $nStart; $i <= $nSegs; $i++) {
            $xml->StartElement('SegmentURL');
            $xml->writeAttribute('media', "{$i}_480x320.mp4");
            $xml->EndElement();
        }
        $xml->EndElement();
        $xml->EndElement();
        $xml->StartElement('Representation');
        $xml->writeAttribute('id', 'LOW');
        $xml->writeAttribute('mimeType', 'video/mp4');
        $xml->writeAttribute('codecs', 'avc1,mp4a');
        $xml->writeAttribute('width', '240');
        $xml->writeAttribute('height', '160');
        $xml->writeAttribute('frameRate', '30');
        $xml->writeAttribute('sar', '1:1');
        $xml->writeAttribute('audioSamplingRate', '48000');
        $xml->writeAttribute('bandwidth', '200000');
        $xml->StartElement('SegmentList');
        $xml->writeAttribute('duration', '3');
        for ($i = $nStart; $i <= $nSegs; $i++) {
            $xml->StartElement('SegmentURL');
            $xml->writeAttribute('media', "{$i}_240x160.mp4");
            $xml->EndElement();
        }
        $xml->EndElement();
        $xml->EndElement();
        $xml->EndElement();
        $xml->EndElement();
        $xml->EndElement();
        $xml->endDocument();
        $xml->flush();
    }

    private function writeM3U8Index($savedir, $id)
    {
        $savePath = $savedir . "index.m3u8";
        $file = new SplFileObject($savePath, "w");
        $file->fwrite('#EXTM3U' . "\n");
        $file->fwrite('#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=3000000,RESOLUTION=720x480,CODECS="avc1,mp4a"' . "\n");
        $file->fwrite('720x480.m3u8' . "\n");
        $file->fwrite('#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=768000,RESOLUTION=480x320,CODECS="avc1,mp4a"' . "\n");
        $file->fwrite('480x320.m3u8' . "\n");
        $file->fwrite('#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=200000,RESOLUTION=240x160,CODECS="avc1,mp4a"' . "\n");
        $file->fwrite('240x160.m3u8' . "\n");
    }    

    private function writeM3U8Info($savedir, $id, $rslt, $nSegs)
    {
        $savePath = $savedir . "{$rslt}.m3u8";
        $file = new SplFileObject($savePath, "w");
        $file->fwrite('#EXTM3U' . "\n");
        $file->fwrite('#EXT-X-TARGETDURATION:3' . "\n");
        $file->fwrite('#EXT-X-VERSION:3' . "\n");
        $file->fwrite('#EXT-X-MEDIA-SEQUENCE:0' . "\n");
        for ($i = 0; $i <= $nSegs; $i++) {
            $file->fwrite('#EXTINF:3.0,' . "\n");
            $file->fwrite("{$i}_{$rslt}" . ".ts\n");
        }
        $file->fwrite('#EXT-X-ENDLIST' . "\n");
    }

    public function generateM3U8($nSegs)
    {
        $id = $this->__file->getId();
        //var_dump($id);
        $savedir = $this->__file->getSaveDir();
        $this->writeM3U8Index($savedir, $id);
        $this->writeM3U8Info($savedir, $id, '720x480', $nSegs);
        $this->writeM3U8Info($savedir, $id, '480x320', $nSegs);
        $this->writeM3U8Info($savedir, $id, '240x160', $nSegs);
    }
}
?>
