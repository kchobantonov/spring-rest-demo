package com.test.restapi.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.webmvc.HttpHeadersPreparer;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.restapi.entity.dozer.employee.EmployeeResource;
import com.test.restapi.repository.dozer.employee.EmployeeResourceRepository;

/**
 * REST API endpoint to manage employee resources
 * 
 * @author kchobantonov
 */
@RepositoryRestController
@RequestMapping("employees")
public class EmployeeResourceController extends ResourceController<EmployeeResource> {

	@Autowired
	private EmployeeResourceRepository repository;

	public EmployeeResourceController(PagedResourcesAssembler<Object> pagedResourcesAssembler,
			Repositories repositories, RepositoryRestConfiguration config, RepositoryEntityLinks entityLinks,
			HttpHeadersPreparer headersPreparer, ResourceMappings mappings) {
		super(pagedResourcesAssembler, repositories, config, entityLinks, headersPreparer, mappings);
	}

	/**
	 * Get employee resources
	 * 
	 * @return the employees collection model
	 */
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public CollectionModel<?> getCollectionResource(final PersistentEntityResourceAssembler assembler,
			Pageable pageable) {
		Page<EmployeeResource> employees = repository.findAll(pageable);

		return entitiesToResource(employees, pageable, assembler);
	}

	/**
	 * Get employee resource specified by its id
	 * 
	 * @param id the employee id
	 * @return the employee that matches the request
	 */
	@RequestMapping(value = "/{id:[0-9]+}", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<EntityModel<EmployeeResource>> getItemResource(@PathVariable("id") Integer id,
			final PersistentEntityResourceAssembler assembler, @RequestHeader HttpHeaders headers)
			throws HttpRequestMethodNotSupportedException {

		Optional<EmployeeResource> employee = repository.findById(id);

		return entityToResource(employee, headers, assembler);
	}
}
