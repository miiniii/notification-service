CREATE DATABASE notification_archive_db
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE notification_archive_db;

CREATE TABLE notifications_archive (
                                       is_read bit(1) NOT NULL,
                                       created_at datetime(6) NOT NULL,
                                       id bigint NOT NULL,
                                       requester_id bigint NOT NULL,
                                       user_id bigint NOT NULL,
                                       request_id varchar(50) NOT NULL,
                                       service varchar(50) NOT NULL,
                                       title varchar(200) NOT NULL,
                                       target_url varchar(500) NOT NULL,
                                       body varchar(1000) NOT NULL,
                                       channel varchar(30) NOT NULL,
                                       PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

SHOW CREATE TABLE notifications_archive;