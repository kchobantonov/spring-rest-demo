package com.test.restapi.controller;

import static org.springframework.data.rest.webmvc.ControllerUtils.EMPTY_RESOURCE_LIST;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

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
import org.springframework.data.rest.core.event.AfterDeleteEvent;
import org.springframework.data.rest.core.event.AfterSaveEvent;
import org.springframework.data.rest.core.event.BeforeCreateEvent;
import org.springframework.data.rest.core.event.BeforeDeleteEvent;
import org.springframework.data.rest.core.event.BeforeSaveEvent;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
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
import org.springframework.data.rest.webmvc.support.ETagDoesntMatchException;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
public abstract class ResourceController<T, ID> implements ApplicationEventPublisherAware {
	private static final EmbeddedWrappers WRAPPERS = new EmbeddedWrappers(false);

	@Autowired
	private PagedResourcesAssembler<Object> pagedResourcesAssembler;

	private static final List<String> ACCEPT_PATCH_HEADERS = Arrays.asList(//
			RestMediaTypes.MERGE_PATCH_JSON.toString(), //
			RestMediaTypes.JSON_PATCH_JSON.toString(), //
			MediaType.APPLICATION_JSON_VALUE);

	protected static final String ACCEPT_HEADER = "Accept";
	private static final String LINK_HEADER = "Link";

	@Autowired
	private Repositories repositories;

	@Autowired
	private RepositoryEntityLinks entityLinks;

	@Autowired
	private RepositoryRestConfiguration config;

	@Autowired
	private HttpHeadersPreparer headersPreparer;

	@Autowired
	private ResourceMappings mappings;

	@Autowired
	private RepositoryInvokerFactory invokerFactory;

	private PersistentEntity<T, ?> persitentEntity;
	private Class<T> domainType;
	private ResourceMetadata metadata;
	private RepositoryInvoker invoker;

	private ApplicationEventPublisher publisher;

	@Autowired
	private SelfLinkProvider selfLinkProvider;

	@PostConstruct
	void init() {
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

	/**
	 * <code>GET /{repository}</code> - Returns the collection resource (paged or
	 * unpaged).
	 *
	 * @param pageable
	 * @param assembler
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws HttpRequestMethodNotSupportedException
	 */
	@ResponseBody
	protected CollectionModel<?> getCollectionResource(Pageable pageable, PersistentEntityResourceAssembler assembler)
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
	 * @param id
	 * @param assembler
	 * @param headers
	 * @return
	 * @throws HttpRequestMethodNotSupportedException
	 */
	protected <S extends T> ResponseEntity<EntityModel<S>> getItemResource(@PathVariable("id") ID id,
			final PersistentEntityResourceAssembler assembler, @RequestHeader HttpHeaders headers)
			throws HttpRequestMethodNotSupportedException {

		return entityToResource(getItemResource(id), headers, assembler);
	}

	/**
	 * <code>POST /{repository}</code> - Creates a new entity instances from the
	 * collection resource.
	 *
	 * @param payload
	 * @param assembler
	 * @param acceptHeader
	 * @return
	 * @throws HttpRequestMethodNotSupportedException
	 */
	@ResponseBody
	protected ResponseEntity<RepresentationModel<?>> postCollectionResource(@RequestBody @Valid T payload,
			PersistentEntityResourceAssembler assembler,
			@RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
			throws HttpRequestMethodNotSupportedException {

		return createAndReturn(payload, invoker, assembler, config.returnBodyOnCreate(acceptHeader));
	}

	/**
	 * <code>PUT /{repository}/{id}</code> - Updates an existing entity or creates
	 * one at exactly that place.
	 *
	 * @param payload
	 * @param id
	 * @param assembler
	 * @param eTag
	 * @param acceptHeader
	 * @return
	 * @throws HttpRequestMethodNotSupportedException
	 */
	protected ResponseEntity<? extends RepresentationModel<?>> putItemResource(@RequestBody @Valid T payload,
			@PathVariable("id") ID id, PersistentEntityResourceAssembler assembler, ETag eTag,
			@RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
			throws HttpRequestMethodNotSupportedException {

		eTag.verify(persitentEntity, payload);

		return saveAndReturn(payload, invoker, PUT, assembler, config.returnBodyOnUpdate(acceptHeader));
	}

	/**
	 * <code>PATCH /{repository}/{id}</code> - Updates an existing entity or creates
	 * one at exactly that place.
	 *
	 * @param payload
	 * @param id
	 * @param assembler
	 * @param eTag,
	 * @param acceptHeader
	 * @return
	 * @throws HttpRequestMethodNotSupportedException
	 * @throws ResourceNotFoundException
	 * @throws ETagDoesntMatchException
	 */
	protected ResponseEntity<RepresentationModel<?>> patchItemResource(
			@RequestBody @Valid T payload,
			@PathVariable("id") ID id, PersistentEntityResourceAssembler assembler,
			ETag eTag, @RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
			throws HttpRequestMethodNotSupportedException, ResourceNotFoundException {

		eTag.verify(persitentEntity, payload);

		return saveAndReturn(payload, invoker, PATCH, assembler,
				config.returnBodyOnUpdate(acceptHeader));
	}

	/**
	 * <code>DELETE /{repository}/{id}</code> - Deletes the entity backing the item
	 * resource.
	 *
	 * @param resourceInformation
	 * @param id
	 * @param eTag
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws HttpRequestMethodNotSupportedException
	 * @throws ETagDoesntMatchException
	 */
	protected ResponseEntity<?> deleteItemResource(@PathVariable("id") ID id, ETag eTag)
			throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {

		Optional<Object> domainObj = invoker.invokeFindById(id);

		return domainObj.map(it -> {

			eTag.verify(persitentEntity, it);

			publisher.publishEvent(new BeforeDeleteEvent(it));
			invoker.invokeDeleteById(persitentEntity.getIdentifierAccessor(it).getIdentifier());
			publisher.publishEvent(new AfterDeleteEvent(it));

			return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);

		}).orElseThrow(() -> new ResourceNotFoundException());
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

	private Link getDefaultSelfLink() {
		return new Link(ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString());
	}

	private boolean isModified(HttpHeadersPreparer headersPreparer, HttpHeaders requestHeaders, Object domainObject,
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
	
	/**
	 * Merges the given incoming object into the given domain object.
	 *
	 * @param domainObject
	 * @param invoker
	 * @param httpMethod
	 * @return
	 */
	private ResponseEntity<RepresentationModel<?>> saveAndReturn(Object domainObject, RepositoryInvoker invoker,
			HttpMethod httpMethod, PersistentEntityResourceAssembler assembler, boolean returnBody) {

		publisher.publishEvent(new BeforeSaveEvent(domainObject));
		Object obj = invoker.invokeSave(domainObject);
		publisher.publishEvent(new AfterSaveEvent(obj));

		PersistentEntityResource resource = assembler.toFullResource(obj);
		HttpHeaders headers = headersPreparer.prepareHeaders(Optional.of(resource));

		if (PUT.equals(httpMethod)) {
			addLocationHeader(headers, assembler, obj);
		}

		if (returnBody) {
			return ControllerUtils.toResponseEntity(HttpStatus.OK, headers, resource);
		} else {
			return ControllerUtils.toEmptyResponse(HttpStatus.NO_CONTENT, headers);
		}
	}

}
