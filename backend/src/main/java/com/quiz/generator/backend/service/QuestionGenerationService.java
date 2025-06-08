package com.quiz.generator.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quiz.generator.backend.model.QuizQuestion;
import edu.stanford.nlp.pipeline.StanfordCoreNLP; // Manter para futuras análises ou fallback
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.*;

@Service
public class QuestionGenerationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.gemini.api.key}")
    private String geminiApiKey;

    @Value("${google.gemini.api.url}")
    private String geminiApiUrl;

    // Manter o CoreNLP para possível uso futuro (ex: limpeza de texto, ou fallback)
    private StanfordCoreNLP coreNlpPipeline;

    @PostConstruct
    public void init() {
        // Inicialize o CoreNLP apenas se ainda for necessário para outras funções
        // ou como um fallback. Por enquanto, o foco será no Gemini.
        // Properties props = new Properties();
        // props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        // this.coreNlpPipeline = new StanfordCoreCoreNLP(props);
        System.out.println("QuestionGenerationService inicializado. Pronto para usar Gemini API.");
    }

    public List<QuizQuestion> generateQuestions(String textContent, int count) {
        if (textContent == null || textContent.trim().isEmpty()) {
            System.err.println("Conteúdo do PDF vazio. Não é possível gerar perguntas.");
            return Collections.emptyList();
        }

        // 1. Construa o Prompt para o Gemini
        String prompt = buildGeminiPrompt(textContent, count);

        // 2. Construa o Corpo da Requisição para a API do Gemini
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", prompt);
        ObjectNode content = objectMapper.createObjectNode();
        content.set("parts", objectMapper.createArrayNode().add(part));
        contents.add(content);
        requestBody.set("contents", contents);

        // Para Gemini 1.5 Pro, você pode querer adicionar generationConfig e safetySettings
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.7); // 0.0 a 1.0, controla a aleatoriedade
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 800); // Ajuste conforme a quantidade de texto esperada
        requestBody.set("generationConfig", generationConfig);


        // 3. Faça a Chamada para a API do Gemini
        try {
            String url = geminiApiUrl + geminiApiKey;
            JsonNode geminiResponse = restTemplate.postForObject(url, requestBody, JsonNode.class);

            // 4. Parseie a Resposta do Gemini
            List<QuizQuestion> questions = parseGeminiResponse(geminiResponse);

            // Retorne apenas o número solicitado de perguntas, se houver mais
            if (questions.size() > count) {
                return questions.subList(0, count);
            }
            return questions;

        } catch (Exception e) {
            System.err.println("Erro ao chamar a API do Google Gemini: " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro, você pode querer um fallback para o método antigo do CoreNLP
            // return generateQuestionsWithCoreNLP(textContent, count);
            return Collections.emptyList();
        }
    }

    private String buildGeminiPrompt(String textContent, int count) {
        // É CRUCIAL que o prompt seja bem escrito para obter a saída desejada!
        return String.format(
                "Com base no texto a seguir, gere %d perguntas de múltipla escolha. " +
                        "Cada pergunta deve ter uma pergunta clara, uma resposta correta e 3 distratores (opções incorretas). " +
                        "A saída deve ser estritamente um array JSON de objetos, onde cada objeto tem as chaves 'pergunta', 'correta', e 'alternativas' (um array de strings). " +
                        "Não inclua nenhum texto adicional antes ou depois do JSON. " +
                        "\n\nTexto:\n\"\"\"%s\"\"\"",
                count, textContent
        );
    }

    private List<QuizQuestion> parseGeminiResponse(JsonNode geminiResponse) {
        List<QuizQuestion> questions = new ArrayList<>();
        try {
            // Caminho para a resposta do Gemini pode variar dependendo do modelo e da versão da API
            // Para `gemini-pro:generateContent`, a resposta geralmente está em `candidates[0].content.parts[0].text`
            JsonNode candidateTextNode = geminiResponse
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (candidateTextNode.isMissingNode() || candidateTextNode.isNull()) {
                System.err.println("Resposta do Gemini não contém texto de candidato válido.");
                System.err.println("Resposta completa do Gemini: " + geminiResponse.toPrettyString());
                return Collections.emptyList();
            }

            String jsonString = candidateTextNode.asText();

            // O Gemini pode envolver o JSON em '```json\n...\n```'. Precisa remover.
            jsonString = jsonString.replaceAll("```json\n", "").replaceAll("\n```", "");

            ArrayNode jsonArray = (ArrayNode) objectMapper.readTree(jsonString);

            for (JsonNode node : jsonArray) {
                QuizQuestion question = new QuizQuestion();
                question.setPergunta(node.path("pergunta").asText());
                question.setCorreta(node.path("correta").asText());

                ArrayNode alternativesNode = (ArrayNode) node.path("alternativas");
                List<String> alternatives = new ArrayList<>();
                for (JsonNode altNode : alternativesNode) {
                    alternatives.add(altNode.asText());
                }
                question.setAlternativas(alternatives);
                questions.add(question);
            }
        } catch (Exception e) {
            System.err.println("Erro ao parsear a resposta JSON do Gemini: " + e.getMessage());
            System.err.println("Resposta recebida (problema no parsing): " + geminiResponse.toPrettyString());
            e.printStackTrace();
        }
        return questions;
    }

    // Você pode manter o método generateQuestionsWithCoreNLP como um fallback ou para testes
    // private List<QuizQuestion> generateQuestionsWithCoreNLP(String textContent, int count) { /* ... código anterior ... */ }

}