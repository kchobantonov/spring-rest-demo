package com.test.restapi.controller;

import static org.springframework.data.rest.webmvc.ControllerUtils.EMPTY_RESOURCE_LIST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.mapping.SearchResourceMappings;
import org.springframework.data.rest.webmvc.HttpHeadersPreparer;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.ProfileController;
import org.springframework.data.rest.webmvc.ProfileResourceProcessor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.rest.webmvc.support.ETag;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public abstract class ResourceController<T> {
	private static final EmbeddedWrappers WRAPPERS = new EmbeddedWrappers(false);

	protected final PagedResourcesAssembler<Object> pagedResourcesAssembler;

	protected final Repositories repositories;
	protected final RepositoryEntityLinks entityLinks;
	protected final RepositoryRestConfiguration config;
	protected final HttpHeadersPreparer headersPreparer;
	protected final ResourceMappings mappings;

	protected final PersistentEntity<T, ?> persitentEntity;
	protected final Class<T> domainType;

	public ResourceController(PagedResourcesAssembler<Object> pagedResourcesAssembler, Repositories repositories,
			RepositoryRestConfiguration config, RepositoryEntityLinks entityLinks, HttpHeadersPreparer headersPreparer,
			ResourceMappings mappings) {

		Assert.notNull(pagedResourcesAssembler, "PagedResourcesAssembler must not be null!");

		this.pagedResourcesAssembler = pagedResourcesAssembler;
		this.repositories = repositories;
		this.entityLinks = entityLinks;
		this.config = config;
		this.headersPreparer = headersPreparer;
		this.mappings = mappings;

		List<TypeInformation<?>> arguments = ClassTypeInformation.from(getClass()) //
				.getRequiredSuperTypeInformation(ResourceController.class)//
				.getTypeArguments();

		this.domainType = (Class<T>) resolveTypeParameter(arguments, 0,
				() -> String.format("Could not resolve domain type of %s!", getClass()));

		this.persitentEntity = (PersistentEntity<T, ?>) repositories.getPersistentEntity(domainType);
	}

	private static Class<?> resolveTypeParameter(List<TypeInformation<?>> arguments, int index,
			Supplier<String> exceptionMessage) {

		if (arguments.size() <= index || arguments.get(index) == null) {
			throw new IllegalArgumentException(exceptionMessage.get());
		}

		return arguments.get(index).getType();
	}

	protected Links getCollectionResourceLinks(RepositoryRestConfiguration config, RepositoryEntityLinks entityLinks,
			ResourceMetadata metadata) {

		SearchResourceMappings searchMappings = metadata.getSearchResourceMappings();

		Links links = Links
				.of(new Link(ProfileController.getPath(config, metadata), ProfileResourceProcessor.PROFILE_REL));

		return searchMappings.isExported() //
				? links.and(entityLinks.linkFor(metadata.getDomainType()).slash(searchMappings.getPath())
						.withRel(searchMappings.getRel()))
				: links;
	}

	@SuppressWarnings({ "unchecked" })
	protected CollectionModel<?> toCollectionModel(Iterable<?> source, PersistentEntityResourceAssembler assembler,
			Class<?> domainType, Optional<Link> baseLink) {

		if (source instanceof Page) {
			Page<Object> page = (Page<Object>) source;
			return entitiesToResources(page, assembler, domainType, baseLink);
		} else if (source instanceof Iterable) {
			return entitiesToResources((Iterable<Object>) source, assembler, domainType);
		} else {
			return new CollectionModel(EMPTY_RESOURCE_LIST);
		}
	}

	protected CollectionModel<?> entitiesToResources(Page<Object> page, PersistentEntityResourceAssembler assembler,
			Class<?> domainType, Optional<Link> baseLink) {

		if (page.getContent().isEmpty()) {
			return baseLink.<PagedModel<?>>map(it -> pagedResourcesAssembler.toEmptyModel(page, domainType, it))//
					.orElseGet(() -> pagedResourcesAssembler.toEmptyModel(page, domainType));
		}

		return baseLink.map(it -> pagedResourcesAssembler.toModel(page, assembler, it))//
				.orElseGet(() -> pagedResourcesAssembler.toModel(page, assembler));
	}

	protected CollectionModel<?> entitiesToResources(Iterable<Object> entities,
			PersistentEntityResourceAssembler assembler, Class<?> domainType) {

		if (!entities.iterator().hasNext()) {

			List<Object> content = Arrays.<Object>asList(WRAPPERS.emptyCollectionOf(domainType));
			return new CollectionModel<Object>(content, getDefaultSelfLink());
		}

		List<EntityModel<Object>> resources = new ArrayList<EntityModel<Object>>();

		for (Object obj : entities) {
			resources.add(obj == null ? null : assembler.toModel(obj));
		}

		return new CollectionModel<EntityModel<Object>>(resources, getDefaultSelfLink());
	}

	protected Link getDefaultSelfLink() {
		return new Link(ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString());
	}

	protected boolean isModified(HttpHeadersPreparer headersPreparer, HttpHeaders requestHeaders, Object domainObject,
			PersistentEntity<?, ?> entity) {
		List<String> ifNoneMatch = requestHeaders.getIfNoneMatch();
		ETag eTag = ifNoneMatch.isEmpty() ? ETag.NO_ETAG : ETag.from(ifNoneMatch.get(0));
		HttpHeaders responseHeaders = headersPreparer.prepareHeaders(entity, domainObject);
		return eTag.matches(entity, domainObject) || headersPreparer.isObjectStillValid(domainObject, requestHeaders)
				? false
				: true;
	}

	protected CollectionModel<?> entitiesToResource(Page<T> entities, Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		ResourceMetadata metadata = mappings.getMetadataFor(persitentEntity.getType());

		Optional<Link> baseLink = Optional.of(entityLinks.linkToPagedResource(metadata.getDomainType(), pageable));

		CollectionModel<?> result = toCollectionModel(entities, assembler, metadata.getDomainType(), baseLink);
		result.add(getCollectionResourceLinks(config, entityLinks, metadata));
		return result;
	}

	protected ResponseEntity<EntityModel<T>> entityToResource(Optional<T> entity, HttpHeaders headers,
			PersistentEntityResourceAssembler assembler) throws HttpRequestMethodNotSupportedException {
		return entity.map(it -> {

			if (isModified(headersPreparer, headers, it, persitentEntity)) {
				return new ResponseEntity<EntityModel<T>>((EntityModel<T>) assembler.toFullResource(it), headers,
						HttpStatus.OK);
			} else {
				return new ResponseEntity<EntityModel<T>>(headers, HttpStatus.NOT_MODIFIED);
			}
		}).orElseThrow(() -> new ResourceNotFoundException());
	}
}