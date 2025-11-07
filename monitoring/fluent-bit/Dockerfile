FROM fluent/fluent-bit:3.2

VOLUME ["/logs"]

# 모든 설정 파일 복사
COPY fluent-bit-auction.conf /fluent-bit/etc/
COPY fluent-bit-main.conf /fluent-bit/etc/
COPY parsers.conf /fluent-bit/etc/

ENTRYPOINT ["/fluent-bit/bin/fluent-bit"]
CMD ["-c", "/fluent-bit/etc/fluent-bit-auction.conf"]
