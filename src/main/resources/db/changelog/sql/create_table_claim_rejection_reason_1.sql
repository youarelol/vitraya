create table claim_rejection_reason
(
    id            bigint auto_increment
        primary key,
    claim_data_id bigint                             not null,
    claim_stage   varchar(100)                       not null,
    claim_status  varchar(20)                        not null,
    reason        varchar(200)                       not null,
    date_created  datetime default CURRENT_TIMESTAMP not null,
    date_updated  datetime default CURRENT_TIMESTAMP not null
);