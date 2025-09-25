CREATE
DATABASE IF NOT EXISTS job_huntly_local
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

use job_huntly_local;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS levels;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS majors;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS saved_job;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS report_type;
DROP TABLE IF EXISTS work_type;
DROP TABLE IF EXISTS location_city;
DROP TABLE IF EXISTS location_ward;
DROP TABLE IF EXISTS job_skill;
DROP TABLE IF EXISTS ward_job;
DROP TABLE IF EXISTS ward_company;
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS job_level;
DROP TABLE IF EXISTS job_work_type;
DROP TABLE IF EXISTS job_major;
DROP TABLE IF EXISTS candidate_certi;
DROP TABLE IF EXISTS certificates;
DROP TABLE IF EXISTS candidate_profile;
DROP TABLE IF EXISTS candidate_skill;
DROP TABLE IF EXISTS candidate_exper;
DROP TABLE IF EXISTS work_experience;
DROP TABLE IF EXISTS candidate_edu;
DROP TABLE IF EXISTS edu;


Create table `users`
(
    `user_id`                   INT          NOT NULL AUTO_INCREMENT,
    `city_id`                   INT NULL,
    `role_id`                   INT          NOT NULL,
    `full_name`                 VARCHAR(200) NULL,
    `email`                     VARCHAR(200) NOT NULL UNIQUE,
    `password_hash`             VARCHAR(255) NULL,
    `phone_number`              VARCHAR(20) NULL UNIQUE,
    `status`                    ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'INACTIVE',
    `activation_token`          VARCHAR(64) NULL,
    `google_id`                 VARCHAR(100) UNIQUE,
    `is_active`                 TINYINT(1) DEFAULT 0,
    `sms_notification_active`   TINYINT(1) DEFAULT 0,
    `email_notification_active` TINYINT(1) DEFAULT 0,
    `create_at`                 DATETIME DEFAULT CURRENT_TIMESTAMP(),
    Primary Key (`user_id`)
);


Create table `roles`
(
    `role_id`   INT          NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(200) NOT NULL,
    Primary Key (`role_id`)
);

Create table `categories`
(
    `cate_id`   INT NOT NULL AUTO_INCREMENT,
    `cate_name` VARCHAR(200) NULL,
    Primary Key (`cate_id`)
);

CREATE TABLE companies
(
    company_id        INT          NOT NULL AUTO_INCREMENT,
    user_id           INT          NOT NULL,
    company_name      VARCHAR(200) NOT NULL,
    description       TEXT NULL,
    email             VARCHAR(200) NOT NULL,
    phone_number      VARCHAR(50) NULL,
    website           VARCHAR(255) NULL,
    address           VARCHAR(255) NULL,
    location_city     VARCHAR(100) NULL,
    location_country  VARCHAR(100) NULL,
    founded_year YEAR NULL,
    quantity_employee INT NULL,
    status            ENUM('active','inactive','banned') DEFAULT 'inactive',
    is_pro_company    TINYINT(1) DEFAULT 0,
    followers_count   INT      DEFAULT 0,
    jobs_count        INT      DEFAULT 0,
    facebook_url      VARCHAR(255) NULL,
    twitter_url       VARCHAR(255) NULL,
    linkedin_url      VARCHAR(255) NULL,
    map_embed_url     TEXT NULL,
    avatar            VARCHAR(255) NULL,
    avatar_cover      VARCHAR(255) NULL,
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE company_categories
(
    company_id INT NOT NULL,
    cate_id    INT NOT NULL,
    PRIMARY KEY (company_id, cate_id),
    FOREIGN KEY (company_id) REFERENCES companies (company_id) ON DELETE CASCADE,
    FOREIGN KEY (cate_id) REFERENCES categories (cate_id) ON DELETE CASCADE
);

Create table `follows`
(
    `user_id`    INT NOT NULL,
    `company_id` INT NOT NULL,
    Primary Key (`user_id`, `company_id`)
);


Create table `jobs`
(
    `job_id`       INT NOT NULL primary key AUTO_INCREMENT,
    `company_id`   INT NOT NULL,
    `title`        VARCHAR(200) NOT NULL,
    `date_post`    DATE NULL,
    `description`  TEXT NULL,
    `expired_date` DATE NULL,
    `salary_min`   VARCHAR(200) NULL,
    `salary_max`   VARCHAR(200) NULL,
    `requirements` TEXT NULL,
    `benefits`     TEXT NULL,
    `location`     VARCHAR(200) NULL,
    `status`       VARCHAR(200) NULL,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE ON UPDATE CASCADE
);

Create table `levels`
(
    `level_id`   INT NOT NULL AUTO_INCREMENT,
    `level_name` VARCHAR(200) NULL,
    Primary Key (`level_id`)
);

Create table `majors`
(
    `major_id`   INT NOT NULL AUTO_INCREMENT,
    `major_name` VARCHAR(200) NULL,
    Primary Key (`major_id`)
);


Create table `skills`
(
    `skill_id`   INT NOT NULL AUTO_INCREMENT,
    `skill_name` VARCHAR(200) NULL,
    Primary Key (`skill_id`)
);


Create table `saved_job`
(
    `user_id` INT NOT NULL,
    `job_id`  INT NOT NULL,
    Primary Key (`user_id`, `job_id`)
);


Create table `reports`
(
    `report_id`           INT NOT NULL AUTO_INCREMENT,
    `report_type_id`      INT NOT NULL,
    `user_id`             INT NOT NULL,
    `description`         VARCHAR(200) NULL,
    `create_at`           Datetime NULL,
    `reported_content_id` INT NOT NULL,
    `status`              VARCHAR(200) NULL,
    Primary Key (`report_id`)
);


Create table `report_type`
(
    `report_type_id` INT NOT NULL AUTO_INCREMENT,
    `type_name`      VARCHAR(200) NULL,
    Primary Key (`report_type_id`)
);


Create table `work_type`
(
    `work_type_id`   INT NOT NULL AUTO_INCREMENT,
    `work_type_name` VARCHAR(200) NULL,
    Primary Key (`work_type_id`)
);


Create table `location_city`
(
    `city_id`   INT NOT NULL AUTO_INCREMENT,
    `city_name` VARCHAR(200) NULL,
    Primary Key (`city_id`)
);


Create table `location_ward`
(
    `ward_id`   INT NOT NULL AUTO_INCREMENT,
    `ward_name` VARCHAR(200) NULL,
    `city_id`   INT NOT NULL,
    Primary Key (`ward_id`)
);


Create table `job_skill`
(
    `skill_id` INT NOT NULL,
    `job_id`   INT NOT NULL,
    Primary Key (`skill_id`, `job_id`)
);


Create table `candidate_profile`
(
    `user_id`       INT NOT NULL,
    `profile_id`    INT NOT NULL AUTO_INCREMENT,
    `gender`        ENUM('Male', 'Female', 'Other'),
    `avatar`        VARCHAR(200) NULL,
    `about_me`      TEXT NULL,
    `fullname`      VARCHAR(200) NULL,
    `personal_link` VARCHAR(200) NULL,
    `date_of_birth` DATE NULL,
    `title`         VARCHAR(200) NULL,
    Primary Key (`profile_id`)
);


CREATE table `candidate_skill`
(
    `skill_id`   INT NOT NULL,
    `profile_id` INT NOT NULL,
    Primary Key (`skill_id`, `profile_id`)
);


Create table `ward_job`
(
    `ward_id` INT NOT NULL,
    `job_id`  INT NOT NULL,
    Primary Key (`ward_id`, `job_id`)
);


Create table `ward_company`
(
    `ward_id`    INT NOT NULL,
    `company_id` INT NOT NULL,
    Primary Key (`ward_id`, `company_id`)
);


Create table `applications`
(
    `user_id`        INT NOT NULL,
    `job_id`         INT NOT NULL,
    `cv`             VARCHAR(200) NULL,
    `email`          VARCHAR(200) NULL,
    `status`         VARCHAR(200) NULL,
    `phone_number`   VARCHAR(200) NULL,
    `candidate_name` VARCHAR(200) NULL,
    Primary Key (`user_id`, `job_id`)
);


Create table `job_level`
(
    `level_id` INT NOT NULL,
    `job_id`   INT NOT NULL,
    Primary Key (`level_id`, `job_id`)
);


Create table `job_work_type`
(
    `job_id`       INT NOT NULL,
    `work_type_id` INT NOT NULL,
    Primary Key (`job_id`, `work_type_id`)
);


Create table `job_major`
(
    `job_id`   INT NOT NULL,
    `major_id` INT NOT NULL,
    Primary Key (`job_id`, `major_id`)
);


Create table `certificates`
(
    `cer_id`      INT NOT NULL AUTO_INCREMENT,
    `cer_name`    VARCHAR(200) NULL,
    `date`        DATE NULL,
    `description` VARCHAR(200) NULL,
    `issuer`      VARCHAR(200) NULL,
    Primary Key (`cer_id`)
);


Create table `candidate_certi`
(
    `cer_id`     INT NOT NULL,
    `profile_id` INT NOT NULL,
    Primary Key (`cer_id`, `profile_id`)
);


Create table `work_experience`
(
    `exper_id`     INT NOT NULL AUTO_INCREMENT,
    `description`  TEXT NULL,
    `company_name` VARCHAR(200) NULL,
    `position`     VARCHAR(200) NULL,
    `duration`     VARCHAR(200) NULL,
    Primary Key (`exper_id`)
);


Create table `candidate_exper`
(
    `exper_id`   INT NOT NULL,
    `profile_id` INT NOT NULL,
    Primary Key (`exper_id`, `profile_id`)
);


Create table `edu`
(
    `edu_id`      INT NOT NULL AUTO_INCREMENT,
    `school_name` VARCHAR(200) NULL,
    `degree`      VARCHAR(200) NULL,
    `duration`    VARCHAR(200) NULL,
    `majors`      VARCHAR(200) NULL,
    Primary Key (`edu_id`)
);


Create table `candidate_edu`
(
    `edu_id`     INT NOT NULL,
    `profile_id` INT NOT NULL,
    Primary Key (`edu_id`, `profile_id`)
);


Alter table `companies`
    add foreign key (`user_id`) references `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `follows`
    add foreign key (`user_id`) references `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `saved_job`
    add foreign key (`user_id`) references `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `reports`
    add foreign key (`user_id`) references `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_profile`
    add foreign key (`user_id`) references `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `users`
    add foreign key (`role_id`) references `roles` (`role_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `follows`
    add foreign key (`company_id`) references `companies` (`company_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `ward_company`
    add foreign key (`company_id`) references `companies` (`company_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `saved_job`
    add foreign key (`job_id`) references `jobs` (`job_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_skill`
    add foreign key (`job_id`) references `jobs` (`job_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `ward_job`
    add foreign key (`job_id`) references `jobs` (`job_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `applications`
    add foreign key (`job_id`) references `jobs` (`job_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_level`
    add foreign key (`job_id`) references `jobs` (`job_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_work_type`
    add foreign key (`job_id`) references `jobs` (`job_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_major`
    add foreign key (`job_id`) references `jobs` (`job_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_level`
    add foreign key (`level_id`) references `levels` (`level_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_major`
    add foreign key (`major_id`) references `majors` (`major_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_skill`
    add foreign key (`skill_id`) references `skills` (`skill_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `reports`
    add foreign key (`report_type_id`) references `report_type` (`report_type_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `job_work_type`
    add foreign key (`work_type_id`) references `work_type` (`work_type_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `users`
    add foreign key (`city_id`) references `location_city` (`city_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `location_ward`
    add foreign key (`city_id`) references `location_city` (`city_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `ward_job`
    add foreign key (`ward_id`) references `location_ward` (`ward_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `ward_company`
    add foreign key (`ward_id`) references `location_ward` (`ward_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `applications`
    add foreign key (`user_id`) references `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_certi`
    add foreign key (`profile_id`) references `candidate_profile` (`profile_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_exper`
    add foreign key (`profile_id`) references `candidate_profile` (`profile_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_edu`
    add foreign key (`profile_id`) references `candidate_profile` (`profile_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_certi`
    add foreign key (`cer_id`) references `certificates` (`cer_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_exper`
    add foreign key (`exper_id`) references `work_experience` (`exper_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_edu`
    add foreign key (`edu_id`) references `edu` (`edu_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_skill`
    add foreign key (`skill_id`) references `skills` (`skill_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
Alter table `candidate_skill`
    add foreign key (`profile_id`) references `candidate_profile` (`profile_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;
ALTER TABLE `skills`
    ADD COLUMN `cate_id` INT NULL,
   ADD CONSTRAINT `fk_skills_categories`
    FOREIGN KEY (`cate_id`)
    REFERENCES `categories`(`cate_id`)
    ON
UPDATE CASCADE
ON
DELETE
SET NULL;

-- 1) Thêm parent_id để biến categories thành cây (Category -> Major)
ALTER TABLE `categories`
    ADD COLUMN `parent_id` INT NULL AFTER `cate_name`;

ALTER TABLE `categories`
    ADD CONSTRAINT `fk_categories_parent`
        FOREIGN KEY (`parent_id`) REFERENCES `categories` (`cate_id`)
            ON UPDATE CASCADE ON DELETE CASCADE;

-- 2) Tạo bảng N–N giữa skills và categories
CREATE TABLE `skill_categories`
(
    `skill_id` INT NOT NULL,
    `cate_id`  INT NOT NULL,
    PRIMARY KEY (`skill_id`, `cate_id`),
    CONSTRAINT `fk_skill_categories_skill`
        FOREIGN KEY (`skill_id`) REFERENCES `skills` (`skill_id`)
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_skill_categories_category`
        FOREIGN KEY (`cate_id`) REFERENCES `categories` (`cate_id`)
            ON UPDATE CASCADE ON DELETE CASCADE
);

-- 3) Gỡ liên kết trực tiếp từ skills -> categories và bỏ cột cate_id
ALTER TABLE `skills` DROP FOREIGN KEY `fk_skills_categories`;
ALTER TABLE `skills` DROP COLUMN `cate_id`;

-- 4) Drop các bảng không dùng nữa
DROP TABLE IF EXISTS `job_major`;
DROP TABLE IF EXISTS `majors`;



CREATE TABLE IF NOT EXISTS `job_category`
(
    `job_id`
    INT
    NOT
    NULL,
    `cate_id`
    INT
    NOT
    NULL,
    PRIMARY
    KEY
(
    `job_id`,
    `cate_id`
),
    CONSTRAINT `fk_job_category_job`
    FOREIGN KEY
(
    `job_id`
) REFERENCES `jobs`
(
    `job_id`
)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_job_category_category`
    FOREIGN KEY
(
    `cate_id`
) REFERENCES `categories`
(
    `cate_id`
)
    ON DELETE CASCADE
    ON UPDATE CASCADE
    );

SET
@fk_name := (
  SELECT k.CONSTRAINT_NAME
  FROM information_schema.KEY_COLUMN_USAGE k
  WHERE k.TABLE_SCHEMA = DATABASE()
    AND k.TABLE_NAME   = 'jobs'
    AND k.COLUMN_NAME  = 'cate_id'
    AND k.REFERENCED_TABLE_NAME = 'categories'
  LIMIT 1
);
SET
@sql := IFNULL(CONCAT('ALTER TABLE `jobs` DROP FOREIGN KEY `', @fk_name, '`'), 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `jobs`
    MODIFY COLUMN `salary_min` BIGINT NULL,
    MODIFY COLUMN `salary_max` BIGINT NULL,
    ADD COLUMN `salary_type` TINYINT NOT NULL DEFAULT 0 AFTER `salary_max`;


-- 0) Dọn bảng backup nếu đã tồn tại từ lần chạy trước
DROP TABLE IF EXISTS `saved_job`;

-- 1) Tạo bảng mới với cấu trúc mong muốn
CREATE TABLE `saved_job` (
  `save_job_id` INT NOT NULL AUTO_INCREMENT,
  `user_id`     INT NOT NULL,
  `job_id`      INT NOT NULL,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`save_job_id`),

  CONSTRAINT `fk_saved_job_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_saved_job_job`
    FOREIGN KEY (`job_id`) REFERENCES `jobs` (`job_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
);

-- edit database by VHP --
DROP TABLE IF EXISTS candidate_edu;
ALTER TABLE edu
    ADD COLUMN profile_id INT NOT NULL,
    ADD FOREIGN KEY (profile_id) REFERENCES candidate_profile(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD INDEX idx_profile_id (profile_id);  -- Index cho query nhanh


-- Cho work_experience
DROP TABLE IF EXISTS candidate_exper;
ALTER TABLE work_experience
    ADD COLUMN profile_id INT NOT NULL,
    ADD FOREIGN KEY (profile_id) REFERENCES candidate_profile(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD INDEX idx_profile_id (profile_id);


-- Cho certificates
DROP TABLE IF EXISTS candidate_certi;
ALTER TABLE certificates
    ADD COLUMN profile_id INT NOT NULL,
    ADD FOREIGN KEY (profile_id) REFERENCES candidate_profile(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD INDEX idx_profile_id (profile_id);

-- Thêm awards nếu cần (5 sections: edu, experience, cert, awards, skills)
CREATE TABLE IF NOT EXISTS awards (
    award_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NULL,
    issuer VARCHAR(200) NULL,
    date DATE NULL,
    description VARCHAR(200) NULL,
    profile_id INT NOT NULL,
    PRIMARY KEY (award_id),
    FOREIGN KEY (profile_id) REFERENCES candidate_profile(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_profile_id (profile_id)
);
-- sẽ có 2 skill là core skill và soft skill cho candidate
-- skill hiện tại sẽ là core skill, soft skill sẽ là bảng riêng cho phép nhập từ phía người dùng
DROP TABLE IF EXISTS candidate_skill;
-- core skill 
CREATE TABLE candidate_skill (
    skill_id   INT NOT NULL,
    profile_id INT NOT NULL,
    level_id   INT NULL,
    PRIMARY KEY (skill_id, profile_id),
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (profile_id) REFERENCES candidate_profile(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (level_id) REFERENCES levels(level_id) ON DELETE SET NULL ON UPDATE CASCADE
);
-- soft skill
CREATE TABLE soft_skills (
    soft_skill_id INT NOT NULL AUTO_INCREMENT,
    profile_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(255) NULL,
    level ENUM('Low','Medium','High') NULL,
    PRIMARY KEY (soft_skill_id),
    FOREIGN KEY (profile_id) REFERENCES candidate_profile(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_profile_id (profile_id)
);

DROP TABLE IF EXISTS candidate_skill;

CREATE TABLE candidate_skill (
    candidate_skill_id INT NOT NULL AUTO_INCREMENT,
    skill_id   INT NOT NULL,
    profile_id INT NOT NULL,
    level_id   INT NULL,
    PRIMARY KEY (candidate_skill_id),
    UNIQUE KEY unique_profile_skill (skill_id, profile_id),
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (profile_id) REFERENCES candidate_profile(profile_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (level_id) REFERENCES levels(level_id) ON DELETE SET NULL ON UPDATE CASCADE
);

ALTER TABLE `candidate_profile`
    DROP COLUMN `fullname`;

CREATE TABLE cv_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    html_url VARCHAR(500) NOT NULL,
    preview_image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE users
    ADD COLUMN activation_token_expires_at DATETIME NULL;

ALTER TABLE companies
  ADD COLUMN vip_until DATETIME NULL,
  ADD INDEX idx_companies_vip_until (vip_until);

ALTER TABLE companies ADD COLUMN is_vip TINYINT(1) NOT NULL DEFAULT 0;

-- 1) Gói VIP (có thể thêm gói 3/6/12 tháng sau này)
CREATE TABLE IF NOT EXISTS packages (
  package_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  code           VARCHAR(50) UNIQUE NOT NULL,      -- 'VIP_1M'
  name           VARCHAR(120) NOT NULL,            -- 'VIP 1 Month'
  type           VARCHAR(50) DEFAULT 'VIP',
  duration_days  INT NOT NULL DEFAULT 30,
  price_vnd      BIGINT NOT NULL,
  is_active      TINYINT(1) NOT NULL DEFAULT 1,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2) Lịch sử thanh toán (đa cổng: VNPay, MoMo...)
CREATE TABLE IF NOT EXISTS payments (
  payment_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id        BIGINT NOT NULL,
  package_id        BIGINT NULL,
  amount_vnd        BIGINT NOT NULL,
  currency          CHAR(3) NOT NULL DEFAULT 'VND',
  status            ENUM('PENDING','REQUIRES_ACTION','PAID','FAILED','REFUNDED','PARTIALLY_REFUNDED','CHARGEBACK') NOT NULL DEFAULT 'PENDING',
  provider          VARCHAR(32) NOT NULL,          -- 'VNPAY','MOMO',...
  method            VARCHAR(32) NULL,              -- 'QR','WALLET',...
  txn_ref           VARCHAR(64) NOT NULL,          -- idempotency key nội bộ
  provider_txn      VARCHAR(128) NULL,             -- transactionNo/transId
  provider_order_id VARCHAR(128) NULL,             -- orderId của provider nếu có
  metadata_json     JSON NULL,
  paid_at           DATETIME NULL,
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_txn_ref (txn_ref),
  UNIQUE KEY uq_provider_txn (provider, provider_txn),
  KEY idx_company_created (company_id, created_at)
);

-- 3) Subscription VIP theo thời hạn
CREATE TABLE IF NOT EXISTS company_subscriptions (
  subscription_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id        BIGINT NOT NULL,
  package_id        BIGINT NOT NULL,
  status            ENUM('ACTIVE','EXPIRED','CANCELLED') NOT NULL,
  start_at          DATETIME NOT NULL,
  end_at            DATETIME NOT NULL,
  latest_payment_id BIGINT NULL,
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_company_end (company_id, end_at)
);

INSERT INTO packages (code, name, type, duration_days, price_vnd, is_active)
VALUES ('VIP_1M', 'VIP 1 Month', 'VIP', 30, 100000, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), duration_days=VALUES(duration_days), price_vnd=VALUES(price_vnd), is_active=VALUES(is_active);

ALTER TABLE "applications"
  ADD COLUMN "attempt_count" INT NOT NULL DEFAULT 1 AFTER "status",
  ADD COLUMN "last_user_action_at" DATETIME NULL AFTER "attempt_count";


CREATE TABLE IF NOT EXISTS `tickets` (
     `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
     `subject` VARCHAR(500),
    `from_email` VARCHAR(320) NOT NULL,
    `customer_email` VARCHAR(254),
    `thread_id` VARCHAR(512) NOT NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `status` ENUM('OPEN', 'PENDING', 'CLOSED') NOT NULL DEFAULT 'OPEN',

    UNIQUE KEY `uk_tickets_thread_id` (`thread_id`),
    KEY `ix_tickets_created_at` (`created_at` DESC),
    KEY `ix_tickets_status` (`status`),
    KEY `ix_tickets_customer_email` (`customer_email`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS `ticket_messages` (
     `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
     `ticket_id` BIGINT NOT NULL,
     `message_id` VARCHAR(512) NOT NULL,
    `in_reply_to` VARCHAR(512),
    `from_email` VARCHAR(320) NOT NULL,
    `sent_at` TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    `body_text` LONGTEXT,
    `body_html` LONGTEXT,
    `direction` ENUM('INBOUND','OUTBOUND') NOT NULL DEFAULT 'INBOUND',


    CONSTRAINT `fk_ticket_messages_ticket`
    FOREIGN KEY (`ticket_id`) REFERENCES `tickets`(`id`)
    ON DELETE CASCADE,
    UNIQUE KEY `uk_ticket_messages_message_id` (`message_id`),
    KEY `ix_ticket_messages_ticket_id` (`ticket_id`),
    KEY `ix_ticket_messages_sent_at` (`sent_at`),
    KEY `ix_ticket_messages_in_reply_to` (`in_reply_to`),
    KEY `ix_ticket_messages_from_email` (`from_email`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS `ticket_message_attachments` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `ticket_message_id` BIGINT NOT NULL,
  `filename` VARCHAR(512),
  `content_type` VARCHAR(255),
  `size_bytes` BIGINT,
  `content_id` VARCHAR(512),
  `inline` TINYINT(1) NOT NULL DEFAULT 0,
  `storage_provider` VARCHAR(50) NOT NULL DEFAULT 'CLOUDINARY',
  `storage_public_id` VARCHAR(512),
  `storage_url` VARCHAR(1000),
  `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

  CONSTRAINT `fk_tma_msg`
    FOREIGN KEY (`ticket_message_id`) REFERENCES `ticket_messages`(`id`)
    ON DELETE CASCADE,

  KEY `ix_tma_msg` (`ticket_message_id`),
  KEY `ix_tma_cid` (`content_id`),
  KEY `ix_tma_inline` (`inline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(20) NULL AFTER google_id,
    ADD COLUMN password_set TINYINT(1) DEFAULT 0 AFTER auth_provider;

ALTER TABLE users
    ADD COLUMN password_token_hash VARCHAR(64) NULL AFTER activation_token,
    ADD COLUMN password_token_purpose ENUM('SET','RESET') NULL AFTER password_token_hash,
    ADD COLUMN password_token_expires_at DATETIME(3) NULL AFTER password_token_purpose;

ALTER TABLE users
    ADD COLUMN refresh_token_version INT NOT NULL DEFAULT 0 AFTER password_set;

CREATE INDEX idx_users_pwdtoken_purpose_hash
    ON users (password_token_purpose, password_token_hash);


drop table follows;
 CREATE TABLE `follows` (
   `follow_id` INT NOT NULL AUTO_INCREMENT,
   `user_id` INT NOT NULL,
   `company_id` INT NOT NULL,
   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (`follow_id`),
   KEY `fk_follows_user` (`user_id`),
   KEY `fk_follows_company` (`company_id`),
   CONSTRAINT `fk_follows_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
   CONSTRAINT `fk_follows_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`company_id`) ON DELETE RESTRICT ON UPDATE CASCADE
 );


CREATE TABLE IF NOT EXISTS user_sessions (
  session_id              int PRIMARY KEY AUTO_INCREMENT,
  user_id                 int NOT NULL,
  session_family_id       CHAR(36) NOT NULL,
  parent_session_id       int NULL,
  replaced_by_session_id  int NULL,
  refresh_token_hash      CHAR(64) NOT NULL,
  refresh_expires_at      DATETIME(3) NOT NULL,
  created_at              DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  last_seen_at            DATETIME(3) NULL,
  revoked_at              DATETIME(3) NULL,
  reuse_detected_at       DATETIME(3) NULL,
  ip_address              VARCHAR(45) NULL,
  user_agent              VARCHAR(255) NULL,
  device_label            VARCHAR(100) NULL,

  CONSTRAINT fk_sessions_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_sessions_parent
    FOREIGN KEY (parent_session_id) REFERENCES user_sessions(session_id)
    ON DELETE SET NULL,
  CONSTRAINT fk_sessions_replaced_by
    FOREIGN KEY (replaced_by_session_id) REFERENCES user_sessions(session_id)
    ON DELETE SET NULL,

  UNIQUE KEY ux_refresh_hash (refresh_token_hash),
  KEY idx_user (user_id),
  KEY idx_family (session_family_id),
  KEY idx_expires (refresh_expires_at),
  KEY idx_revoked (revoked_at)
);

CREATE TABLE IF NOT EXISTS user_one_time_tokens (
  token_id      int PRIMARY KEY AUTO_INCREMENT,
  user_id       int NOT NULL,
  purpose       ENUM('ACTIVATION','SET_PASSWORD','RESET_PASSWORD','EMAIL_CHANGE') NOT NULL,
  token_hash    CHAR(64) NOT NULL,
  expires_at    DATETIME(3) NOT NULL,
  consumed_at   DATETIME(3) NULL,
  created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

  CONSTRAINT fk_ott_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE,

  UNIQUE KEY ux_purpose_hash (purpose, token_hash),
  KEY idx_user_purpose (user_id, purpose),
  KEY idx_expires (expires_at)
);

ALTER TABLE users
DROP COLUMN activation_token,
DROP COLUMN password_token_hash,
DROP COLUMN password_token_purpose,
DROP COLUMN password_token_expires_at,
DROP COLUMN refresh_token_version,
DROP COLUMN activation_token_expires_at;

ALTER TABLE users
    ADD COLUMN updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  ADD COLUMN last_login_at DATETIME(3) NULL,
  ADD COLUMN password_changed_at DATETIME(3) NULL;

 CREATE TABLE notifications (
     notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id INT NOT NULL,
     type VARCHAR(50) NOT NULL,                -- "APPLICATION_STATUS", "NEW_JOB", ...
     title VARCHAR(200) NOT NULL,
     message TEXT NOT NULL,
     link VARCHAR(255) DEFAULT NULL,           -- có thể NULL nếu bạn chưa cần
     company_id BIGINT DEFAULT NULL,
     job_id BIGINT DEFAULT NULL,
     application_id BIGINT DEFAULT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     read_at TIMESTAMP NULL DEFAULT NULL,

     CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
     INDEX idx_user_created (user_id, created_at DESC),
     INDEX idx_company (company_id),
     INDEX idx_job (job_id)
 );

-- calendar_events interviews table
CREATE TABLE IF NOT EXISTS interviews (
  interview_id      INT AUTO_INCREMENT PRIMARY KEY,
  job_id            INT NOT NULL,
  company_id        INT NOT NULL,              -- recruiter: công ty tạo lịch
  candidate_id      INT NOT NULL,              -- user ứng viên
  scheduled_at      DATETIME NOT NULL,
  duration_minutes  INT NOT NULL DEFAULT 60,
  status            ENUM('PENDING','ACCEPTED','DECLINED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
  meeting_url       VARCHAR(500) NULL,
  gcal_event_id     VARCHAR(128) NULL,
  reminder_sent     TINYINT(1) NOT NULL DEFAULT 0,
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- FK khớp cấu trúc hiện tại
  CONSTRAINT fk_interviews_job
    FOREIGN KEY (job_id) REFERENCES jobs(job_id)
    ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT fk_interviews_company
    FOREIGN KEY (company_id) REFERENCES companies(company_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT fk_interviews_candidate
    FOREIGN KEY (candidate_id) REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  -- Index phục vụ truy vấn phổ biến
  INDEX idx_interviews_scheduled_at (scheduled_at),
  INDEX idx_interviews_company      (company_id),
  INDEX idx_interviews_candidate    (candidate_id),
  INDEX idx_interviews_job          (job_id),

  -- Tránh tạo trùng event nếu cần
  UNIQUE KEY uq_interviews_gcal_event (gcal_event_id)
);

ALTER TABLE interviews
  ADD COLUMN meeting_room VARCHAR(255) NULL;