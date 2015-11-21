#Mobile MPEC-DASH Player#

This is the team project of CS5248 which I compeleted with two of my friends.

##Introduction##
In this project, we explored on creating a DASH-compliant client-server-based system for video live streaming and hosting from the initial video capture to the final play back of video both live and video on demand (VoD). Two Android applications and a web server hosting are designed and implemented.

##Dirctory structure:##

client_part/  
	|  
        ---Playback/                       <code># playback App Android source code</code>   
        ---Upload/                        <code># upload App Adnroid source code</code>  
  	
 server_part/  
	|  
        ---Convert.class.php               <code># Convert original mp4 to low qulity .mp4 and .ts</code>   
        ---DashFile.class.php              <code># A class for descripting DASH segment</code>  
        ---DashPlaylist.class.php          <code># Generate .mpd playlist and .m3u8 playlist</code>   
        ---db_createtable.sql              <code># A script for creating database tables</code>  
        ---DbMysql.class.php               <code># A class for database operating</code>    
        ---del.php                         <code># A php page for deleting mp4 files</code>  
        ---index.html                      <code># A testing upload page</code>  
        ---retrieve.php                    <code># A page for video playlist retrieving</code>  
        ---ServerController.class.php      <code># A main Controller to manage control flow</code>    
        ---upload.php                      <code># A page for video segment submitting</code>  
        ---video_repo/                     <code># A directory for video saving</code>  

