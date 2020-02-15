package com.test.restapi.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.test.restapi.entity.jpa.security.User;
import com.test.restapi.repository.jpa.security.UserRepository;

@Component
public class RestApiUserDetailsService implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final Optional<User> user = userRepository.findByUsername(username);

		return user.map(it -> {
			return org.springframework.security.core.userdetails.User.builder().username(it.getUsername())
					.password(it.getPassword()).roles("USER").build();
		}).orElseThrow(() -> new UsernameNotFoundException(
				this.messages.getMessage("JdbcDaoImpl.notFound", new Object[] { username }, "Username {0} not found")));
	}
}
