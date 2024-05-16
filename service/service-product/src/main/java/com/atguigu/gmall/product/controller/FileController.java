package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/fileUpload")
    public Result<String> fileUpload(MultipartFile file) {
        return Result.ok(fileService.fileUpload(file));
    }

}
