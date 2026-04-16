CREATE TABLE `benefit_code_mapping`
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT,
    `benefit_code`            varchar(255) DEFAULT NULL,
    `category`                varchar(255) DEFAULT NULL,
    `date_created`            datetime     DEFAULT CURRENT_TIMESTAMP,
    `date_updated`            datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `expense_master_category` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `bill_tariff_response`
(
    `id`                   bigint(20) NOT NULL AUTO_INCREMENT,
    `claim_data_id`        bigint(20) NOT NULL,
    `bill_identifier`      varchar(20) DEFAULT NULL,
    `bill_tariff_response` json       NOT NULL,
    `date_created`         datetime    DEFAULT CURRENT_TIMESTAMP,
    `date_updated`         datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `bill_tariff_tat`      varchar(50) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `bill_tariff_response_claim_data_id_index` (`claim_data_id`)
);

CREATE TABLE `claim_data`
(
    `id`                            bigint(20) NOT NULL AUTO_INCREMENT,
    `intimation_number`             varchar(50)    DEFAULT NULL,
    `insurance_agency_id`           bigint(20)     DEFAULT NULL,
    `tpa_id`                        int(11)        DEFAULT '0',
    `hospital_id`                   bigint(20)     DEFAULT NULL,
    `claim_type`                    varchar(20)    DEFAULT NULL,
    `patient_name`                  varchar(100)   DEFAULT NULL,
    `patient_age`                   int(11)        DEFAULT NULL,
    `designation`                   varchar(100)   DEFAULT NULL,
    `current_policy_end_date`       datetime(6)    DEFAULT NULL,
    `current_policy_start_date`     datetime(6)    DEFAULT NULL,
    `reason_for_hospitalization`    varchar(100)   DEFAULT NULL,
    `date_of_birth`                 datetime(6)    DEFAULT NULL,
    `cover_code`                    varchar(100)   DEFAULT NULL,
    `hospital_zone`                 varchar(100)   DEFAULT NULL,
    `status`                        varchar(100)   DEFAULT NULL,
    `base_sum_insured`              decimal(19, 2) DEFAULT NULL,
    `remaining_sum_insured`         decimal(19, 2) DEFAULT NULL,
    `current_policy_inception_date` datetime(6)    DEFAULT NULL,
    `is_enhancement_processed`      bit(1)         DEFAULT NULL,
    `active`                        bit(1)         DEFAULT NULL,
    `deleted`                       bit(1)         DEFAULT NULL,
    `date_created`                  datetime       DEFAULT CURRENT_TIMESTAMP,
    `date_updated`                  datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `date_of_admission`             datetime(6)    DEFAULT NULL,
    `date_of_discharge`             datetime(6)    DEFAULT NULL,
    `icd_code`                      varchar(50)    DEFAULT NULL,
    `policy_renewal_history`        json           DEFAULT NULL,
    `procedure_id`                  bigint(20)     DEFAULT NULL,
    `product_code`                  varchar(100)   DEFAULT NULL,
    `room_type`                     varchar(100)   DEFAULT NULL,
    `copay_zone`                    varchar(100)   DEFAULT NULL,
    `policy_variant`                varchar(100)   DEFAULT NULL,
    `treatment_type`                varchar(100)   DEFAULT NULL,
    `assigned`                      bit(1)         DEFAULT b'0',
    `assigned_by`                   varchar(255)   DEFAULT NULL,
    `is_insurer_visible`            bit(1)         DEFAULT b'0',
    `pushed_to_insurer`             tinyint(1)     DEFAULT '0',
    `claim_history`                 json           DEFAULT NULL,
    `policy_number`                 varchar(50)    DEFAULT NULL,
    `claim_status`                  varchar(100)   DEFAULT NULL,
    `attendant_mobile_number`       varchar(15)    DEFAULT NULL,
    `diagnosis`                     varchar(100)   DEFAULT NULL,
    `ped_list`                      json           DEFAULT NULL,
    `date_of_first_diagnosis`       datetime(6)    DEFAULT NULL,
    `adjudication_status`           varchar(100)   DEFAULT NULL,
    `medical_card_number`           varchar(25)    DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `claim_data_composite_idx` (`intimation_number`, `insurance_agency_id`, `date_created`, `active`, `deleted`),
    KEY `claim_data_date_updated_idx` (`date_updated`)
);

CREATE TABLE `claim_module_stats`
(
    `id`                 bigint(20)   NOT NULL AUTO_INCREMENT,
    `claim_data_id`      bigint(20)   NOT NULL,
    `bill_identifier`    varchar(20) DEFAULT NULL,
    `pml_identifier`     varchar(20) DEFAULT NULL,
    `medical_identifier` varchar(20) DEFAULT NULL,
    `bill_tariff_tat`    varchar(50) DEFAULT NULL,
    `pml_tat`            varchar(50) DEFAULT NULL,
    `medical_tat`        varchar(50) DEFAULT NULL,
    `claim_stage`        varchar(100) NOT NULL,
    `date_created`       datetime    DEFAULT CURRENT_TIMESTAMP,
    `date_updated`       datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `corporates`
(
    `id`                       bigint(20) NOT NULL AUTO_INCREMENT,
    `name`                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `corporate_code`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `status`                   varchar(50)                                                   DEFAULT 'ACTIVE',
    `type`                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `claim_data_parse_mapping` varchar(50)                                                   DEFAULT NULL,
    `enabled_for_review`       tinyint(1)                                                    DEFAULT '0',
    `created_by`               varchar(255)                                                  DEFAULT NULL,
    `updated_by`               varchar(255)                                                  DEFAULT NULL,
    `date_created`             datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `date_updated`             datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UC_HOSPITAL_CODE` (`corporate_code`)
);

CREATE TABLE `document_master`
(
    `id`                      bigint(20) NOT NULL AUTO_INCREMENT,
    `date_created`            datetime      DEFAULT CURRENT_TIMESTAMP,
    `date_updated`            datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `document_status`         int(11)       DEFAULT NULL,
    `document_type`           varchar(100)  DEFAULT NULL,
    `file_name`               varchar(255)  DEFAULT NULL,
    `file_supported`          bit(1)        DEFAULT NULL,
    `file_type`               varchar(255)  DEFAULT NULL,
    `parent_table_id`         bigint(20)    DEFAULT NULL,
    `storage_file_name`       varchar(255)  DEFAULT NULL,
    `omni_docs_image_index`   varchar(255)  DEFAULT NULL,
    `parent_table_intimation` varchar(100)  DEFAULT '0',
    `stage`                   varchar(255)  DEFAULT NULL,
    `latest_bill_documents`   bit(1)        DEFAULT b'0',
    `pre_signed_url`          varchar(1255) DEFAULT NULL,
    `doc_path`                varchar(255)  DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `enhancement_config`
(
    `id`                             bigint(20) NOT NULL AUTO_INCREMENT,
    `active`                         bit(1)     DEFAULT NULL,
    `date_created`                   datetime   DEFAULT CURRENT_TIMESTAMP,
    `date_updated`                   datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`                        bit(1)     DEFAULT NULL,
    `hospital_id`                    bigint(20) DEFAULT NULL,
    `insurance_agency_id`            bigint(20) DEFAULT NULL,
    `is_bill_tariff_enabled`         bit(1)     DEFAULT NULL,
    `is_pml_enabled`                 bit(1)     DEFAULT NULL,
    `is_vneuron_enabled`             bit(1)     DEFAULT NULL,
    `is_bill_enabled`                bit(1)     DEFAULT b'0',
    `is_diagnosis_construct_enabled` bit(1)     DEFAULT b'0',
    PRIMARY KEY (`id`)
);

CREATE TABLE `hospital_service_type`
(
    `id`                 int(11)      NOT NULL AUTO_INCREMENT,
    `hospital_id`        int(11)      NOT NULL,
    `hospital_room_name` varchar(150) NOT NULL,
    `vitraya_room_type`  varchar(256)          DEFAULT NULL,
    `room_category`      varchar(100) NOT NULL,
    `room_tariff_day`    double       NOT NULL,
    `service_code`       varchar(15)  NOT NULL,
    `date_created`       datetime              DEFAULT CURRENT_TIMESTAMP,
    `date_updated`       datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `enabled`            tinyint(1)            DEFAULT '1',
    `private_ac`         tinyint(1)   NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `HOSPITAL_SERVICE_TYPE_HOSPITALID_ROOMNAME_COL` (`hospital_id`, `hospital_room_name`)
);

CREATE TABLE `illnesses`
(
    `id`                  bigint(20) NOT NULL AUTO_INCREMENT,
    `default_icd_code`    varchar(50)  DEFAULT NULL,
    `category`            varchar(20)  DEFAULT NULL,
    `name`                varchar(255) DEFAULT NULL,
    `relevant_procedures` json         DEFAULT NULL,
    `related_disease`     json         DEFAULT NULL,
    `illness_code`        varchar(15)  DEFAULT NULL,
    `active`              tinyint(1)   DEFAULT '1',
    `deleted`             tinyint(1)   DEFAULT '0',
    `date_created`        datetime     DEFAULT CURRENT_TIMESTAMP,
    `date_updated`        datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `modules`
(
    `id`                  bigint(20)   NOT NULL AUTO_INCREMENT,
    `module_name`         varchar(255) NOT NULL,
    `module_display_name` varchar(255) NOT NULL,
    `can_create`          tinyint(1)        DEFAULT '0',
    `can_read`            tinyint(1)        DEFAULT '0',
    `can_update`          tinyint(1)        DEFAULT '0',
    `can_delete`          tinyint(1)        DEFAULT '0',
    `created_by`          varchar(255)      DEFAULT NULL,
    `updated_by`          varchar(255)      DEFAULT NULL,
    `date_created`        timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
    `date_updated`        timestamp    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `pml_response`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `claim_data_id`  bigint(20) NOT NULL,
    `pml_identifier` varchar(50) DEFAULT NULL,
    `pml_response`   json       NOT NULL,
    `pml_tat`        varchar(50) DEFAULT NULL,
    `date_created`   datetime    DEFAULT CURRENT_TIMESTAMP,
    `date_updated`   datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `pml_response_claim_data_id_index` (`claim_data_id`)
);

CREATE TABLE `procedures`
(
    `id`                 bigint(20)   NOT NULL AUTO_INCREMENT,
    `active`             bit(1)      DEFAULT NULL,
    `date_created`       datetime    DEFAULT CURRENT_TIMESTAMP,
    `deleted`            bit(1)      DEFAULT NULL,
    `name`               varchar(100) NOT NULL,
    `procedure_code`     varchar(50) DEFAULT NULL,
    `date_updated`       datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `vneuron_sctid_code` varchar(50) DEFAULT NULL,
    `benefit_code`       varchar(50) DEFAULT NULL,
    `pcs_code`           varchar(50) DEFAULT NULL,
    `treatment_type`     varchar(10) DEFAULT 'SURGICAL',
    PRIMARY KEY (`id`)
);

CREATE TABLE `roles`
(
    `id`              bigint(20)   NOT NULL AUTO_INCREMENT,
    `role_name`       varchar(255) NOT NULL,
    `status`          varchar(50)       DEFAULT 'ACTIVE',
    `organisation_id` bigint(20)   NOT NULL,
    `created_by`      varchar(255)      DEFAULT NULL,
    `updated_by`      varchar(255)      DEFAULT NULL,
    `date_created`    timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
    `date_updated`    timestamp    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `role_access`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `role_id`      bigint(20) NOT NULL,
    `module_id`    bigint(20) NOT NULL,
    `can_create`   tinyint(1)      DEFAULT '0',
    `can_read`     tinyint(1)      DEFAULT '0',
    `can_update`   tinyint(1)      DEFAULT '0',
    `can_delete`   tinyint(1)      DEFAULT '0',
    `created_by`   varchar(255)    DEFAULT NULL,
    `updated_by`   varchar(255)    DEFAULT NULL,
    `date_created` timestamp  NULL DEFAULT CURRENT_TIMESTAMP,
    `date_updated` timestamp  NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_role` (`role_id`),
    KEY `fk_module` (`module_id`),
    CONSTRAINT `fk_module` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`),
    CONSTRAINT `fk_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
);



CREATE TABLE `user_access`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id`      bigint(20) NOT NULL,
    `role_id`      bigint(20) NOT NULL,
    `module_id`    bigint(20) NOT NULL,
    `can_create`   tinyint(1)      DEFAULT '0',
    `can_read`     tinyint(1)      DEFAULT '0',
    `can_update`   tinyint(1)      DEFAULT '0',
    `can_delete`   tinyint(1)      DEFAULT '0',
    `created_by`   varchar(255)    DEFAULT NULL,
    `updated_by`   varchar(255)    DEFAULT NULL,
    `date_created` timestamp  NULL DEFAULT CURRENT_TIMESTAMP,
    `date_updated` timestamp  NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `users`
(
    `id`                bigint(20)   NOT NULL AUTO_INCREMENT,
    `username`          varchar(255) NOT NULL,
    `password`          varchar(255) NOT NULL,
    `email`             varchar(255)          DEFAULT NULL,
    `mobile_number`     varchar(20)           DEFAULT NULL,
    `status`            varchar(50)           DEFAULT 'ACTIVE',
    `password_attempts` int(11)               DEFAULT '0',
    `corporate_id`      bigint(20)   NOT NULL,
    `block_time`        timestamp    NULL     DEFAULT NULL,
    `created_by`        varchar(255)          DEFAULT NULL,
    `updated_by`        varchar(255)          DEFAULT NULL,
    `date_created`      timestamp    NULL     DEFAULT CURRENT_TIMESTAMP,
    `date_updated`      timestamp    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `role`              bigint(20)   NOT NULL DEFAULT '0',
    `userid`            varchar(25)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`),
    KEY `fk_corporates` (`corporate_id`),
    CONSTRAINT `fk_corporates` FOREIGN KEY (`corporate_id`) REFERENCES `corporates` (`id`)
);

CREATE TABLE `vneuron_response`
(
    `id`                 bigint(20) NOT NULL AUTO_INCREMENT,
    `claim_data_id`      bigint(20) NOT NULL,
    `medical_identifier` varchar(50) DEFAULT NULL,
    `vneuron_response`   json       NOT NULL,
    `date_created`       datetime    DEFAULT CURRENT_TIMESTAMP,
    `date_updated`       datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `vneuron_tat`        varchar(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
);
