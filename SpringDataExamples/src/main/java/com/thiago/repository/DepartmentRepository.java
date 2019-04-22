package com.thiago.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiago.model.Department;

public interface DepartmentRepository  extends MongoRepository<Department, Long>{

}
