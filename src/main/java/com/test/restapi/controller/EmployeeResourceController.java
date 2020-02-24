package com.test.restapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
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

import com.test.restapi.entity.dozer.employee.EmployeeResource;
import com.test.restapi.repository.dozer.employee.EmployeeResourceRepository;

/**
 * REST API endpoint to manage employee resources
 * 
 * @author kchobantonov
 */
@RepositoryRestController
@RequestMapping("employees")
public class EmployeeResourceController extends ResourceController<EmployeeResource, Integer> {

	@Autowired
	private EmployeeResourceRepository repository;
	
	/**
	 * Get employee resources
	 * 
	 * @return the employees collection model
	 * @throws HttpRequestMethodNotSupportedException
	 * @throws ResourceNotFoundException
	 */
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET)
	@Override
	public CollectionModel<?> getCollectionResource(Pageable pageable,
			final PersistentEntityResourceAssembler assembler)
			throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		return super.getCollectionResource(pageable, assembler);
	}

	/**
	 * Get employee resource specified by its id
	 * 
	 * @param id the employee id
	 * @return the employee that matches the request
	 */
	@RequestMapping(value = "/{id:[0-9]+}", method = RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	@Override
	public <S extends EmployeeResource> ResponseEntity<EntityModel<S>> getItemResource(@PathVariable("id") Integer id,
			PersistentEntityResourceAssembler assembler, @RequestHeader HttpHeaders headers)
			throws HttpRequestMethodNotSupportedException {
		return super.getItemResource(id, assembler, headers);
	}

}
