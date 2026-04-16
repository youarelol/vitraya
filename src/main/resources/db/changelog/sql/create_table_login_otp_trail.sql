create table login_otp_trail
(
    id            bigint auto_increment
        primary key,
    user_name     varchar(100)                         not null,
    mobile_number varchar(12)                          not null,
    otp           varchar(8)                           not null,
    creation_time datetime   default CURRENT_TIMESTAMP not null,
    expiry_time   datetime   default CURRENT_TIMESTAMP not null,
    message_id    varchar(100)                         null,
    otp_consumed  tinyint(1) default 0                 null
);