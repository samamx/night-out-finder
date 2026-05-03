package com.nightout.nlp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NlpService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NlpService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }                                        // ← closes constructor

    public String extractKeyword(String userInput) {
        try {
            String prompt = String.format("""
                You are helping find events in London.
                Extract the most relevant single search keyword from this user input.
                Return ONLY a JSON object with no extra text, like this:
                {"keyword": "jazz"}
                User input: "%s"
                """, userInput);

            String requestBody = String.format("""
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [
                        {
                            "role": "user",
                            "content": "%s"
                        }
                    ],
                    "max_tokens": 50,
                    "temperature": 0.3
                }
                """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("OpenAI error: HTTP " + response.statusCode());
                return fallbackExtract(userInput);
            }

            return parseKeyword(response.body(), userInput);

        } catch (Exception e) {
            System.err.println("OpenAI failed: " + e.getMessage());
            return fallbackExtract(userInput);
        }
    }

    private String parseKeyword(String responseBody, String originalInput) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices")
                                .get(0)
                                .path("message")
                                .path("content")
                                .asText();

            // Parse the JSON response from OpenAI e.g. {"keyword": "jazz"}
            JsonNode keywordNode = objectMapper.readTree(content.trim());
            String keyword = keywordNode.path("keyword").asText("");

            if (keyword.isBlank()) {
                System.err.println("OpenAI returned empty keyword, using fallback");
                return fallbackExtract(originalInput);
            }

            System.out.println("🤖 OpenAI extracted keyword: \"" + keyword + "\"");
            return keyword;

        } catch (Exception e) {
            System.err.println("Failed to parse OpenAI response: " + e.getMessage());
            return fallbackExtract(originalInput);
        }
    }

    private String fallbackExtract(String userInput) {
        System.out.println("Using fallback keyword extraction");
        String[] fillerWords = {"i", "want", "to", "see", "go", "a", "an", "the",
                            "some", "feel", "like", "fancy", "looking", "for"};
        String[] words = userInput.toLowerCase().trim().split("\\s+");
        for (String word : words) {
            boolean isFiller = false;
            for (String filler : fillerWords) {
                if (word.equals(filler)) { isFiller = true; break; }
            }
            if (!isFiller && word.length() > 2) return word;
        }
        return userInput.trim();
    }


}