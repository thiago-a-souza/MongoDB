package com.thiago.repository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.thiago.model.Employee;


import java.util.List;


public class EmployeeCustomRepositoryImpl implements EmployeeCustomRepository {
	@Autowired
	private MongoOperations mongo;

	public List<Employee> findByNameOrAgeCriteriaSearch(String name, int age) {
		Criteria criteria = new Criteria();
		criteria.orOperator(Criteria.where("name").is(name), Criteria.where("age").is(age));		
		Query query = Query.query(criteria)
				.with(new Sort(Sort.Direction.ASC, "name"));
		
		return mongo.find(query, Employee.class);
	}
}
