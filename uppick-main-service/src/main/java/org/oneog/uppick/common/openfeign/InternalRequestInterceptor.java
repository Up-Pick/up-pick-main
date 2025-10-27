package org.oneog.uppick.common.openfeign;

import org.oneog.uppick.common.constants.AuthConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class InternalRequestInterceptor implements RequestInterceptor {
    private final String internalApiSecret;

    public InternalRequestInterceptor(@Value("${internal.api.secret}")
    String internalApiSecret) {
        this.internalApiSecret = internalApiSecret;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(AuthConstant.INTERNAL_AUTH_HEADER, internalApiSecret);
    }
}
