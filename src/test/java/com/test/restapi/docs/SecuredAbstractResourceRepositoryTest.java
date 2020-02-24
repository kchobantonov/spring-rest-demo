package com.test.restapi.docs;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class SecuredAbstractResourceRepositoryTest<T, ID> extends AbstractResourceRepositoryTest<T, ID> {
	private String username;
	private String password;

	public SecuredAbstractResourceRepositoryTest(String username, String password, Class<T> resourceClass, T resource) {
		super(resourceClass, resource);
		this.username = username;
		this.password = password;
	}

	@Override
	protected MockHttpServletRequestBuilder httpProfileResource() {
		return super.httpProfileResource().with(userToken(username, password));
	}

	@Override
	protected MockHttpServletRequestBuilder httpSearchResources() {
		return super.httpSearchResources().with(userToken(username, password));
	}

	@Override
	protected MockHttpServletRequestBuilder httpGetResources() {
		return super.httpGetResources().with(userToken(username, password));
	}

	@Override
	protected MockHttpServletRequestBuilder httpGetResource(ID id) {
		return super.httpGetResource(id).with(userToken(username, password));
	}

	@Override
	protected MockHttpServletRequestBuilder httpPatchResource(ID id) {
		return super.httpPatchResource(id).with(userToken(username, password));
	}

	@Override
	protected MockHttpServletRequestBuilder httpPostResource() {
		return super.httpPostResource().with(userToken(username, password));
	}

	@Override
	protected MockHttpServletRequestBuilder httpDeleteResource(ID id) {
		return super.httpDeleteResource(id).with(userToken(username, password));
	}
}
