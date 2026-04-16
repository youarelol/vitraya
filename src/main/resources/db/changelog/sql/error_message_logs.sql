CREATE TABLE `error_message_logs`
(
    `id`                bigint(20) NOT NULL AUTO_INCREMENT,
    `claim_data_id`     bigint NOT NULL,
    `error_reason`      varchar(100) DEFAULT NULL,
    `error_message`     varchar(200) DEFAULT NULL,
    `failure_engine`     varchar(50)  DEFAULT NULL,
    `date_created`      timestamp NULL                         DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
