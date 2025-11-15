package com.example.junction2025.utils.llms;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI GPT-5 호출을 전담하는 에이전트.
 * <p>
 * application.yml 의 {@code api.key.openai} 값을 사용해 OpenAI API 와 통신한다.
 */

@Slf4j
@Component
public class OpenAILLMAgent {
    private static final String CHAT_COMPLETION_ENDPOINT = "/chat/completions";
    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant.";

    private final RestClient restClient;
    private final String openAIApiKey;

    public OpenAILLMAgent(
            RestClient.Builder restClientBuilder,
            @Value("${api.key.openai}") String openAIApiKey,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.openAIApiKey = openAIApiKey;
    }

    /**
     * 사용자 프롬프트를 GPT-5 에 전달하고, 첫 번째 응답 메시지를 반환한다.
     *
     * @param userPrompt 사용자 입력
     * @return LLM 응답 텍스트
     */
    public String getCompletion(String userPrompt) {
        ChatCompletionRequest requestPayload = new ChatCompletionRequest(
                "gpt-5",
                List.of(
                        new ChatMessage("system", DEFAULT_SYSTEM_PROMPT),
                        new ChatMessage("user", userPrompt)
                ),
                1,
                2048
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIApiKey);
        System.out.println("OpenAI API Key: " + openAIApiKey);
        try {
            ChatCompletionResponse response = restClient.post()
                    .uri(CHAT_COMPLETION_ENDPOINT)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(requestPayload)
                    .retrieve()
                    .body(ChatCompletionResponse.class);
            System.out.println("response: " + response);
            return Optional.ofNullable(response)
                    .flatMap(body -> body.choices().stream().findFirst())
                    .map(choice -> choice.message().content())
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> new IllegalStateException("OpenAI 로부터 유효한 응답을 받지 못했습니다."));
        } catch (RestClientResponseException e) {
            log.error("OpenAI API 호출 실패 - status: {}, body: {}", e.getStatusCode().value(), e.getResponseBodyAsString(), e);
            throw new IllegalStateException("OpenAI API 호출 중 오류가 발생했습니다.", e);
        } catch (RestClientException e) {
            log.error("OpenAI API 통신 오류", e);
            throw new IllegalStateException("OpenAI API 통신 중 오류가 발생했습니다.", e);
        }
    }

    private record ChatCompletionRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            int max_completion_tokens
    ) {
    }

    private record ChatMessage(
            String role,
            String content
    ) {
    }

    @SuppressWarnings("unused")
    private record ChatCompletionResponse(
            List<Choice> choices
    ) {
    }

    @SuppressWarnings("unused")
    private record Choice(
            int index,
            ChatMessage message,
            String finish_reason
    ) {
    }
}

