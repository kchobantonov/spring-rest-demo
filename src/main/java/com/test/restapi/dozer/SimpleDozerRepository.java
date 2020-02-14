package com.test.restapi.dozer;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.util.Lazy;
import org.springframework.util.Assert;

import com.github.dozermapper.core.Mapper;
import com.github.dozermapper.core.MappingException;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class SimpleDozerRepository<T, ID> implements DozerRepositoryImplementation<T, ID>, BeanPostProcessor {

	private final DozerEntityInformation<T, ?> entityInformation;
	private final Mapper dozerMapper;

	private final Lazy<Optional<RepositoryInformation>> delegateRepositoryInformation;
	private final Lazy<Optional<PagingAndSortingRepository<Object, Object>>> delegateRepository;

	public SimpleDozerRepository(DozerEntityInformation<T, ?> entityInformation, Mapper dozerMapper,
			BeanFactory beanFactory) {

		Assert.notNull(entityInformation, "DozerEntityInformation must not be null!");
		Assert.notNull(dozerMapper, "Mapper must not be null!");
		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory, "beanFactory must be of type ListableBeanFactory!");

		this.entityInformation = entityInformation;
		this.dozerMapper = dozerMapper;

		this.delegateRepositoryInformation = Lazy.of(() -> beanFactory.getBeanProvider(Repositories.class)
				.getIfAvailable(() -> new Repositories((ListableBeanFactory) beanFactory))
				.getRepositoryInformationFor(entityInformation.getTargetEntityClass()));

		this.delegateRepository = Lazy.of(() -> beanFactory.getBeanProvider(Repositories.class)
				.getIfAvailable(() -> new Repositories((ListableBeanFactory) beanFactory))
				.getRepositoryFor(entityInformation.getTargetEntityClass())
				.map(r -> (PagingAndSortingRepository<Object, Object>) r));
	}

	private RepositoryInformation getDelegateRepositoryInformation() {
		return delegateRepositoryInformation.get().get();
	}

	private PagingAndSortingRepository<Object, Object> getDelegateRepository() {
		return delegateRepository.get().get();
	}

	@Override
	public void validateAfterRefresh() {
		try {
			getDelegateRepositoryInformation();
		} catch (NoSuchElementException e) {
			throw new IllegalStateException(
					"Unable to find repository information for " + entityInformation.getTargetEntityClass()
							+ " to support dozer entity " + entityInformation.getJavaType() + ". Validate annotation "
							+ DozerEntity.class + " attribute domainClass",
					e);
		}
		try {
			getDelegateRepository();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to find " + PagingAndSortingRepository.class + " repository for "
					+ entityInformation.getTargetEntityClass() + " to support dozer entity "
					+ entityInformation.getJavaType() + ". Validate annotation " + DozerEntity.class
					+ " attribute domainClass", e);
		}
	}

	@Override
	public Iterable<T> findAll(Sort sort) {
		Iterable<?> entities = getDelegateRepository().findAll(sort);

		Iterable<T> resources = Iterables.transform(entities,
				source -> dozerMapper.map(source, entityInformation.getJavaType()));

		return resources;
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		Page<?> entities = getDelegateRepository().findAll(pageable);

		Page<T> resources = entities.map(source -> dozerMapper.map(source, entityInformation.getJavaType()));

		return resources;
	}

	@Override
	public <S extends T> S save(S resource) {
		Object entity = dozerMapper.map(resource, entityInformation.getTargetEntityClass());

		getDelegateRepository().save(entity);

		return resource;
	}

	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> resources) {
		Iterable<?> entities = Iterables.transform(resources,
				source -> dozerMapper.map(source, entityInformation.getTargetEntityClass()));

		getDelegateRepository().saveAll(entities);

		return resources;
	}

	@Override
	public Optional<T> findById(ID resourceId) {
		Object entityId;
		try {
			entityId = dozerMapper.map(resourceId, getDelegateRepositoryInformation().getIdType());
		} catch (MappingException e) {
			throw new ResourceNotFoundException("Can't convert id", e);
		}

		Optional<Object> entity = getDelegateRepository().findById(entityId);

		return entity.map(source -> dozerMapper.map(source, entityInformation.getJavaType()));
	}

	@Override
	public boolean existsById(ID resourceId) {
		Object entityId;
		try {
			entityId = dozerMapper.map(resourceId, getDelegateRepositoryInformation().getIdType());
		} catch (MappingException e) {
			return false;
		}
		return getDelegateRepository().existsById(entityId);
	}

	@Override
	public Iterable<T> findAll() {
		Iterable<?> entities = getDelegateRepository().findAll();

		Iterable<T> resources = Iterables.transform(entities,
				source -> dozerMapper.map(source, entityInformation.getJavaType()));

		return resources;
	}

	@Override
	public Iterable<T> findAllById(Iterable<ID> resourceIds) {
		Iterable<Object> entityIds = Iterables.filter(Iterables.transform(resourceIds, source -> {
			try {
				return dozerMapper.map(source, getDelegateRepositoryInformation().getIdType());
			} catch (MappingException e) {
				return null;
			}
		}), Predicates.notNull());

		Iterable<Object> entities = getDelegateRepository().findAllById(entityIds);

		Iterable<T> resources = Iterables.transform(entities,
				source -> dozerMapper.map(source, entityInformation.getJavaType()));

		return resources;
	}

	@Override
	public long count() {
		return getDelegateRepository().count();
	}

	@Override
	public void deleteById(ID resourceId) {
		if (resourceId != null) {
			Object entityId;
			try {
				entityId = dozerMapper.map(resourceId, getDelegateRepositoryInformation().getIdType());
			} catch (MappingException e) {
				return;
			}

			if (entityId != null) {
				getDelegateRepository().deleteById(entityId);
			}
		}
	}

	@Override
	public void delete(T resource) {
		deleteById((ID) entityInformation.getId(resource));
	}

	@Override
	public void deleteAll(Iterable<? extends T> resources) {
		Iterable<Object> entityIds = Iterables.transform(resources, source -> dozerMapper
				.map(entityInformation.getId(source), getDelegateRepositoryInformation().getIdType()));

		for (Object entityId : entityIds) {
			if (entityId != null) {
				getDelegateRepository().deleteById(entityId);
			}
		}
	}

	@Override
	public void deleteAll() {
		getDelegateRepository().deleteAll();
	}

}
