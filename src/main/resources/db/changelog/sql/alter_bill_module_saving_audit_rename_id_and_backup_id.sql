
-- Step 1: Rename `id` to `bill_line_item_saving_id`
ALTER TABLE bill_module_saving_audit
    CHANGE COLUMN id bill_module_saving_id BIGINT;

-- Step 2: Rename `backup_id` to `id` and drop the primary key
ALTER TABLE bill_module_saving_audit
    CHANGE COLUMN backup_id id BIGINT,
DROP PRIMARY KEY;

-- Step 3: Set `id` as the new AUTO_INCREMENT PRIMARY KEY
ALTER TABLE bill_module_saving_audit
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT,
    ADD PRIMARY KEY (id);
