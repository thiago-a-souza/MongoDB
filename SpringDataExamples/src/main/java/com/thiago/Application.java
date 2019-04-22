package com.thiago;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.mongodb.core.MongoOperations;

import com.thiago.model.Address;
import com.thiago.model.Department;
import com.thiago.model.Employee;
import com.thiago.repository.DepartmentRepository;
import com.thiago.repository.EmployeeRepository;

@SpringBootApplication
public class Application implements CommandLineRunner {
	
	@Autowired
	MongoOperations mongo;
	
	@Autowired
	private EmployeeRepository employeeRepo;
	
	@Autowired
	private DepartmentRepository departmentRepo;
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		departmentRepo.deleteAll();
		departmentRepo.save(new Department(1, "IT"));
		departmentRepo.save(new Department(2, "HR"));

		employeeRepo.deleteAll();
		employeeRepo.save(new Employee("john", "john@email.com", 25, 1000.0, new Address("New York", "NY", "USA"), Arrays.asList(new String[] {"1111-1111", "2222-2222"}), new Department(1, "IT")));
		employeeRepo.save(new Employee("peter", "peter@email.com", 36, 2000.0, new Address("Los Angeles", "CA", "USA"), Arrays.asList(new String[] {"3333-3333"}), new Department(2, "HR")));
		employeeRepo.save(new Employee("alex", "alex@email.com", 36, 3000.0, new Address("Miami", "FL", "USA"), Arrays.asList(new String[] {"4444-4444"}), new Department(2, "HR")));

		List<Employee> list = employeeRepo.findAll();
		for(Employee e : list)
			System.out.println(e);
	}

}