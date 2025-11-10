# 1. AWS에서 제공하는 공식 ADOT Collector 이미지를 기반으로 시작합니다.
FROM public.ecr.aws/aws-observability/aws-otel-collector:latest

# 2. 로컬의 설정 파일(otel-collector-config.yaml)을
#    이미지 안의 특정 경로(/etc/otel/config.yaml)로 복사합니다.
COPY otel-collector-config.yaml /etc/otel/config.yaml