package com.mh.notification.infrastructure.persistence;

import com.mh.notification.application.dto.ArchiveNotificationRow;
import com.mh.notification.application.port.ArchiveNotificationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArchiveNotificationRepositoryImpl implements ArchiveNotificationRepository {

    private final JdbcTemplate archiveJdbcTemplate;

    public ArchiveNotificationRepositoryImpl(
            @Qualifier("archiveJdbcTemplate") JdbcTemplate archiveJdbcTemplate
    ) {
        this.archiveJdbcTemplate = archiveJdbcTemplate;
    }

    @Override
    public void saveAll(List<ArchiveNotificationRow> rows) {
        String sql = """
                INSERT IGNORE INTO notifications_archive (
                    id, requester_id, user_id, request_id, service,
                    title, body, target_url, is_read, channel, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        archiveJdbcTemplate.batchUpdate(
                sql,
                rows,
                rows.size(),
                (ps, row) -> {
                    ps.setLong(1, row.id());
                    ps.setLong(2, row.requesterId());
                    ps.setLong(3, row.userId());
                    ps.setString(4, row.requestId());
                    ps.setString(5, row.service());
                    ps.setString(6, row.title());
                    ps.setString(7, row.body());
                    ps.setString(8, row.targetUrl());
                    ps.setBoolean(9, row.isRead());
                    ps.setString(10, String.valueOf(row.channel()));
                    ps.setObject(11, row.createdAt());
                }
        );
    }
}
