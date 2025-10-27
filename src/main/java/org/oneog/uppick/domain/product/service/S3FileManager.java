package org.oneog.uppick.domain.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileManager {
    String store(MultipartFile file);
}
