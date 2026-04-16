# Claim Data
alter table claim_data
    add column txn_id varchar(13) not null default '';

# Claim Admission Details
alter table claim_admission_details
    add column txn_id varchar(13) not null default '';

# Claim Module Status
alter table claim_module_stats
    add column txn_id varchar(13) not null default '';

# Claim Adjudication Details
alter table claim_adjudication_result
    add column txn_id varchar(13) not null default '';