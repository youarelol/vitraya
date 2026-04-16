alter table bill_line_item_saving add column unit int not null default 0;
alter table bill_line_item_saving_audit add column unit int not null default 0;

alter table bill_line_item_saving add column irdai_payable tinyint(1) not null default 1;
alter table bill_line_item_saving_audit add column irdai_payable tinyint(1) not null default 1;

alter table bill_line_item_saving add column insurer_irdai_payable tinyint(1) not null default 1;
alter table bill_line_item_saving_audit add column insurer_irdai_payable tinyint(1) not null default 1;