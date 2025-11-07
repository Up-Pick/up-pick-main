FROM fluent/fluent-bit:3.2

# 로그 볼륨 (Spring Boot 컨테이너와 공유)
VOLUME ["/logs"]

# 설정 파일 복사
COPY fluent-bit.conf /fluent-bit/etc/fluent-bit.conf
COPY parsers.conf /fluent-bit/etc/parsers.conf

# Fluent Bit 실행
ENTRYPOINT ["/fluent-bit/bin/fluent-bit"]
CMD ["-c", "/fluent-bit/etc/fluent-bit.conf"]
