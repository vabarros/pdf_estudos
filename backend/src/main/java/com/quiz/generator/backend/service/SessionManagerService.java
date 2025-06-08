// backend/src/main/java/com/quiz/generator/backend/service/SessionManagerService.java
package com.quiz.generator.backend.service;

import com.quiz.generator.backend.model.QuizQuestion;
import com.quiz.generator.backend.model.UserSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManagerService {

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public String createSession(String pdfContent) {
        String sessionId = UUID.randomUUID().toString();
        UserSession newSession = new UserSession(sessionId, pdfContent, LocalDateTime.now(), new ArrayList<>());
        sessions.put(sessionId, newSession);
        return sessionId;
    }

    public UserSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void addGeneratedQuestions(String sessionId, List<QuizQuestion> newQuestions) {
        UserSession session = sessions.get(sessionId);
        if (session != null) {
            session.getGeneratedQuestions().addAll(newQuestions);
        } else {
            System.err.println("Sessão " + sessionId + " não encontrada ao tentar adicionar questões.");
        }
    }

    // Este é o método que estava faltando ou não estava sendo reconhecido
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}