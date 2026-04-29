package com.jdeveloperweb.aiagent.repository;

import com.jdeveloperweb.aiagent.model.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSession, Long> {
    List<AgentSession> findAllByOrderByCreatedAtDesc();
}
