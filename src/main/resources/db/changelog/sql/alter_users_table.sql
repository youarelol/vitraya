alter table users add column otp varchar(8) default null;
alter table users add column otp_identifier varchar(100) default null;
alter table users add column otp_created timestamp  default CURRENT_TIMESTAMP null;