package com.test.restapi.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class RestApiBasicAuthenticationFilter extends BasicAuthenticationFilter {
	private RestApiAuthenticationSuccessHandler successHandler;

	public RestApiBasicAuthenticationFilter(AuthenticationManager authenticationManager,
			RestApiAuthenticationSuccessHandler successHandler) {
		super(authenticationManager);
		this.successHandler = successHandler;
	}

	@Override
	protected void onSuccessfulAuthentication(javax.servlet.http.HttpServletRequest request,
			javax.servlet.http.HttpServletResponse response, Authentication authResult) throws IOException {
		successHandler.onAuthenticationSuccess(request, response, authResult);
	}
}
