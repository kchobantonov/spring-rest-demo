package com.test.restapi.data.jpa;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.UuidGenerator;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@MappedSuperclass
@Converter(name = "uuid", converterClass = UUIDConverter.class)
@EqualsAndHashCode(of = "id")
@UuidGenerator(name = "uuid")
public abstract class AbstractEntity {
	@Id
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", updatable = false, nullable = false)
	@Convert("uuid")
	private UUID id;

}
