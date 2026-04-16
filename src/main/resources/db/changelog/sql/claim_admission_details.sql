CREATE TABLE `claim_admission_details`
(
    `id`              bigint     NOT NULL AUTO_INCREMENT,
    `claim_data_id`   bigint     NOT NULL,
    `admission_date`  datetime   NOT NULL,
    `discharge_date`  datetime   NOT NULL,
    `cost_estimation` json                DEFAULT NULL,
    `is_package`      tinyint(1) NOT NULL DEFAULT '0',
    `package_amount`  double     NOT NULL DEFAULT '0',
    `date_created`    datetime            DEFAULT CURRENT_TIMESTAMP,
    `date_updated`    datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);