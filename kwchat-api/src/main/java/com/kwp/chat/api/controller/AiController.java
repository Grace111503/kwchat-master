package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.dto.AiResponse;
import com.kwp.chat.model.dto.SummaryRequest;
import com.kwp.chat.model.dto.TranslateRequest;
import com.kwp.chat.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI控制器
 */
@Tag(name = "AI功能", description = "智能摘要、消息翻译等AI功能")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "生成聊天记录摘要")
    @PostMapping("/summary")
    public Result<AiResponse> generateSummary(@Valid @RequestBody SummaryRequest request) {
        AiResponse response = aiService.generateSummary(
                request.getConversationId(),
                request.getMessageLimit(),
                request.getSummaryType()
        );
        return Result.success(response);
    }

    @Operation(summary = "翻译文本")
    @PostMapping("/translate")
    public Result<AiResponse> translate(@Valid @RequestBody TranslateRequest request) {
        AiResponse response = aiService.translate(
                request.getText(),
                request.getSourceLanguage(),
                request.getTargetLanguage()
        );
        return Result.success(response);
    }

    @Operation(summary = "翻译消息")
    @PostMapping("/translate/message")
    public Result<AiResponse> translateMessage(@RequestParam Long messageId,
                                               @RequestParam(defaultValue = "en") String targetLanguage) {
        AiResponse response = aiService.translateMessage(messageId, targetLanguage);
        return Result.success(response);
    }

    @Operation(summary = "获取智能回复建议")
    @GetMapping("/suggest-replies")
    public Result<List<String>> suggestReplies(@RequestParam Long conversationId) {
        List<String> suggestions = aiService.suggestReplies(conversationId);
        return Result.success(suggestions);
    }
}