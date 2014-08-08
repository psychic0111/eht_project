package com.eht.testbase;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:test-spring-*.xml"}) 
public class TestBaseService {
	
	protected ApplicationContext ctx = null;
	
	protected WebClient client = null; 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		//ctx = new ClassPathXmlApplicationContext("applicationContext-client.xml");
       // client = ctx.getBean("webClient", WebClient.class);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test(){
	}
}
