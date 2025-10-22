# `member-service` 분리 및 API Gateway 도입 계획서

## 1. 목표

-   기존 모놀리식 애플리케이션을 `uppick-main`, `member-service`, `api-gateway` 세 개의 마이크로서비스로 분리합니다.
-   `api-gateway`를 통해 모든 요청에 대한 단일 진입점을 제공하고, 인증 등 공통 기능을 중앙에서 처리합니다.
-   재사용 가능한 `uppick-common` 모듈을 Spring Boot 자동 설정을 통해 다른 서비스에서 쉽게 사용하도록 구성합니다.
-   각 서비스는 독립적으로 개발, 테스트, 배포가 가능해야 합니다.

## 2. 핵심 결정사항

-   **API Gateway**: 모든 외부 요청은 **API Gateway**를 통해서만 내부 서비스로 전달됩니다. 이를 통해 인증, 라우팅, 로깅 등을 중앙 관리합니다.
-   **데이터베이스**: 초기에는 **단일 데이터베이스를 공유**합니다. 이는 구조 변경을 단순화하지만, 장기적으로는 서비스별 데이터베이스 분리가 권장됩니다.
-   **공통 코드**: 여러 서비스에서 사용되는 DTO, Exception, Util 클래스 등을 `uppick-common` 모듈에서 관리하여 코드 중복을 방지하고 일관성을 유지합니다.

---

## 3. 단계별 실행 계획

### Phase 1: Gradle 멀티 프로젝트 구조 설정

1.  **`settings.gradle` 수정**: `uppick-main`, `member-service`, `uppick-common`, `api-gateway` 네 개의 모듈을 정의합니다.

    ```gradle
    rootProject.name = 'uppick'

    include 'uppick-main'
    include 'member-service'
    include 'uppick-common'
    include 'api-gateway'
    ```

2.  **디렉토리 구조 생성**:
    -   `uppick-main`, `member-service`, `uppick-common`, `api-gateway` 디렉토리를 생성합니다.
    -   기존의 `src`, `build.gradle` 등 모든 파일을 `uppick-main` 디렉토리 안으로 이동시킵니다.
    -   나머지 디렉토리에는 비어있는 `build.gradle` 파일을 만듭니다.

3.  **모듈별 `build.gradle` 설정**:
    -   `uppick-common`: 다른 모듈이 의존할 최소한의 라이브러리(`spring-boot-autoconfigure`, `lombok` 등)를 추가합니다.
    -   `uppick-main`: `implementation project(':uppick-common')` 의존성을 추가합니다.
    -   `member-service`: `implementation project(':uppick-common')` 및 필요한 의존성(Web, Data-JPA 등)을 구성합니다.
    -   `api-gateway`: `spring-cloud-starter-gateway` 의존성을 추가합니다.

### Phase 2: `uppick-common` 모듈 구현 (Spring Boot Starter)

(기존 내용과 동일, 변경 없음)

1.  **공통 코드 이동**: `uppick-main/src/main/java/org/oneog/uppick/common`의 코드를 `uppick-common/src/main/java/org/oneog/uppick/common` 경로로 이동합니다.

2.  **자동 설정 클래스 생성**:
    -   `uppick-common` 모듈에 `CommonAutoConfiguration.java` 파일을 생성하고 `common` 패키지를 스캔하도록 설정합니다.

    ```java
    // uppick-common/src/main/java/org/oneog/uppick/common/config/CommonAutoConfiguration.java
    @Configuration
    @ComponentScan("org.oneog.uppick.common")
    public class CommonAutoConfiguration {}
    ```

3.  **자동 설정 등록**:
    -   `uppick-common/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일에 `CommonAutoConfiguration` 클래스의 전체 경로를 추가합니다.

### Phase 3: 코드 분리 및 리팩토링

1.  **`member-service` 코드 구성**:
    -   `uppick-main/src`에서 `domain/member`와 `domain/auth` 패키지를 `member-service/src/main/java/...` 경로로 **이동**합니다.
    -   `member-service`에 Spring Boot 시작 클래스(`MemberApplication.java`)를 생성합니다.

2.  **`uppick-main` 코드 정리**:
    -   `uppick-main` 모듈에서 `domain/member`, `domain/auth` 관련 코드를 **모두 삭제**합니다.
    -   `JwtAuthenticationFilter` 등 인증 관련 로직은 `api-gateway`로 이전되므로 삭제하거나 수정합니다.

3.  **서비스 간 통신 구현**:
    -   `uppick-main`에서 `Member` 정보가 필요한 경우, `WebClient` (권장) 등을 사용하여 `member-service`의 REST API를 호출하도록 변경합니다. (이 통신은 서비스 내부 통신이 됩니다.)

### Phase 4: API Gateway 구축

1.  **`api-gateway` 모듈 구성**:
    -   `api-gateway`에 Spring Boot 시작 클래스(`ApiGatewayApplication.java`)를 생성합니다.
    -   `application.yml`에 라우팅 규칙 및 포트(예: 8000)를 설정합니다.

    ```yaml
    # api-gateway/src/main/resources/application.yml
    spring:
      cloud:
        gateway:
          routes:
            - id: member-service
              uri: lb://MEMBER-SERVICE # 또는 http://localhost:8081
              predicates:
                - Path=/api/v1/members/**, /api/v1/auth/**
            - id: uppick-main
              uri: lb://UPPICK-MAIN # 또는 http://localhost:8080
              predicates:
                - Path=/api/v1/products/**, /api/v1/auctions/**
    ```

2.  **중앙 인증 필터 구현**:
    -   Gateway에 `GlobalFilter`를 구현하여 모든 요청에 대해 JWT 토큰을 검증하는 **인증(Authentication)** 로직을 추가합니다.
    -   인증 성공 시, 토큰에서 추출한 사용자 ID 등의 정보를 `X-Authenticated-User-Id`와 같은 커스텀 헤더에 담아 다운스트림 서비스로 전달합니다.

3.  **마이크로서비스 보안 재구성**:
    -   `uppick-main`과 `member-service`는 Gateway에서 오는 요청을 신뢰하도록 설정합니다.
    -   각 서비스에서는 전달받은 헤더의 사용자 정보를 기반으로 **인가(Authorization)** 로직(예: 리소스 소유권 확인, Role 기반 접근 제어)에만 집중하도록 보안 설정을 간소화합니다.

### Phase 5: 빌드 및 배포 설정

1.  **Dockerfile 생성**: `uppick-main`, `member-service`, `api-gateway` 각각의 디렉토리 내부에 독립적인 `Dockerfile`을 생성합니다.
2.  **`docker-compose.yml` 수정**: 세 개의 서비스를 모두 정의하고, 클라이언트의 요청이 `api-gateway`를 통하도록 포트 설정을 조정합니다.
3.  **CI/CD 파이프라인 수정**: GitHub Actions 워크플로우가 세 개의 서비스를 각각 독립적으로 빌드하고 Docker 이미지를 생성하도록 수정합니다.
