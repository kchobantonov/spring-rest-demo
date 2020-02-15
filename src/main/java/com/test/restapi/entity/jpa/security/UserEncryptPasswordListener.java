package com.test.restapi.entity.jpa.security;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserEncryptPasswordListener {
	public final static PasswordEncoder ENCODER = new BCryptPasswordEncoder(11);

	@PostLoad
	public void setPassword(User user) {
		user.setPassword(user.getRawpassword());
	}

	@PreUpdate
	@PrePersist
	public void encryptPassword(User user) {
		String password = user.getPassword();

		if (password != null) {
			user.setRawpassword(ENCODER.encode(password));
		}
	}
}
