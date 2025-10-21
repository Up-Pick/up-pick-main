package org.oneog.uppick.domain.product.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.oneog.uppick.domain.product.exception.ProductErrorCode;
import org.oneog.uppickcommon.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class S3FileStorageService implements S3FileManager {

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
		"jpg", "jpeg", "png", "gif", "webp");
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
	private final S3Client s3Client;
	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;
	@Value("${spring.cloud.aws.region.static}")
	private String region;

	@Override
	public String store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ProductErrorCode.EMPTY_FILE);
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			throw new BusinessException(ProductErrorCode.FILE_SIZE_EXCEEDED);
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.isBlank()) {
			throw new BusinessException(ProductErrorCode.INVALID_FILE_TYPE);
		}

		String extension = getExtension(originalFilename);
		if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new BusinessException(ProductErrorCode.INVALID_FILE_TYPE);
		}

		String s3Key = generateS3Key(extension);

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.contentType(file.getContentType())
				.contentLength(file.getSize())
				.build();

			s3Client.putObject(
				putObjectRequest,
				RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			String fileUrl = String.format(
				"https://%s.s3.%s.amazonaws.com/%s",
				bucketName, region, s3Key);

			log.info("S3 업로드 성공: {}", fileUrl);
			return fileUrl;

		} catch (IOException e) {
			log.error("S3 업로드 실패: {}", e.getMessage());
			throw new BusinessException(ProductErrorCode.FILE_UPLOAD_FAILED);
		}
	}

	private String generateS3Key(String extension) {
		String timestamp = LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("yyyyMMdd/HHmmss"));
		String uuid = UUID.randomUUID().toString().substring(0, 8);
		return String.format("products/%s_%s.%s", timestamp, uuid, extension);
	}

	private String getExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new BusinessException(ProductErrorCode.INVALID_FILE_TYPE);
		}
		return filename.substring(filename.lastIndexOf(".") + 1);
	}
}