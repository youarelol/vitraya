CREATE TABLE `policy_product`
(
    `id`                  bigint(20) NOT NULL AUTO_INCREMENT,
    `active`              bit(1)                                  DEFAULT NULL,
    `date_created`        timestamp  NULL                         DEFAULT CURRENT_TIMESTAMP,
    `date_updated`        timestamp  NULL                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`             bit(1)                                  DEFAULT NULL,
    `insurance_agency_id` bigint(20)                              DEFAULT NULL,
    `policy_name`         varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `product_code`        varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
);

INSERT INTO policy_product (policy_name, product_code, insurance_agency_id, active, deleted, date_created)
VALUES ('ReAssure –Individual Basis', 'NIVA_REASSURE2.0', 5, true, false, now());
