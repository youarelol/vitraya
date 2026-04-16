alter table bill_module_saving
    modify column claim_data_id bigint(20) NOT NULL;

alter table bill_module_saving_audit
    modify column claim_data_id bigint(20) NOT NULL;