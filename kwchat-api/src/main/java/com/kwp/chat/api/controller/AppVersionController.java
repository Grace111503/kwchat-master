package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用版本控制器
 * 提供热更新版本信息查询接口
 */
@Tag(name = "应用版本", description = "热更新版本信息查询")
@RestController
@RequestMapping("/app")
public class AppVersionController {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.update.url:http://118.25.44.250:8080/updates/app-v1.0.1.zip}")
    private String updateUrl;

    @Value("${app.update.notes:初始版本}")
    private String updateNotes;

    @Value("${app.update.forceUpdate:false}")
    private boolean forceUpdate;

    @Operation(summary = "获取应用版本信息")
    @GetMapping("/version")
    public Result<Map<String, Object>> getVersion() {
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("version", appVersion);
        versionInfo.put("url", updateUrl);
        versionInfo.put("notes", updateNotes);
        versionInfo.put("forceUpdate", forceUpdate);
        return Result.success(versionInfo);
    }
}
