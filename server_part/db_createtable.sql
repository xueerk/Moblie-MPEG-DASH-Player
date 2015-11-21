use test;

CREATE TABLE videos_tbl (
    idx int unsigned not null auto_increment primary key,
    id varchar(20) not null default '',
    name varchar(20) not null default '',
    duration int not null default '0',
    curSn int not null default '0',   -- the newest sn currently uploaded
    nConverted int not null default '-1', -- numbers of converted segments
    status int not null default '0' -- 0. uploading, live 1. stored
 );

/*
create table segments_tbl (
    id int unsigned not null auto_increment primary key,
    subcode varchar(20) not null default '',
    idcode varchar(20) not null default '',
    duration int not null default 0,
    segment int not null default 0
 );
*/

