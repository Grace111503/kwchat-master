package com.kwp.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 快伟通 - 企业级即时通讯平台
 */
@SpringBootApplication(exclude = {
    org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@MapperScan("com.kwp.chat.dao")
@EnableAsync
@EnableScheduling
public class KuaiTongApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuaiTongApplication.class, args);
        System.out.println("========================================");
        System.out.println("   快伟通启动成功！");
        System.out.println("   API文档: http://localhost:8080/api/doc.html");
        System.out.println("========================================");
    }
}
