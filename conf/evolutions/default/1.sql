# Tasks schema
 
# --- !Ups

CREATE SEQUENCE trainpoint_id_seq;
CREATE TABLE trainpoint (
    id integer NOT NULL DEFAULT nextval('trainpoint_id_seq'),
    locLat decimal NOT NULL,
    locLon decimal NOT NULL,
    trainguid varchar(10) NOT NULL,
    updated timestamp NOT NULL
);

 
# --- !Downs
 
DROP TABLE trainpoint;
DROP SEQUENCE trainpoint_id_seq;
