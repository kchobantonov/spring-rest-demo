package com.test.restapi.controller;

import static org.springframework.data.rest.webmvc.ControllerUtils.EMPTY_RESOURCE_LIST;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.AfterCreateEvent;
import org.springframework.data.rest.core.event.BeforeCreateEvent;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.mapping.ResourceType;
import org.springframework.data.rest.core.mapping.SearchResourceMappings;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.HttpHeadersPreparer;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.ProfileController;
import org.springframework.data.rest.webmvc.ProfileResourceProcessor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.data.rest.webmvc.RootResourceInformation;
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
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public abstract class ResourceController<T, ID> implements ApplicationEventPublisherAware {
	private static final EmbeddedWrappers WRAPPERS = new EmbeddedWrappers(false);

	protected final PagedResourcesAssembler<Object> pagedResourcesAssembler;

	private static final List<String> ACCEPT_PATCH_HEADERS = Arrays.asList(//
			RestMediaTypes.MERGE_PATCH_JSON.toString(), //
			RestMediaTypes.JSON_PATCH_JSON.toString(), //
			MediaType.APPLICATION_JSON_VALUE);

	private static final String ACCEPT_HEADER = "Accept";
	private static final String LINK_HEADER = "Link";

	protected final Repositories repositories;
	protected final RepositoryEntityLinks entityLinks;
	protected final RepositoryRestConfiguration config;
	protected final HttpHeadersPreparer headersPreparer;
	protected final ResourceMappings mappings;

	protected final PersistentEntity<T, ?> persitentEntity;
	protected final Class<T> domainType;
	protected final ResourceMetadata metadata;
	protected final RepositoryInvoker invoker;

	private ApplicationEventPublisher publisher;

	@Autowired
	private SelfLinkProvider selfLinkProvider;

	@Autowired
	private RepositoryInvokerFactory invokerFactory;

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
		this.metadata = mappings.getMetadataFor(persitentEntity.getType());
		invoker = invokerFactory.getInvokerFor(domainType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.ApplicationEventPublisherAware#
	 * setApplicationEventPublisher(org.springframework.context.
	 * ApplicationEventPublisher)
	 */
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@ResponseBody
	public CollectionModel<?> getCollectionResource(Pageable pageable, PersistentEntityResourceAssembler assembler)
			throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {

		if (null == invoker) {
			throw new ResourceNotFoundException();
		}

		Iterable<?> results = invoker.invokeFindAll(pageable);

		Optional<Link> baseLink = Optional.of(entityLinks.linkToPagedResource(domainType, pageable));

		CollectionModel<?> result = toCollectionModel(results, assembler, metadata.getDomainType(), baseLink);
		result.add(getCollectionResourceLinks());
		return result;
	}

	/**
	 * <code>GET /{repository}/{id}</code> - Returns a single entity.
	 *
	 * @param resourceInformation
	 * @param id
	 * @return
	 * @throws HttpRequestMethodNotSupportedException
	 */
	public <S extends T> ResponseEntity<EntityModel<S>> getItemResource(ID id,
			final PersistentEntityResourceAssembler assembler, @RequestHeader HttpHeaders headers)
			throws HttpRequestMethodNotSupportedException {

		return entityToResource(getItemResource(id), headers, assembler);
	}

	@ResponseBody
	public ResponseEntity<RepresentationModel<?>> postCollectionResource(RootResourceInformation resourceInformation,
			PersistentEntityResource payload, PersistentEntityResourceAssembler assembler,
			@RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
			throws HttpRequestMethodNotSupportedException {

		resourceInformation.verifySupportedMethod(HttpMethod.POST, ResourceType.COLLECTION);

		return createAndReturn(payload.getContent(), resourceInformation.getInvoker(), assembler,
				config.returnBodyOnCreate(acceptHeader));
	}

	private static Class<?> resolveTypeParameter(List<TypeInformation<?>> arguments, int index,
			Supplier<String> exceptionMessage) {

		if (arguments.size() <= index || arguments.get(index) == null) {
			throw new IllegalArgumentException(exceptionMessage.get());
		}

		return arguments.get(index).getType();
	}

	protected Links getCollectionResourceLinks() {

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
		return eTag.matches(entity, domainObject) || headersPreparer.isObjectStillValid(domainObject, requestHeaders)
				? false
				: true;
	}

	protected CollectionModel<?> entitiesToResource(Page<T> entities, Pageable pageable,
			PersistentEntityResourceAssembler assembler) {

		Optional<Link> baseLink = Optional.of(entityLinks.linkToPagedResource(metadata.getDomainType(), pageable));

		CollectionModel<?> result = toCollectionModel(entities, assembler, metadata.getDomainType(), baseLink);
		result.add(getCollectionResourceLinks());
		return result;
	}

	protected <S extends T> ResponseEntity<EntityModel<S>> entityToResource(Optional<T> entity, HttpHeaders headers,
			PersistentEntityResourceAssembler assembler) throws HttpRequestMethodNotSupportedException {
		return entity.map(it -> {

			if (isModified(headersPreparer, headers, it, persitentEntity)) {
				return new ResponseEntity<EntityModel<S>>((EntityModel<S>) assembler.toFullResource(it), headers,
						HttpStatus.OK);
			} else {
				return new ResponseEntity<EntityModel<S>>(headers, HttpStatus.NOT_MODIFIED);
			}
		}).orElseThrow(() -> new ResourceNotFoundException());
	}

	/**
	 * Triggers the creation of the domain object and renders it into the response
	 * if needed.
	 *
	 * @param domainObject
	 * @param invoker
	 * @return
	 */
	private ResponseEntity<RepresentationModel<?>> createAndReturn(Object domainObject, RepositoryInvoker invoker,
			PersistentEntityResourceAssembler assembler, boolean returnBody) {

		publisher.publishEvent(new BeforeCreateEvent(domainObject));
		Object savedObject = invoker.invokeSave(domainObject);
		publisher.publishEvent(new AfterCreateEvent(savedObject));

		Optional<PersistentEntityResource> resource = Optional
				.ofNullable(returnBody ? assembler.toFullResource(savedObject) : null);

		HttpHeaders headers = headersPreparer.prepareHeaders(resource);
		addLocationHeader(headers, assembler, savedObject);

		return ControllerUtils.toResponseEntity(HttpStatus.CREATED, headers, resource);
	}

	/**
	 * Sets the location header pointing to the resource representing the given
	 * instance. Will make sure we properly expand the URI template potentially
	 * created as self link.
	 *
	 * @param headers   must not be {@literal null}.
	 * @param assembler must not be {@literal null}.
	 * @param source    must not be {@literal null}.
	 */
	private void addLocationHeader(HttpHeaders headers, PersistentEntityResourceAssembler assembler, Object source) {

		String selfLink = selfLinkProvider.createSelfLinkFor(source).withSelfRel().expand().getHref();
		headers.setLocation(UriTemplate.of(selfLink).expand());
	}

	/**
	 * Returns the object backing the item resource for the given
	 * {@link RootResourceInformation} and id.
	 *
	 * @param resourceInformation
	 * @param id
	 * @return
	 * @throws HttpRequestMethodNotSupportedException
	 * @throws {@link                                 ResourceNotFoundException}
	 */
	private Optional<T> getItemResource(ID id)
			throws HttpRequestMethodNotSupportedException, ResourceNotFoundException {

		return invoker.invokeFindById(id);
	}
}
