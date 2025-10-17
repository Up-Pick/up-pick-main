package org.oneog.uppick.support;

import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.context.annotation.Import;

@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@Import(RestDocsConfig.class)
public abstract class RestDocsBase {

}
