create table claim_adjudication_result
(
    id                 bigint auto_increment
    primary key,
    claim_data_id      bigint                             not null,
    claim_decision     varchar(50)                        null,
    insurer_decision   varchar(50)                        null,
    claim_stage        varchar(20)                        null,
    remarks            varchar(500)                       null,
    pre_auth_decision  varchar(50)                        null,
    final_insurer_decision        varchar(50)             null,
    date_created       datetime default CURRENT_TIMESTAMP null,
    date_updated       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);