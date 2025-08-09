CREATE DATABASE IF NOT EXISTS job_huntly_local
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

use job_huntly_local;

DROP TABLE IF EXISTS user_type;
CREATE TABLE user_type (
                           id CHAR(2) PRIMARY KEY,
                           name VARCHAR(50) NOT NULL
);

INSERT INTO user_type (id, name) VALUES
                                     ('CA', 'Candidate'),
                                     ('RE', 'Recruiter'),
                                     ('AD', 'Admin' );

DROP TABLE IF EXISTS user;
CREATE TABLE user (
                      id INT AUTO_INCREMENT PRIMARY KEY,

                      user_type_id CHAR(2) NOT NULL,     -- 'CA' cho Candidate, 'RE' cho Recruiter
                      email VARCHAR(100) NOT NULL UNIQUE,
                      password_hash VARCHAR(255),        -- NULL nếu đăng nhập bằng Google
                      google_id VARCHAR(100) UNIQUE,     -- ID từ Google OAuth2 ("sub")

                      full_name VARCHAR(100),
                      date_of_birth DATE,
                      gender ENUM('Male', 'Female', 'Other'),
                      contact_number VARCHAR(20),
                      user_image_url VARCHAR(255),       -- Avatar URL

                      registration_date DATETIME DEFAULT CURRENT_TIMESTAMP(),

                      is_active TINYINT(1) DEFAULT 0,
                      sms_notification_active TINYINT(1) DEFAULT 0,
                      email_notification_active TINYINT(1) DEFAULT 0,

                      FOREIGN KEY (user_type_id) REFERENCES user_type(id)
                          ON DELETE RESTRICT ON UPDATE CASCADE
);
