package com.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:Spring-Module.xml")
public class InsertTest {

	@org.junit.Test(timeout = 1000)
	public void test() {
		
	}
}
