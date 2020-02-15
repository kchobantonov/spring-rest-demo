package com.test.restapi.repository.jpa.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.restapi.entity.jpa.employee.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

}
