package com.mh.notification.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class NotificationPartitionScheduler {

    private final JdbcTemplate jdbcTemplate;

    public NotificationPartitionScheduler(
            @Qualifier("mainJdbcTemplate") JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void addNextDayPartition() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        String partitionName = "p" + targetDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        String lessThanDate = targetDate.plusDays(1).toString();

        if (partitionExists(partitionName)) {
            log.info("Partition already exists: {}", partitionName);
            return;
        }

        String sql = """
                ALTER TABLE notifications
                REORGANIZE PARTITION pmax INTO (
                  PARTITION %s VALUES LESS THAN (TO_DAYS('%s')),
                  PARTITION pmax VALUES LESS THAN MAXVALUE
                )
                """.formatted(partitionName, lessThanDate);
        jdbcTemplate.execute(sql);
        log.info("Partition created: {}", partitionName);
    }

    private boolean partitionExists(String partitionName) {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.PARTITIONS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'notifications'
                  AND PARTITION_NAME = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, partitionName);
        return count != null && count > 0;
    }
}
