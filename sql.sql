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

SELECT
    PARTITION_NAME,
    PARTITION_DESCRIPTION
FROM INFORMATION_SCHEMA.PARTITIONS
WHERE TABLE_SCHEMA = 'notification_db'
  AND TABLE_NAME = 'notifications';

USE notification_db;

ALTER TABLE notifications
    REORGANIZE PARTITION pmax INTO (
        PARTITION p20260522 VALUES LESS THAN (TO_DAYS('2026-05-23')),
        PARTITION p20260523 VALUES LESS THAN (TO_DAYS('2026-05-24')),
        PARTITION p20260524 VALUES LESS THAN (TO_DAYS('2026-05-25')),
        PARTITION p20260525 VALUES LESS THAN (TO_DAYS('2026-05-26')),
        PARTITION p20260526 VALUES LESS THAN (TO_DAYS('2026-05-27')),
        PARTITION p20260527 VALUES LESS THAN (TO_DAYS('2026-05-28')),
        PARTITION p20260528 VALUES LESS THAN (TO_DAYS('2026-05-29')),
        PARTITION p20260529 VALUES LESS THAN (TO_DAYS('2026-05-30')),
        PARTITION pmax VALUES LESS THAN MAXVALUE
        );

UPDATE notifications
SET created_at = NOW(),
    requester_id = 1
WHERE DATE(created_at) = '2026-04-19'
LIMIT 1000;

SELECT requester_id, COUNT(*) AS count
FROM notifications
WHERE created_at >= NOW() - INTERVAL 7 DAY
GROUP BY requester_id
ORDER BY count DESC;