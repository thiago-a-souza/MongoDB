package com.thiago.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.thiago.model.Department;
import com.thiago.repository.DepartmentRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DepartmentRepositoryTests {
	
	@Autowired
	private DepartmentRepository repo;
	
	@Before
	public void init() {
		repo.deleteAll();
		repo.save(new Department(1, "IT"));
		repo.save(new Department(2, "HR"));
	}
	
	@Test
	public void count() {
		assertThat(repo.count(), is(2L));
	}

}
