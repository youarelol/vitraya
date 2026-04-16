CREATE table claim_riders_details (
                                      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      claim_data_id BIGINT NOT NULL,
                                      intimation_number  VARCHAR(100) DEFAULT NULL,
                                      refill_flag_policy VARCHAR(50) DEFAULT NULL,
                                      policy_ported VARCHAR(50) DEFAULT NULL,
                                      reassure_benefit_amount VARCHAR(50) DEFAULT NULL,
                                      safeguard  tinyint(1) DEFAULT 0,
                                      safeguard_plus tinyint(1)  DEFAULT 0
);