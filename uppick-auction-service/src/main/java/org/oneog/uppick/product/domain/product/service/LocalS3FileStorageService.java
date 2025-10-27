package org.oneog.uppick.product.domain.product.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Profile("!dev & !prod")
@Service
public class LocalS3FileStorageService implements S3FileManager {

	@Override
	public String store(MultipartFile file) {

		// 랜덤 문자열의 파일 이름 리턴
		return "local-" + java.util.UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
	}

}
