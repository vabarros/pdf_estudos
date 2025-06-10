package com.quiz.generator.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quiz.generator.backend.model.QuizQuestion;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;   // Adicionado para HttpEntity
import org.springframework.http.HttpHeaders;  // Adicionado para HttpHeaders
import org.springframework.http.MediaType;    // Adicionado para MediaType
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException; // Adicionado para tratar exceções de parsing
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties; // Manter se precisar do CoreNLP para outras funções

@Service
public class QuestionGenerationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // ALTERAÇÃO: Nova variável para a chave da API OpenAI
    @Value("${openai.api.key}")
    private String openAiApiKey;

    // ALTERAÇÃO: URL fixa para a API de chat completions da OpenAI
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @PostConstruct
    public void init() {
        System.out.println("QuestionGenerationService inicializado. Pronto para usar OpenAI API.");
    }

    public List<QuizQuestion> generateQuestions(String textContent, int count) {
        if (textContent == null || textContent.trim().isEmpty()) {
            System.err.println("Conteúdo do PDF vazio. Não é possível gerar perguntas.");
            return Collections.emptyList();
        }

        try {
            // 1. Constrói o corpo da requisição JSON para a API do ChatGPT
            ObjectNode requestBody = buildOpenAIChatRequestBody(textContent, count);

            // 2. Constrói os cabeçalhos da requisição (incluindo autenticação)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // ALTERAÇÃO: Autenticação via Bearer Token para OpenAI
            headers.setBearerAuth(openAiApiKey);

            // Cria a entidade HTTP com o corpo e os cabeçalhos
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            // 3. Faz a Chamada para a API do ChatGPT
            // ALTERAÇÃO: Usando a URL da OpenAI e o método postForObject com HttpEntity
            JsonNode openAIResponse = restTemplate.postForObject(OPENAI_API_URL, requestEntity, JsonNode.class);

            // 4. Parseia a Resposta do ChatGPT
            List<QuizQuestion> questions = parseOpenAIResponse(openAIResponse);

            // Retorna apenas o número solicitado de perguntas, se houver mais
            if (questions.size() > count) {
                return questions.subList(0, count);
            }
            return questions;

        } catch (Exception e) {
            System.err.println("Erro ao chamar a API do OpenAI: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // NOVO MÉTODO: Constrói o corpo da requisição JSON para a API de chat completions da OpenAI
    private ObjectNode buildOpenAIChatRequestBody(String textContent, int count) {
        ObjectNode requestBodyJson = objectMapper.createObjectNode();
        // ALTERAÇÃO: Modelo do ChatGPT
        requestBodyJson.put("model", "gpt-3.5-turbo"); // Ou "gpt-4", se você tiver acesso e preferir

        ArrayNode messagesArray = requestBodyJson.putArray("messages");

        // Mensagem do sistema: Define o comportamento do AI e as instruções de formatação
        ObjectNode systemMessage = messagesArray.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                "Você é um gerador de perguntas de múltipla escolha. " +
                        "Sua única saída deve ser um array JSON de objetos, onde cada objeto tem as chaves 'pergunta' (string), 'correta' (string) e 'alternativas' (array de strings). " +
                        "Não inclua nenhum texto adicional, explicações ou formatação além do JSON. " +
                        "Certifique-se de que o JSON seja válido e esteja pronto para ser parseado diretamente. " +
                        "Não use markdown blocks (```json)."); // Importante para evitar ````json` na resposta

        // Mensagem do usuário: Contém o conteúdo do texto e a instrução específica da tarefa
        ObjectNode userMsg = messagesArray.addObject();
        userMsg.put("role", "user");
        userMsg.put("content",
                String.format(
                        "Com base no texto a seguir, gere %d perguntas de múltipla escolha. " +
                                "Cada pergunta deve ter uma pergunta clara, uma resposta correta e 3 distratores (opções incorretas). " +
                                "As alternativas devem incluir a resposta correta e 3 incorretas, totalizando 4 alternativas. " +
                                "\n\nTexto:\n\"\"\"%s\"\"\"",
                        count, textContent
                ));

        // Configurações de geração (opcional, mas recomendado para controle)
        requestBodyJson.put("temperature", 0.7); // Controla a criatividade/aleatoriedade (0.0 a 2.0)
        requestBodyJson.put("max_tokens", 800); // Limita o tamanho da resposta gerada em tokens
        // Outros parâmetros como top_p, frequency_penalty, presence_penalty podem ser adicionados aqui.

        return requestBodyJson;
    }

    // NOVO MÉTODO: Parseia a resposta JSON da API de chat completions da OpenAI
    private List<QuizQuestion> parseOpenAIResponse(JsonNode openAIResponse) {
        List<QuizQuestion> questions = new ArrayList<>();
        try {
            // Caminho para o conteúdo da resposta do ChatGPT: choices[0].message.content
            JsonNode candidateTextNode = openAIResponse
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (candidateTextNode.isMissingNode() || candidateTextNode.isNull()) {
                System.err.println("Resposta do OpenAI não contém texto de candidato válido.");
                System.err.println("Resposta completa do OpenAI: " + openAIResponse.toPrettyString());
                return Collections.emptyList();
            }

            String jsonString = candidateTextNode.asText();

            // Devido à instrução no prompt do sistema, o ChatGPT *não deve* envolver o JSON em '```json\n...\n```'.
            // No entanto, como uma medida de segurança, você PODE manter a remoção se ainda tiver problemas ocasionais.
            // jsonString = jsonString.replaceAll("```json\n", "").replaceAll("\n```", "");

            // O que é CRUCIAL é que o JSON retornado esteja *diretamente* no formato de Array JSON.
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
        } catch (IOException e) { // Mudança para IOException para parsing específico
            System.err.println("Erro de IO ao parsear a resposta JSON do OpenAI: " + e.getMessage());
            System.err.println("Resposta recebida (problema no parsing): " + openAIResponse.toPrettyString());
            e.printStackTrace();
        } catch (Exception e) { // Para outras exceções não IO
            System.err.println("Erro inesperado ao parsear a resposta JSON do OpenAI: " + e.getMessage());
            System.err.println("Resposta recebida (problema no parsing): " + openAIResponse.toPrettyString());
            e.printStackTrace();
        }
        return questions;
    }

    // Você pode manter o método generateQuestionsWithCoreNLP como um fallback ou para testes
    // private List<QuizQuestion> generateQuestionsWithCoreNLP(String textContent, int count) { /* ... código anterior ... */ }

}