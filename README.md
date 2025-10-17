## 도커 실행

docker-compose up --build

## 도커 종료

docker-compose down

## REST Docs

### 테스트 + 문서 생성

./gradlew test asciidoctor

### 테스트 문서 생성 과정

- `./gradlew test` 실행 시 REST Docs 스니펫이 `build/generated-snippets` 아래에 떨어집니다.
- 테스트가 끝나면 Gradle이 `generateRestDocsIndex` 작업을 자동으로 수행해, 방금 생성된 모든 스니펫 디렉터리를 읽고 `auto-index.adoc` 파일을 만듭니다.
- `src/docs/asciidoc/index.adoc`은 이 `auto-index.adoc`을 그대로 include 하도록 되어 있어, 새 테스트가 추가돼도 README를 포함한 문서 템플릿을 다시 손댈 필요가 없습니다.
- 이어서 `asciidoctor` 작업이 실행되어 `build/docs/asciidoc/index.html`이 갱신되며, 여기에는 모든 REST Docs 테스트 결과가 자동으로 반영됩니다.
- 전역 `RestDocsMockMvcConfigurationCustomizer`가 요청/응답을 pretty-print 하며, `src/test/resources/org/springframework/restdocs/templates/asciidoctor`에 둔 커스텀 템플릿이 응답 필드 표에 `Notes` 컬럼(필수 여부, 포맷 등)을 자동으로 붙여 줍니다.

### 새 엔드포인트를 문서화하려면

1. MockMvc 테스트 추가: `WebMvcTest` 기반 테스트 클래스에 `@AutoConfigureRestDocs(outputDir = "build/generated-snippets")`와 `@Import(RestDocsConfig.class)`를 붙이고, `mockMvc.perform(...)`에 `andDo(document("operation-name", ...))`를 연결합니다. 문서 표에 노출할 정보는 `fieldWithPath(...).attributes(key("format").value("yyyy-MM-dd"))`처럼 `Attributes`로 넘겨주세요.
2. 스니펫 이름 규칙: `document("operation-name", ...)`의 첫 번째 인자가 스니펫 폴더명이 됩니다. 예) `document("product-get", ...)` → `build/generated-snippets/product-get`.
3. 자동 목차 반영: `generateRestDocsIndex`가 스니펫 폴더를 순회해 `.adoc`을 자동 구성합니다. 테스트만 추가하면 `build/docs/asciidoc/index.html`에 새 섹션이 생깁니다.
4. 커스텀 템플릿: `src/test/resources/org/springframework/restdocs/templates/asciidoctor` 아래의 `.snippet` 파일로 표 레이아웃을 바꿀 수 있습니다. 필요하다면 `request-fields.snippet` 등 다른 템플릿도 동일한 구조로 추가하세요.
5. 레이아웃 조정: `src/docs/asciidoc/index.adoc`에서 Asciidoctor 속성을 조정(`:toclevels:` 등)하거나, `auto-index.adoc` 대신 `operation::operation-name[...]` 매크로를 사용해 수동으로 문서 구조를 설계할 수도 있습니다.

### 생성된 문서 확인(Windows)

start build/docs/asciidoc/index.html

### 참고할 예시 테스트
`ProductControllerRestDocsTest.java`

