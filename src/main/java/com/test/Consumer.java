package com.test;

public class Consumer {
	long id;

	String name;
	int age;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Consumer(String name, int age) {
		super();
		this.name = name;
		this.age = age;
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

}
