package com.thiago.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.thiago.model.Employee;


public interface EmployeeRepository extends MongoRepository<Employee, String>, EmployeeCustomRepository {
	
	@Query(value = "{ age : { $gt : ?0 } }", sort = "{ name : 1 }")
	public List<Employee> findEmployeesOlderThan(int age);
	
	@Query(sort= "{ name : 1} ")
	public List<Employee> findByAge(int age, Pageable pageable);
	
	public long countByAge(int age);
	
	

}
