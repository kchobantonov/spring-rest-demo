package com.test.restapi.config;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.Path;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.BasePathAwareHandlerMapping;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.support.DelegatingHandlerMapping;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import com.test.restapi.controller.ResourceController;

@Configuration
public class ResourceControllerConfig {

	@Autowired
	public void setHandlerMapping(ListableBeanFactory factory, DelegatingHandlerMapping restHandlerMapping,
			Repositories repositories, RepositoryResourceMappings repositoryResourceMappings,
			RepositoryRestConfiguration repositoryRestConfiguration) {

		Collection<ResourceController> handlers = factory.getBeansOfType(ResourceController.class).values();
		BasePathAwareHandlerMapping mapping = ((BasePathAwareHandlerMapping) restHandlerMapping.getDelegates().get(1));

		for (ResourceController handler : handlers) {
			Class<?> handlerClass = AopUtils.getTargetClass(handler);

			List<TypeInformation<?>> arguments = ClassTypeInformation.from(handlerClass) //
					.getRequiredSuperTypeInformation(ResourceController.class)//
					.getTypeArguments();

			Class<?> domainType = resolveTypeParameter(arguments, 0,
					() -> String.format("Could not resolve domain type of %s!", handlerClass));
			Class<?> domainIdType = resolveTypeParameter(arguments, 1,
					() -> String.format("Could not resolve domain id type of %s!", handlerClass));

			ResourceMetadata resourceMetadata = repositoryResourceMappings.getMetadataFor(domainType);

			Method getCollectionResourceMethod = ReflectionUtils.findMethod(handlerClass, "getCollectionResource",
					new Class[] { Pageable.class, PersistentEntityResourceAssembler.class });

			if (getCollectionResourceMethod != null
					&& !getCollectionResourceMethod.getDeclaringClass().equals(ResourceController.class)
					&& resourceMetadata != null) {

				RequestMappingInfo info = RequestMappingInfo
						.paths(new Path(repositoryRestConfiguration.getBasePath().toString())
								.slash(resourceMetadata.getPath()).toString())
						.methods(RequestMethod.GET).build();
				mapping.registerMapping(info, handler, getCollectionResourceMethod);
			}

			Method getItemResourceMethod = ReflectionUtils.findMethod(handlerClass, "getItemResource",
					new Class[] { domainIdType, PersistentEntityResourceAssembler.class, HttpHeaders.class });

			if (getItemResourceMethod != null
					&& !getItemResourceMethod.getDeclaringClass().equals(ResourceController.class)
					&& resourceMetadata != null) {

				RequestMappingInfo info = RequestMappingInfo.paths(
						new Path(repositoryRestConfiguration.getBasePath().toString()).slash(resourceMetadata.getPath()
								.slash("{id" + (handler.getIdPathVariableRegex() != null
										? ":" + handler.getIdPathVariableRegex()
										: "") + "}"))
								.toString())
						.methods(RequestMethod.GET).build();
				mapping.registerMapping(info, handler, getItemResourceMethod);
			}

		}
	}

	private static Class<?> resolveTypeParameter(List<TypeInformation<?>> arguments, int index,
			Supplier<String> exceptionMessage) {

		if (arguments.size() <= index || arguments.get(index) == null) {
			throw new IllegalArgumentException(exceptionMessage.get());
		}

		return arguments.get(index).getType();
	}
}
