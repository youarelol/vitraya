DELIMITER //

CREATE TRIGGER trg_update_claim_tat
    AFTER UPDATE
    ON claim_module_stats
    FOR EACH ROW
BEGIN
    -- Check if all three identifiers are not null
    IF NEW.bill_identifier IS NOT NULL AND
       NEW.pml_identifier IS NOT NULL AND
       NEW.medical_identifier IS NOT NULL THEN
        -- Update claim_end_time and claim_tat in the table
        UPDATE claim_module_stats
        SET claim_end_time = CURRENT_TIMESTAMP,
            claim_tat      = TIMESTAMPDIFF(MICROSECOND, NEW.claim_start_time, CURRENT_TIMESTAMP) / 1000
        WHERE id = NEW.id;
    END IF;
END;
//

DELIMITER ;

-- PML Response Trigger
DELIMITER //

CREATE TRIGGER trg_pml_response
    AFTER INSERT ON pml_response
    FOR EACH ROW
BEGIN
    DECLARE bill_tariff_decision_val VARCHAR(50);
    DECLARE vneuron_decision_val VARCHAR(50);

    -- Fetch corresponding decisions
    SELECT bill_tariff_decision INTO bill_tariff_decision_val
    FROM bill_tariff_response
    WHERE claim_data_id = NEW.claim_data_id;

    SELECT vneuron_decision INTO vneuron_decision_val
    FROM vneuron_response
    WHERE claim_data_id = NEW.claim_data_id;

    -- Check and update adjudication_status in claim_data
    IF bill_tariff_decision_val IS NOT NULL AND NEW.pml_decision IS NOT NULL AND vneuron_decision_val IS NOT NULL THEN
        IF bill_tariff_decision_val = 'TARIFF_APPLIED' AND NEW.pml_decision = 'true' AND vneuron_decision_val = 'ACCEPTED' THEN
            UPDATE claim_data SET adjudication_status = 'APPROVED' WHERE id = NEW.claim_data_id;
        ELSEIF NEW.pml_decision = 'false' OR vneuron_decision_val = 'REJECTED' THEN
            UPDATE claim_data SET adjudication_status = 'Denied' WHERE id = NEW.claim_data_id;
        ELSEIF vneuron_decision_val = 'QUERY' THEN
            UPDATE claim_data SET adjudication_status = 'QUERY' WHERE id = NEW.claim_data_id;
        END IF;
    END IF;
END;
//

DELIMITER ;

-- VNeuron Response Trigger
DELIMITER //

CREATE TRIGGER trg_vneuron_response
    AFTER INSERT ON vneuron_response
    FOR EACH ROW
BEGIN
    DECLARE bill_tariff_decision_val VARCHAR(50);
    DECLARE pml_decision_val VARCHAR(50);

    -- Fetch corresponding decisions
    SELECT bill_tariff_decision INTO bill_tariff_decision_val
    FROM bill_tariff_response
    WHERE claim_data_id = NEW.claim_data_id;

    SELECT pml_decision INTO pml_decision_val
    FROM pml_response
    WHERE claim_data_id = NEW.claim_data_id;

    -- Check and update adjudication_status in claim_data
    IF bill_tariff_decision_val IS NOT NULL AND pml_decision_val IS NOT NULL AND NEW.vneuron_decision IS NOT NULL THEN
        IF bill_tariff_decision_val = 'TARIFF_APPLIED' AND pml_decision_val = 'true' AND NEW.vneuron_decision = 'ACCEPTED' THEN
            UPDATE claim_data SET adjudication_status = 'APPROVED' WHERE id = NEW.claim_data_id;
        ELSEIF pml_decision_val = 'false' OR NEW.vneuron_decision = 'REJECTED' THEN
            UPDATE claim_data SET adjudication_status = 'Denied' WHERE id = NEW.claim_data_id;
        ELSEIF NEW.vneuron_decision = 'QUERY' THEN
            UPDATE claim_data SET adjudication_status = 'QUERY' WHERE id = NEW.claim_data_id;
        END IF;
    END IF;
END;
//

DELIMITER ;

-- Bill Tariff Response Trigger
DELIMITER //

CREATE TRIGGER trg_bill_tariff_response
    AFTER INSERT ON bill_tariff_response
    FOR EACH ROW
BEGIN
    DECLARE pml_decision_val VARCHAR(50);
    DECLARE vneuron_decision_val VARCHAR(50);

    -- Fetch corresponding decisions
    SELECT pml_decision INTO pml_decision_val
    FROM pml_response
    WHERE claim_data_id = NEW.claim_data_id;

    SELECT vneuron_decision INTO vneuron_decision_val
    FROM vneuron_response
    WHERE claim_data_id = NEW.claim_data_id;

    -- Check and update adjudication_status in claim_data
    IF NEW.bill_tariff_decision IS NOT NULL AND pml_decision_val IS NOT NULL AND vneuron_decision_val IS NOT NULL THEN
        IF NEW.bill_tariff_decision = 'TARIFF_APPLIED' AND pml_decision_val = 'true' AND vneuron_decision_val = 'ACCEPTED' THEN
            UPDATE claim_data SET adjudication_status = 'APPROVED' WHERE id = NEW.claim_data_id;
        ELSEIF pml_decision_val = 'false' OR vneuron_decision_val = 'REJECTED' THEN
            UPDATE claim_data SET adjudication_status = 'Denied' WHERE id = NEW.claim_data_id;
        ELSEIF vneuron_decision_val = 'QUERY' THEN
            UPDATE claim_data SET adjudication_status = 'QUERY' WHERE id = NEW.claim_data_id;
        END IF;
    END IF;
END;
//

DELIMITER ;