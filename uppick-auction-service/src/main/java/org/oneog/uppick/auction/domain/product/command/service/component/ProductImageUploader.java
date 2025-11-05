package org.oneog.uppick.auction.domain.product.command.service.component;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageUploader {

	String store(MultipartFile file);

}
