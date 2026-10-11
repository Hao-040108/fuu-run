package com.karrecy.common.jackson;

import cn.hutool.core.util.ObjectUtil;
import com.karrecy.common.annotation.Sensitive;
import com.karrecy.common.core.service.SensitiveService;
import com.karrecy.common.enums.SensitiveStrategy;
import com.karrecy.common.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Objects;

/**
 * 数据脱敏json序列化工具
 * <p>
 * Spring Boot 4 / Jackson 3：JsonSerializer 更名为 ValueSerializer，
 * ContextualSerializer 接口取消，上下文处理合并为 ValueSerializer#createContextual。
 * </p>
 */
@Slf4j
public class SensitiveJsonSerializer extends ValueSerializer<String> {

    private SensitiveStrategy strategy;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) {
        try {
            SensitiveService sensitiveService = SpringUtils.getBean(SensitiveService.class);
            if (ObjectUtil.isNotNull(sensitiveService) && sensitiveService.isSensitive()) {
                gen.writeString(strategy.desensitizer().apply(value));
            } else {
                gen.writeString(value);
            }
        } catch (BeansException e) {
            log.error("脱敏实现不存在, 采用默认处理 => {}", e.getMessage());
            gen.writeString(value);
        }
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            this.strategy = annotation.strategy();
            return this;
        }
        return ctxt.findValueSerializer(property.getType());
    }
}
