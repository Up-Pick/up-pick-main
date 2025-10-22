package org.oneog.uppick.support;

import org.oneog.uppick.common.auth.JwtUtil;
import org.oneog.uppick.common.auth.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@Import({RestDocsConfig.class, SecurityConfig.class})
public abstract class RestDocsBase {
    @MockitoBean
    protected JwtUtil jwtUtil;

    @Autowired
    protected MockMvc mockMvc;
}
