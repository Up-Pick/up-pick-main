package org.oneog.uppick.auction.domain.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileManager {

	String store(MultipartFile file);

}
