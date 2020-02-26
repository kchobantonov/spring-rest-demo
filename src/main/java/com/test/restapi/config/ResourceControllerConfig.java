package com.test.restapi.config;

import static java.util.Arrays.asList;
import static org.springframework.data.util.Optionals.toStream;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.Path;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.BasePathAwareHandlerMapping;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.support.DelegatingHandlerMapping;
import org.springframework.data.rest.webmvc.support.ETag;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.Optionals;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import com.test.restapi.controller.ResourceController;

@Configuration
public class ResourceControllerConfig {
	private static final String GET_COLLECTION_RESOURCE = "getCollectionResource";
	private static final String GET_ITEM_RESOURCE = "getItemResource";
	private static final String POST_COLLECTION_RESOURCE = "postCollectionResource";
	private static final String PUT_ITEM_RESOURCE = "putItemResource";
	private static final String PATCH_ITEM_RESOURCE = "patchItemResource";
	private static final String DELETE_ITEM_RESOURCE = "deleteItemResource";

	@Autowired
	public void setHandlerMapping(ListableBeanFactory factory, DelegatingHandlerMapping restHandlerMapping,
			Repositories repositories, RepositoryResourceMappings repositoryResourceMappings,
			RepositoryRestConfiguration repositoryRestConfiguration) {

		Collection<ResourceController> handlers = factory.getBeansOfType(ResourceController.class).values();
		BasePathAwareHandlerMapping mapping = (BasePathAwareHandlerMapping) restHandlerMapping.getDelegates().get(1);

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

			if (resourceMetadata == null) {
				continue;
			}

			Path path = resourceMetadata.getPath();

			Optional<Method> getCollectionResourceMethod = selectMostSuitableGetCollectionResourceMethod(handlerClass);
			if (isRedeclaredMethod(getCollectionResourceMethod)) {
				RequestMappingInfo info = RequestMappingInfo
						.paths(new Path(repositoryRestConfiguration.getBasePath().toString()).slash(path).toString())
						.methods(RequestMethod.GET).build();
				mapping.registerMapping(info, handler, getCollectionResourceMethod.get());
			}

			Optional<Method> getItemResourceMethod = selectMostSuitableGetItemResourceMethod(handlerClass,
					domainIdType);
			if (isRedeclaredMethod(getItemResourceMethod)) {
				RequestMappingInfo info = RequestMappingInfo
						.paths(new Path(repositoryRestConfiguration.getBasePath().toString()).slash(path.slash("{id"
								+ (handler.getIdPathVariableRegex() != null ? ":" + handler.getIdPathVariableRegex()
										: "")
								+ "}")).toString())
						.methods(RequestMethod.GET).build();
				mapping.registerMapping(info, handler, getItemResourceMethod.get());
			}

			Optional<Method> postCollectionResourceMethod = selectMostSuitablePostCollectionResourceMethod(handlerClass,
					domainType);
			if (isRedeclaredMethod(postCollectionResourceMethod)) {
				RequestMappingInfo info = RequestMappingInfo
						.paths(new Path(repositoryRestConfiguration.getBasePath().toString()).slash(path).toString())
						.methods(RequestMethod.POST).build();
				mapping.registerMapping(info, handler, postCollectionResourceMethod.get());
			}

			Optional<Method> putItemResourceMethod = selectMostSuitablePutItemResourceMethod(handlerClass, domainType,
					domainIdType);
			if (isRedeclaredMethod(putItemResourceMethod)) {
				RequestMappingInfo info = RequestMappingInfo
						.paths(new Path(repositoryRestConfiguration.getBasePath().toString()).slash(path.slash("{id"
								+ (handler.getIdPathVariableRegex() != null ? ":" + handler.getIdPathVariableRegex()
										: "")
								+ "}")).toString())
						.methods(RequestMethod.PUT).build();
				mapping.registerMapping(info, handler, putItemResourceMethod.get());
			}

			Optional<Method> patchItemResourceMethod = selectMostSuitablePatchItemResourceMethod(handlerClass,
					domainType, domainIdType);
			if (isRedeclaredMethod(patchItemResourceMethod)) {
				RequestMappingInfo info = RequestMappingInfo
						.paths(new Path(repositoryRestConfiguration.getBasePath().toString()).slash(path.slash("{id"
								+ (handler.getIdPathVariableRegex() != null ? ":" + handler.getIdPathVariableRegex()
										: "")
								+ "}")).toString())
						.methods(RequestMethod.PATCH).build();
				mapping.registerMapping(info, handler, patchItemResourceMethod.get());
			}

			Optional<Method> deleteItemResourceMethod = selectMostSuitableDeleteItemResourceMethod(handlerClass,
					domainIdType);
			if (isRedeclaredMethod(deleteItemResourceMethod)) {
				RequestMappingInfo info = RequestMappingInfo
						.paths(new Path(repositoryRestConfiguration.getBasePath().toString()).slash(path.slash("{id"
								+ (handler.getIdPathVariableRegex() != null ? ":" + handler.getIdPathVariableRegex()
										: "")
								+ "}")).toString())
						.methods(RequestMethod.DELETE).build();
				mapping.registerMapping(info, handler, deleteItemResourceMethod.get());

			}
		}
	}

	private static Optional<Method> selectMostSuitableGetCollectionResourceMethod(Class<?> clazz) {
		return toStream(findMethod(clazz, GET_COLLECTION_RESOURCE,
				new Class<?>[] { Pageable.class, PersistentEntityResourceAssembler.class }))//
						.flatMap(it -> toStream(getMostSpecificMethod(it, clazz)))//
						.findFirst();
	}

	private static Optional<Method> selectMostSuitableGetItemResourceMethod(Class<?> clazz, Class<?> domainIdType) {
		Stream<Class<?>[]> source = Stream.of(//
				new Class<?>[] { domainIdType, PersistentEntityResourceAssembler.class, HttpHeaders.class }, //
				new Class<?>[] { Object.class, PersistentEntityResourceAssembler.class, HttpHeaders.class });
		
		return source//
				.flatMap(it -> toStream(findMethod(clazz, GET_ITEM_RESOURCE, it)))//
				.flatMap(it -> toStream(getMostSpecificMethod(it, clazz)))//
				.findFirst();
	}

	private static Optional<Method> selectMostSuitablePostCollectionResourceMethod(Class<?> clazz,
			Class<?> domainType) {

		Stream<Class<?>[]> source = Stream.of(//
				new Class<?>[] { domainType, PersistentEntityResourceAssembler.class, String.class }, //
				new Class<?>[] { Object.class, PersistentEntityResourceAssembler.class, String.class });

		return source//
				.flatMap(it -> toStream(findMethod(clazz, POST_COLLECTION_RESOURCE, it)))//
				.flatMap(it -> toStream(getMostSpecificMethod(it, clazz)))//
				.findFirst();
	}

	private static Optional<Method> selectMostSuitablePutItemResourceMethod(Class<?> clazz, Class<?> domainType,
			Class<?> domainIdType) {
		Stream<Class<?>[]> source = Stream.of(//
				new Class<?>[] { domainType, domainIdType, PersistentEntityResourceAssembler.class, ETag.class, String.class }, //
				new Class<?>[] { Object.class, Object.class, PersistentEntityResourceAssembler.class, ETag.class, String.class });
		
		return source//
				.flatMap(it -> toStream(findMethod(clazz, PUT_ITEM_RESOURCE, it)))//
				.flatMap(it -> toStream(getMostSpecificMethod(it, clazz)))//
				.findFirst();
	}

	private static Optional<Method> selectMostSuitablePatchItemResourceMethod(Class<?> clazz, Class<?> domainType,
			Class<?> domainIdType) {
		Stream<Class<?>[]> source = Stream.of(//
				new Class<?>[] { domainType, domainIdType, PersistentEntityResourceAssembler.class, ETag.class, String.class }, //
				new Class<?>[] { Object.class, Object.class, PersistentEntityResourceAssembler.class, ETag.class, String.class });
		
		return source//
				.flatMap(it -> toStream(findMethod(clazz, PATCH_ITEM_RESOURCE, it)))//
				.flatMap(it -> toStream(getMostSpecificMethod(it, clazz)))//
				.findFirst();
	}

	private static Optional<Method> selectMostSuitableDeleteItemResourceMethod(Class<?> clazz, Class<?> domainIdType) {
		Stream<Class<?>[]> source = Stream.of(//
				new Class<?>[] { domainIdType, ETag.class }, //
				new Class<?>[] { Object.class, ETag.class });
		
		return source//
				.flatMap(it -> toStream(findMethod(clazz, DELETE_ITEM_RESOURCE, it)))//
				.flatMap(it -> toStream(getMostSpecificMethod(it, clazz)))//
				.findFirst();
	}

	/**
	 * Looks up the most specific method for the given method and type and returns
	 * an accessible version of discovered {@link Method} if found.
	 *
	 * @param method
	 * @param type
	 * @see ClassUtils#getMostSpecificMethod(Method, Class)
	 * @return
	 */
	private static Optional<Method> getMostSpecificMethod(Method method, Class<?> type) {

		return Optionals.toStream(Optional.ofNullable(ClassUtils.getMostSpecificMethod(method, type)))//
				.map(it -> BridgeMethodResolver.findBridgedMethod(it))//
				.peek(it -> ReflectionUtils.makeAccessible(it))//
				.findFirst();
	}

	private static Optional<Method> findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
		return Optional.ofNullable(ReflectionUtils.findMethod(type, name, parameterTypes));
	}

	private static boolean isRedeclaredMethod(Optional<Method> method) {
		return method.map(it -> !it.getDeclaringClass().equals(ResourceController.class)).orElse(false);
	}

	private static Class<?> resolveTypeParameter(List<TypeInformation<?>> arguments, int index,
			Supplier<String> exceptionMessage) {

		if (arguments.size() <= index || arguments.get(index) == null) {
			throw new IllegalArgumentException(exceptionMessage.get());
		}

		return arguments.get(index).getType();
	}
}
