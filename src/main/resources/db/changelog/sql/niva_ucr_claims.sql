CREATE TABLE `niva_ucr_claims`
(
    `id`                    bigint(20) NOT NULL AUTO_INCREMENT,
    `date_created`          timestamp NULL                         DEFAULT CURRENT_TIMESTAMP,
    `date_updated`          timestamp NULL                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `claim_data_id`         bigint NOT NULL,
    `pml_approved_amount`   DECIMAL(19, 2)                            DEFAULT NULL,
    `ucr_amount`            DECIMAL(19, 2)                            DEFAULT NULL,
    `final_approved_amount` DECIMAL(19, 2) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
);