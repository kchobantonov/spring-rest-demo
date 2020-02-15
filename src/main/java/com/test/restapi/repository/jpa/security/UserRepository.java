package com.test.restapi.repository.jpa.security;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.test.restapi.entity.jpa.security.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	@Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.username = :name")
	@Modifying
	@Transactional
	void updateLastLogin(@Param("name") String name, @Param("lastLogin") LocalDate lastLogin);

	Optional<User> findByUsername(String username);

}
