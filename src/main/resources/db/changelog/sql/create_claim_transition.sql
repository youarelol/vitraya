create table claim_transition
(
    id            BIGINT auto_increment
        primary key,
    claim_data_id long         not null,
    status        varchar(100) not null,
    date_created  timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);