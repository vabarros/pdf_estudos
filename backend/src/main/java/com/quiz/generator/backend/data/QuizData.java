// backend/src/main/java/com/quiz/generator/backend/data/QuizData.java
package com.quiz.generator.backend.data;

import com.quiz.generator.backend.model.QuizQuestion; // Importe sua classe QuizQuestion
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map; // Para o mapa de capítulos

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizData {
    // Mapeia os capítulos (String) para uma lista de QuizQuestion
    private Map<String, List<QuizQuestion>> quizChapters;
}