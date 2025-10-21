# `member-service` 분리 계획서 (Common 모듈 포함 Best Practice)

## 1. 목표

-   재사용 가능한 `uppick-common` 모듈을 포함하여, `uppick-app`과 `member-service` 두 개의 마이크로서비스로 분리합니다.
-   `uppick-common` 모듈은 Spring Boot 자동 설정을 통해 다른 서비스에서 쉽게 사용 가능하도록 구성합니다.
-   각 서비스는 독립적으로 개발, 테스트, 배포가 가능해야 합니다.

## 2. 핵심 결정사항

-   **데이터베이스**: 초기에는 **단일 데이터베이스를 공유**합니다. 이는 구조 변경을 단순화하지만, 장기적으로는 서비스 간의 강한 결합을 유발하므로 분리가 권장됩니다.
-   **공통 코드**: 여러 서비스에서 사용되는 DTO, Exception, Util 클래스 등을 `uppick-common` 모듈에서 중앙 관리하여 코드 중복을 방지하고 일관성을 유지합니다.

---

## 3. 단계별 실행 계획

### Phase 1: Gradle 멀티 프로젝트 구조 설정

1.  **`settings.gradle` 수정**: `uppick-app`, `member-service`, `uppick-common` 세 개의 모듈을 정의합니다.

    ```gradle
    rootProject.name = 'uppick'

    include 'uppick-app'
    include 'member-service'
    include 'uppick-common'
    ```

2.  **디렉토리 구조 생성**:
    -   `uppick-app`, `member-service`, `uppick-common` 디렉토리를 생성합니다.
    -   기존의 `src`, `build.gradle` 등 모든 파일을 `uppick-app` 디렉토리 안으로 이동시킵니다.
    -   나머지 두 디렉토리에는 비어있는 `build.gradle` 파일을 만듭니다.

3.  **모듈별 `build.gradle` 설정**:
    -   `uppick-common/build.gradle`: `spring-boot-autoconfigure`, `lombok` 등 다른 모듈이 의존할 최소한의 라이브러리를 추가합니다.
    -   `uppick-app/build.gradle`: `implementation project(':uppick-common')` 의존성을 추가합니다.
    -   `member-service/build.gradle`: `implementation project(':uppick-common')` 의존성을 추가하고, 필요한 다른 의존성(Web, Data-JPA 등)을 구성합니다.

### Phase 2: `uppick-common` 모듈 구현 (Spring Boot Starter)

1.  **공통 코드 이동**: `uppick-app/src/main/java/org/oneog/uppick/common`의 코드를 `uppick-common/src/main/java/org/oneog/uppick/common` 경로로 이동합니다.

2.  **자동 설정 클래스 생성**:
    -   `uppick-common` 모듈에 `CommonAutoConfiguration.java` 파일을 생성합니다.
    -   이 클래스가 `common` 모듈 내의 Bean(`@RestControllerAdvice`, `@Component` 등)을 스캔하도록 설정합니다.

    ```java
    // uppick-common/src/main/java/org/oneog/uppick/common/config/CommonAutoConfiguration.java
    package org.oneog.uppick.common.config;

    import org.springframework.context.annotation.ComponentScan;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    @ComponentScan("org.oneog.uppick.common")
    public class CommonAutoConfiguration {}
    ```

3.  **자동 설정 등록**:
    -   `uppick-common/src/main/resources/META-INF/spring/` 디렉토리를 생성합니다.
    -   그 안에 `org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일을 만들고, 위에서 생성한 자동 설정 클래스의 전체 경로를 추가합니다.

    ```
    # uppick-common/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    org.oneog.uppick.common.config.CommonAutoConfiguration
    ```

### Phase 3: 코드 분리 및 리팩토링

1.  **`member-service` 코드 구성**:
    -   `uppick-app/src`에서 `domain/member`와 `domain/auth` 패키지를 `member-service/src/main/java/...` 경로로 **이동**합니다.
    -   `member-service`에 Spring Boot 시작 클래스(`MemberApplication.java`)를 생성합니다.

2.  **`uppick-app` 코드 정리**:
    -   `uppick-app` 모듈에서 `domain/member`, `domain/auth`, `common` 관련 코드를 **모두 삭제**합니다. (이제 `uppick-common` 모듈을 통해 주입받으므로 필요 없습니다.)

3.  **서비스 간 통신 구현**:
    -   `uppick-app`에서 `MemberService`를 직접 호출하던 코드를 `WebClient` (권장) 또는 `RestTemplate`을 사용하여 `member-service`의 REST API를 호출하도록 변경합니다.

### Phase 4: 빌드 및 배포 설정

1.  **Dockerfile 생성**: `uppick-app`과 `member-service` 각각의 디렉토리 내부에 독립적인 `Dockerfile`을 생성합니다.
2.  **`docker-compose.yml` 수정**: `uppick-app`과 `member-service` 두 서비스를 정의하고, 두 서비스 모두 동일한 데이터베이스에 연결하도록 설정합니다.
3.  **CI/CD 파이프라인 수정**: GitHub Actions 워크플로우가 `uppick-app`과 `member-service`를 각각 독립적으로 빌드하고 Docker 이미지를 생성하도록 수정합니다.

---