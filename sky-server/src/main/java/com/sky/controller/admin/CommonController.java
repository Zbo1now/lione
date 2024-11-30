package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传: {}", file);
        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取文件类型 例如:.png
            String fileName = originalFilename.substring(originalFilename.lastIndexOf("."));
            //生成新名称(放置存储在oss里文件名字重复,导致覆盖)
            String objectName = UUID.randomUUID() + fileName;
            //上传文件
            String fillPath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(fillPath);
        } catch (IOException e) {
            log.error("文件上传失败:{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
