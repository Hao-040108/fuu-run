package com.karrecy.admin.config;

import com.karrecy.common.config.FuuConfig;
import com.karrecy.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地文件访问配置
 * <p>把 fuu.local-upload-dir 指定的目录映射到 HTTP 路径 /uploads/** 下，
 * 供管理端预览、小程序端直接加载图片。</p>
 */
@Configuration
@RequiredArgsConstructor
public class LocalUploadWebConfig implements WebMvcConfigurer {

    private final FuuConfig fuuConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = fuuConfig.getLocalUploadDir();
        if (StringUtils.isBlank(dir)) {
            return;
        }
        String location = dir.replace("\\", "/");
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + location);
    }
}
