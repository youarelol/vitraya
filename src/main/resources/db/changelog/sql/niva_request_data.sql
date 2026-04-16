CREATE TABLE `niva_request_data`
(
    `id`             int          NOT NULL AUTO_INCREMENT,
    `claim_data_id`  int          NOT NULL,
    `request_type`   varchar(100) NOT NULL,
    `request_data`   json                  DEFAULT NULL,
    `is_success`     tinyint(1)   NOT NULL DEFAULT '0',
    `create_date`    datetime     NOT NULL,
    `error_msg`      varchar(2000)         DEFAULT NULL,
    `wdms_unique_no` varchar(100)          DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `claim_id_idx` (`claim_data_id`)
);

CREATE TABLE `insurer_fetch_responses`
(
    `id`                      bigint       NOT NULL AUTO_INCREMENT,
    `claim_intimation_number` varchar(512) NOT NULL,
    `claim_data_id`           bigint DEFAULT NULL,
    `policy_data`             json   DEFAULT NULL,
    `claim_history`           json   DEFAULT NULL,
    `date_created`            date   DEFAULT NULL,
    `date_updated`            date   DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `claim_intimation_number` (`claim_intimation_number`),
    KEY `insurer_fetch_responses_index` (`claim_data_id`)
);

CREATE TABLE `day_care_procedures_mapping`
(
    `id`             int NOT NULL AUTO_INCREMENT,
    `procedure_id`   bigint       DEFAULT NULL,
    `procedure_name` varchar(200) DEFAULT NULL,
    PRIMARY KEY (`id`)
);