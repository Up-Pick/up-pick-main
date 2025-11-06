package org.oneog.uppick.auction.domain.product.command.service.component;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Profile("!dev & !prod")
@Service
@Qualifier("localProductImageUploader")
public class LocalProductImageUploader implements ProductImageUploader {

	@Override
	public String store(MultipartFile file) {

		// 랜덤 문자열의 파일 이름 리턴
		return "local-" + java.util.UUID.randomUUID() + "-" + file.getOriginalFilename();
	}

}
