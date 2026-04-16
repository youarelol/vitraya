CREATE TABLE `niva_push_events`
(
    `id`                    bigint(20) NOT NULL AUTO_INCREMENT,
    `intimation_number`      VARCHAR(100) DEFAULT NULL,
    `status`          VARCHAR(50) DEFAULT NULL,
    `message`         VARCHAR(50) DEFAULT NULL,
    `time_taken_ms`   bigint(20)  DEFAULT NULL,
    `date_created`    datetime    DEFAULT CURRENT_TIMESTAMP,
    `date_updated`    datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);