package com.samplemcp.mcpsample.config;

import com.samplemcp.mcpsample.mcp.Customer360Tools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Customer360Tools} icindeki {@code @Tool} metotlarini MCP server'a
 * kaydeden konfigurasyon. Spring AI MCP starter, bulunan
 * {@link ToolCallbackProvider} bean'lerini otomatik olarak yayinlar.
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider customer360ToolCallbackProvider(Customer360Tools customer360Tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(customer360Tools)
                .build();
    }
}
