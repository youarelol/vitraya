drop table claim_adjudication_result;

create table claim_adjudication_result
(
    id                       bigint auto_increment
        primary key,
    claim_data_id            bigint                             not null,
    claim_decision           varchar(50)                        null,
    claim_stage              varchar(100)                       null,
    remarks                  varchar(500)                       null,
    pre_auth_bill_amount     double                             null,
    pre_auth_amount_approved double                             null,
    pre_auth_decision        varchar(50)                        null,
    pre_auth_insurer_decision varchar(50)                        null,
    pre_auth_insurer_amount_approved double                             null,
    pre_auth_savings         double                             null,
    discharge_bill_amount    double                             null,
    discharge_amount_approved double                             null,
    discharge_claim_decision varchar(100)                       null,
    discharge_savings        double                             null,
    discharge_insurer_decision varchar(50)                        null,
    insurer_discharge_amount_approved double                             null,
    final_insurer_decision_reverse_feed varchar(50)                        null,
    final_insurer_amount_reverse_feed double                             null,
    date_created             datetime default CURRENT_TIMESTAMP null,
    date_updated             datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);