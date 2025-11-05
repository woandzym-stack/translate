package com.example.translate.thirdpart;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionModel;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@Slf4j
public class DeepSeekProcessor {
    // 共享的 DeepSeekClient 实例（线程安全，可复用）
    private final DeepSeekClient deepSeekClient = DeepSeekClient.builder()
            .baseUrl("https://api.deepseek.com")
            .openAiApiKey("sk-19478ad2177e4948b570be248b531571")
            .build();

    /**
     * 处理单个问题并返回答案字符串
     */
    public String processQuestion(String context, String question) {
        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(ChatCompletionModel.DEEPSEEK_CHAT)
                    .addSystemMessage(context)
                    .addUserMessage(question)
                    .temperature(0.8)
                    .build();
            // 使用复用的 DeepSeekClient 实例
            ChatCompletionResponse response = deepSeekClient
                    .chatCompletion(request)
                    .execute();

            return response.content();
        } catch (Exception e) {
            // 异常时返回默认提示，并可记录日志
            log.error("deepSeek调用出现异常", e);
            throw new RuntimeException("deepSeek调用出现异常", e);
        }
    }


}
