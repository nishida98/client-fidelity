package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.lhn.client_fidelity.application.authentication.AccessTokenVerifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcAuthenticationConfiguration implements WebMvcConfigurer {

	private final AccessTokenVerifier accessTokenVerifier;

	public WebMvcAuthenticationConfiguration(AccessTokenVerifier accessTokenVerifier) {
		this.accessTokenVerifier = accessTokenVerifier;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new JwtAuthenticationInterceptor(accessTokenVerifier)).addPathPatterns("/campaigns/**");
	}
}
