package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.product.service.FileService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@SuppressWarnings("all")
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.bucketName}")
    private String bucketName;

    @Override
    public String fileUpload(MultipartFile file) {
        String url = "";
        try {
            // 创建一个MinioClient对象，用于连接MinIO服务器，并提供相应的访问凭证。
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(accessKey, secretKey)
                            .build();

            // 检查存储桶是否存在
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            // 如果不存在，则创建一个名为bucketName的存储桶
            if (!found) {
                // 创建一个名为bucketName的存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                // 如果存储桶已经存在，则忽略
                log.info("Bucket {} already exists", bucketName);
            }
            // 这种是流式上传，可以直接上传MultipartFile对象
            LocalDate nowDate = LocalDate.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("/yyyy/MM/dd/");
            String nowDateFormat = nowDate.format(dateTimeFormatter);
            // 这里用当前时间戳和UUID生成一个唯一的文件名
            String fileName = new StringBuilder(nowDateFormat).append(System.currentTimeMillis()).append(UUID.randomUUID().toString().replace("-", "")).toString();
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            url = new StringBuilder(endpoint).append("/").append(bucketName).append("/").append(fileName).toString();
            log.info("文件上传成功，url={}", url);
        } catch (Exception e) {
            throw new GmallException("文件上传失败，请联系管理员！");
        }
        return url;
    }
}
