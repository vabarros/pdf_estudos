// backend/src/main/java/com/quiz/generator/backend/model/UserSession.java
package com.quiz.generator.backend.model;

import java.time.LocalDateTime;
import java.util.List;

public class UserSession {
    private String id;
    private String pdfContent;
    private LocalDateTime createdAt;
    private List<QuizQuestion> generatedQuestions;

    // Construtor vazio (geralmente necessário para frameworks)
    public UserSession() {
    }

    // Construtor com todos os campos
    public UserSession(String id, String pdfContent, LocalDateTime createdAt, List<QuizQuestion> generatedQuestions) {
        this.id = id;
        this.pdfContent = pdfContent;
        this.createdAt = createdAt;
        this.generatedQuestions = generatedQuestions;
    }

    // --- GETTERS ---
    public String getId() {
        return id;
    }

    public String getPdfContent() {
        return pdfContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ESTE É O MÉTODO QUE ESTÁ FALTANDO OU NÃO ESTÁ SENDO RECONHECIDO
    public List<QuizQuestion> getGeneratedQuestions() {
        return generatedQuestions;
    }

    // --- SETTERS (Opcional, mas geralmente boa prática) ---
    public void setId(String id) {
        this.id = id;
    }

    public void setPdfContent(String pdfContent) {
        this.pdfContent = pdfContent;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setGeneratedQuestions(List<QuizQuestion> generatedQuestions) {
        this.generatedQuestions = generatedQuestions;
    }

    // Opcional: toString, equals, hashCode se você não usa Lombok
    @Override
    public String toString() {
        return "UserSession{" +
                "id='" + id + '\'' +
                ", pdfContent='" + pdfContent + '\'' +
                ", createdAt=" + createdAt +
                ", generatedQuestions=" + generatedQuestions +
                '}';
    }
}