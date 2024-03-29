package com.test.restapi.controller;

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

import com.test.restapi.entity.dozer.employee.LimitedEmployeeResource;

/**
 * REST API endpoint to manage employee resources
 * 
 * @author kchobantonov
 */
@RepositoryRestController
public class EmployeeResourceController extends ResourceController<LimitedEmployeeResource, Integer> {
	
	@Override
	public String getIdPathVariableRegex() {
		return "[0-9]+";
	}
	
	/**
	 * Get employee resources
	 * 
	 * @return the employees collection model
	 * @throws HttpRequestMethodNotSupportedException
	 * @throws ResourceNotFoundException
	 */
	@PreAuthorize("isAuthenticated()")
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
	@PreAuthorize("isAuthenticated()")
	@Override
	public <S extends LimitedEmployeeResource> ResponseEntity<EntityModel<S>> getItemResource(@PathVariable("id") Integer id,
			PersistentEntityResourceAssembler assembler, @RequestHeader HttpHeaders headers)
			throws HttpRequestMethodNotSupportedException {
		return super.getItemResource(id, assembler, headers);
	}

	/*
	@Override
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<RepresentationModel<?>> postCollectionResource(
			@RequestBody @Valid EmployeeResource payload, PersistentEntityResourceAssembler assembler,
			@RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
			throws HttpRequestMethodNotSupportedException {
		return super.postCollectionResource(payload, assembler, acceptHeader);
	}*/
}
