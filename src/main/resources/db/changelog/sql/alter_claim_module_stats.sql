alter table claim_module_stats
    add column claim_tat int(50) DEFAULT NULL after medical_tat;
alter table claim_module_stats
    modify bill_tariff_tat int default 0;
alter table claim_module_stats
    modify pml_tat int default 0;
alter table claim_module_stats
    modify medical_tat int default 0;
alter table claim_module_stats
    add column claim_start_time datetime DEFAULT CURRENT_TIMESTAMP after claim_tat;
alter table claim_module_stats
    add column claim_end_time datetime DEFAULT CURRENT_TIMESTAMP after claim_tat;

alter table bill_tariff_response modify column bill_identifier varchar(50) DEFAULT NULL;