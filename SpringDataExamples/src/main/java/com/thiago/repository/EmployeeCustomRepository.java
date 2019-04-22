package com.thiago.repository;

import java.util.List;

import com.thiago.model.Employee;

public interface EmployeeCustomRepository {

	List<Employee> findByNameOrAgeCriteriaSearch(String name, int age);
}
