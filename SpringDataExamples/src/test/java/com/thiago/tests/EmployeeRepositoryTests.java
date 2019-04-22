package com.thiago.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit4.SpringRunner;

import com.thiago.repository.EmployeeRepository;
import com.thiago.model.Address;
import com.thiago.model.Department;
import com.thiago.model.Employee;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeRepositoryTests {

	@Autowired
	private EmployeeRepository repo;

	@Before
	public void init() {
		repo.deleteAll();
		repo.save(new Employee("john", "john@email.com", 25, 1000.0, new Address("New York", "NY", "USA"), Arrays.asList(new String[] {"1111-1111", "2222-2222"}), new Department(1, "IT")));
		repo.save(new Employee("peter", "peter@email.com", 36, 2000.0, new Address("Los Angeles", "CA", "USA"), Arrays.asList(new String[] {"3333-3333"}), new Department(2, "HR")));
		repo.save(new Employee("alex", "alex@email.com", 36, 3000.0, new Address("Miami", "FL", "USA"), Arrays.asList(new String[] {"4444-4444"}), new Department(2, "HR")));
	}

	@Test
	public void count() {
		assertThat(repo.count(), is(3L));
	}

	@Test
	public void salaries() {
		double sum = 0.0;
		for (Employee e : repo.findAll())
			sum += e.getSalary();

		assertThat(sum, is(6000.0));
	}

	@Test
	public void findEmployeesOlderThan() {
		assertThat(repo.findEmployeesOlderThan(30), hasSize(2));
	}

	@Test(expected = DuplicateKeyException.class)
	public void duplicateEmail() {
		repo.save(new Employee("aaa", "email@email.com", 1, 2, null, null, null));
		repo.save(new Employee("bbb", "email@email.com", 3, 4, null, null, null));
	}

	@Test
	public void findByNameOrAgeCriteriaSearch() {
		assertThat(repo.findByNameOrAgeCriteriaSearch("alex", 25), hasSize(2));
	}
	
	@Test
	public void countByAge() {
		assertThat(repo.countByAge(36), is(2L));
	}
}
