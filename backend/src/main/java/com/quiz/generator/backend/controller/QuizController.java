package com.quiz.generator.backend.controller;

import com.quiz.generator.backend.model.QuizQuestion;
import com.quiz.generator.backend.service.PdfReaderService;
import com.quiz.generator.backend.service.QuestionGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizController {

    @Autowired
    private PdfReaderService pdfReaderService;

    @Autowired
    private QuestionGenerationService questionGenerationService;

    @PostMapping("/generate-quiz")
    public ResponseEntity<List<QuizQuestion>> generateQuiz(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "count", defaultValue = "5") int count,
            @RequestParam(value = "startPage", required = false) Integer startPage, // Novo parâmetro
            @RequestParam(value = "endPage", required = false) Integer endPage // Novo parâmetro
    ) {
        try {
            String textContent = pdfReaderService.readPdf(file, startPage, endPage); // Passa os parâmetros
            List<QuizQuestion> questions = questionGenerationService.generateQuestions(textContent, count);
            return ResponseEntity.ok(questions);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}