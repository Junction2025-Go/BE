package com.example.junction2025.utils.llms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LLMAgentTest {
    @Autowired
    OpenAILLMAgent llmAgent;

    @Test
    void realOpenAiCall() {
        String result = llmAgent.getCompletion("안녕, 오늘 헬싱키 날씨는 어때?");
        System.out.println(result);
    }
}

