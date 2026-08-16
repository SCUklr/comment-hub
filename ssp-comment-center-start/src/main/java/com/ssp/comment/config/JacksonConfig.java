package com.ssp.comment.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 序列化配置：将 Long 类型序列化为字符串。
 *
 * <p>雪花算法生成的 ID（如 744087244154953728）超出 JS Number 安全整数范围
 * （Number.MAX_SAFE_INTEGER = 2^53 - 1 ≈ 9e15），前端 JSON.parse 会丢失精度，
 * 导致回传的 ID 查不到数据（表现为"评论不存在/回复不存在"）。
 * 将 boxed Long 统一序列化为字符串可彻底避免该问题。
 *
 * <p>注意：仅配置 {@link Long}（包装类型，即所有 ID 字段），
 * 不配置 long（原始类型，如 PageResult.total 计数），保证分页 total 仍为数字。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance);
    }
}
