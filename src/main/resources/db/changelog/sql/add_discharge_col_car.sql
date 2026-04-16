alter table claim_adjudication_result add column discharge_insurer_decision varchar(255) NULL;
alter table claim_adjudication_result add column discharge_bill_amount double NULL;
alter table claim_adjudication_result add column discharge_amount_approved double NULL;
alter table claim_adjudication_result add column pre_auth_savings double NULL;
alter table claim_adjudication_result add column discharge_savings double NULL;