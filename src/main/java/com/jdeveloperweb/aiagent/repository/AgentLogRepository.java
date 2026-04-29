package com.jdeveloperweb.aiagent.repository;

import com.jdeveloperweb.aiagent.model.AgentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface AgentLogRepository extends JpaRepository<AgentLog, Long> {
    List<AgentLog> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AgentLog l WHERE l.sessionId = :sessionId")
    void deleteBySessionId(Long sessionId);
}