package com.test.restapi.data.jpa;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Converter(name = "uuid", converterClass = UUIDConverter.class)
@EntityListeners(UserEncryptPasswordListener.class)
public class User {

	@Id
	@UuidGenerator(name = "uuid")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", updatable = false, nullable = false)
	@Convert("uuid")
	private UUID id;

	@Basic
	@Column(unique = true, name = "USERNAME")
	private String username;

	@Basic
	@Column(name = "PASSWORD")
	@Setter(AccessLevel.MODULE)
	@Getter(AccessLevel.MODULE)
	private String rawpassword;

	@Transient
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;

	@Basic
	private LocalDate lastLogin;

	@Basic
	@NotNull
	@Builder.Default
	private Boolean enabled = Boolean.TRUE;

}
