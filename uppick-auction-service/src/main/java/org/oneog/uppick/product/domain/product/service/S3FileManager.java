package org.oneog.uppick.product.domain.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileManager {

	String store(MultipartFile file);

}
