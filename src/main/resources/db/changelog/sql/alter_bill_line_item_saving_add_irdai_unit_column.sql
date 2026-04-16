ALTER TABLE bill_line_item_saving
    ADD COLUMN irdai_payable BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN insurer_irdai_payable BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN unit INT,
ADD COLUMN insurer_unit INT;
