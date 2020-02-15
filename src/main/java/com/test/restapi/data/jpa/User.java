package com.test.restapi.data.jpa;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

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
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(UserEncryptPasswordListener.class)
public class User extends AbstractEntity {

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
