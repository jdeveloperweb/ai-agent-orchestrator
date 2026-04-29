package com.jdeveloperweb.aiagent.controller;

import com.jdeveloperweb.aiagent.dto.*;
import com.jdeveloperweb.aiagent.service.AgentOrchestratorService;
import com.jdeveloperweb.aiagent.service.SessionStoreService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentOrchestratorService orchestratorService;
    private final SessionStoreService sessionStore;
    private final com.jdeveloperweb.aiagent.repository.AgentSessionRepository sessionRepository;
    private final com.jdeveloperweb.aiagent.repository.AgentLogRepository logRepository;

    public AgentController(AgentOrchestratorService orchestratorService, 
                           SessionStoreService sessionStore,
                           com.jdeveloperweb.aiagent.repository.AgentSessionRepository sessionRepository,
                           com.jdeveloperweb.aiagent.repository.AgentLogRepository logRepository) {
        this.orchestratorService = orchestratorService;
        this.sessionStore = sessionStore;
        this.sessionRepository = sessionRepository;
        this.logRepository = logRepository;
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getAllSessions() {
        return ResponseEntity.ok(sessionRepository.findAllByOrderByCreatedAtDesc());
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        orchestratorService.deleteSession(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/{id}/logs")
    public ResponseEntity<?> getSessionLogs(@PathVariable Long id) {
        return ResponseEntity.ok(orchestratorService.getLogsForSession(id));
    }


    @PostMapping("/full-cycle")
    public FullCycleResponse fullCycle(@RequestBody FullCycleRequest request) {
        return orchestratorService.runFullCycle(request, null);
    }

    @GetMapping(value = "/full-cycle-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter fullCycleStream(
            @RequestParam String objective,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String contextQuestion,
            @RequestParam(defaultValue = "Java Spring Boot") String targetStack,
            @RequestParam(defaultValue = "true") boolean saveFiles,
            @RequestParam(defaultValue = "false") boolean specOnly) {

        SseEmitter emitter = new SseEmitter(600_000L); // 10 min

        FullCycleRequest request = new FullCycleRequest(objective, contextQuestion, targetStack, saveFiles);
        request.setSessionId(sessionId);
        request.setSpecOnly(specOnly);

        new Thread(() -> {
            try {
                orchestratorService.runFullCycle(request, emitter);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
