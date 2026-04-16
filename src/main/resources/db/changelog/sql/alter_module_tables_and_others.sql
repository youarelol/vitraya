alter table vneuron_response add column vneuron_decision varchar(50);
alter table pml_response add column pml_decision varchar(50);
alter table bill_tariff_response add column bill_tariff_decision varchar(50);

alter table claim_data add column in_scope_hospital bit(1) DEFAULT 1;
alter table claim_data add column in_scope_policy bit(1) DEFAULT 1;
alter table claim_data add column in_scope_procedure bit(1) DEFAULT 1;