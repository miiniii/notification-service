package com.mh.notification.infrastructure.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationPartitionSchedulerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private NotificationPartitionScheduler notificationPartitionScheduler;

    @BeforeEach
    void setUp() {
        notificationPartitionScheduler = new NotificationPartitionScheduler(jdbcTemplate);
    }

    @Test
    void addNextDayPartition_whenPartitionAlreadyExists_thenDoNotExecuteAlterTable() {
        // given
        given(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .willReturn(1);

        // when
        notificationPartitionScheduler.addNextDayPartition();

        // then
        verify(jdbcTemplate, times(1))
                .queryForObject(anyString(), eq(Integer.class), anyString());
        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    void addNextDayPartition_whenPartitionDoesNotExist_thenExecuteAlterTable() {
        // given
        given(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .willReturn(0);

        // when
        notificationPartitionScheduler.addNextDayPartition();

        // then
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate, times(1))
                .queryForObject(anyString(), eq(Integer.class), anyString());
        verify(jdbcTemplate, times(1)).execute(sqlCaptor.capture());

        String executedSql = sqlCaptor.getValue();
        assertThat(executedSql).contains("ALTER TABLE notifications");
        assertThat(executedSql).contains("REORGANIZE PARTITION pmax");
    }


}
