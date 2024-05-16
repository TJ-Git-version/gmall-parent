package com.atguigu.gmall.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String fileUpload(MultipartFile file);

}
