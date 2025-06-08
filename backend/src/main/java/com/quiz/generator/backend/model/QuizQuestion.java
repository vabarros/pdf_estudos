package com.quiz.generator.backend.model;

import java.util.List;
import java.util.Objects;

public class QuizQuestion {
    private String pergunta;
    private List<String> alternativas;
    private String correta;

    // Construtor vazio (sem argumentos)
    public QuizQuestion() {
    }

    // CONSTRUTOR COM TODOS OS ARGUMENTOS (ESSENCIAL PARA O SEU ERRO)
    public QuizQuestion(String pergunta, List<String> alternativas, String correta) {
        this.pergunta = pergunta;
        this.alternativas = alternativas;
        this.correta = correta;
    }

    // Getters (se não estiver usando Lombok)
    public String getPergunta() {
        return pergunta;
    }

    public List<String> getAlternativas() {
        return alternativas;
    }

    public String getCorreta() {
        return correta;
    }

    // Setters (se não estiver usando Lombok)
    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas;
    }

    public void setCorreta(String correta) {
        this.correta = correta;
    }

    @Override
    public String toString() {
        return "QuizQuestion{" +
                "pergunta='" + pergunta + '\'' +
                ", alternativas=" + alternativas +
                ", correta='" + correta + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizQuestion that = (QuizQuestion) o;
        return Objects.equals(pergunta, that.pergunta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pergunta);
    }
}