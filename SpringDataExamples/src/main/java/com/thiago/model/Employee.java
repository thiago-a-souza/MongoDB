package com.thiago.model;


import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection="employees")
public class Employee {
	@Id
	private String id;
	private String name;
	
	@Indexed(unique=true)
	private String email;
	
	private int age;
	private double salary;
	
	@Field("addr")
	private Address address;
	
	private List<String> phones;
	
	@DBRef
	private Department department;
	
	public Employee() {
		
	}
	
	public Employee(String name, String email, int age, double salary, Address addr, List<String> phones, Department dept) {
		this.name = name;
		this.email = email;
		this.age = age;
		this.salary = salary;
		this.address = addr;
		this.phones = phones;
		this.department = dept;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public double getSalary() {
		return salary;
	}
	public void setSalary(double salary) {
		this.salary = salary;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	@Override
	public String toString() {
		return "name = " + name+
				", age = " + age +
				", salary = " + salary +
				", city = " + (address != null ? address.getCity() : "null") +
				", state = " + (address != null ? address.getState() : "null") +
				", country = " + (address != null ? address.getCountry() : "null") +
				", phones = " + phones + 
				", department = " + (department != null ? department.getName() : "null");
	}
}
