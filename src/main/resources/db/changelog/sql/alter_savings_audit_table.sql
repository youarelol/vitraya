alter table bill_line_item_saving
    add column category_name varchar (255);

alter table bill_line_item_saving_audit
    add column category_name varchar (255);

alter table bill_line_item_saving
    modify column date_created datetime DEFAULT CURRENT_TIMESTAMP,
    modify column date_updated datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

alter table bill_line_item_saving_audit
    modify column date_created datetime DEFAULT CURRENT_TIMESTAMP,
    modify column date_updated datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

alter table bill_category_savings
    modify column date_created datetime DEFAULT CURRENT_TIMESTAMP,
    modify column date_updated datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

alter table bill_category_savings_audit
    modify column date_created datetime DEFAULT CURRENT_TIMESTAMP,
    modify column date_updated datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

alter table bill_module_saving
    modify column date_created datetime DEFAULT CURRENT_TIMESTAMP,
    modify column date_updated datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

alter table bill_module_saving_audit
    modify column date_created datetime DEFAULT CURRENT_TIMESTAMP,
    modify column date_updated datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;