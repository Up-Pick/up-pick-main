package org.oneog.uppick.auction.support.restdocs;

import org.oneog.uppick.auction.support.auth.TestSecurityConfig;
import org.oneog.uppick.common.auth.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@Import({RestDocsConfig.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
public abstract class RestDocsBase {

    @MockitoBean
    protected JwtUtil jwtUtil;

    @Autowired
    protected MockMvc mockMvc;

}
