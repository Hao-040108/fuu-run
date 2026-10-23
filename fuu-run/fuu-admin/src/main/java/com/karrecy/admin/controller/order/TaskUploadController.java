package com.karrecy.admin.controller.order;

import com.karrecy.common.config.FuuConfig;
import com.karrecy.common.core.controller.BaseController;
import com.karrecy.common.core.domain.R;
import com.karrecy.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 任务附件图片上传（本地存储，便于离线演示）
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class TaskUploadController extends BaseController {

    private static final List<String> ALLOWED_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    private final FuuConfig fuuConfig;

    @PostMapping("/upload/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ServiceException("图片大小不能超过 5MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new ServiceException("仅支持图片格式：" + String.join("/", ALLOWED_EXT));
        }
        String monthPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relative = "images/" + monthPath + "/" + fileName;
        try {
            Path root = Paths.get(fuuConfig.getLocalUploadDir()).toAbsolutePath().normalize();
            Path dest = root.resolve(relative).normalize();
            if (!dest.startsWith(root)) {
                throw new ServiceException("非法文件路径");
            }
            Files.createDirectories(dest.getParent());
            file.transferTo(dest.toFile());
        } catch (IOException e) {
            log.error("本地文件保存失败", e);
            throw new ServiceException("文件保存失败：" + e.getMessage());
        }
        String baseUrl = fuuConfig.getLocalUploadBaseUrl() == null ? "" : fuuConfig.getLocalUploadBaseUrl();
        String url = baseUrl.replaceAll("/+$", "") + "/uploads/" + relative;
        log.info("任务附件上传成功：{} -> {}", original, url);
        return R.ok("上传成功", url);
    }
}
