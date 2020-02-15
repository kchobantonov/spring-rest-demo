package com.test.restapi.repository.jpa.security;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.restapi.entity.jpa.security.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

}
