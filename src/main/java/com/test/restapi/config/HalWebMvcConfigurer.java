package com.test.restapi.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class HalWebMvcConfigurer implements WebMvcConfigurer {

	@Autowired
	@Qualifier("halJacksonHttpMessageConverter")
	private ObjectProvider<TypeConstrainedMappingJackson2HttpMessageConverter> halJacksonHttpMessageConverter;

	@Autowired
	@Qualifier("repositoryExporterHandlerAdapter")
	private RequestMappingHandlerAdapter repositoryExporterHandlerAdapter;

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(0, halJacksonHttpMessageConverter.getObject());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.addAll(repositoryExporterHandlerAdapter.getCustomArgumentResolvers());
	}

}