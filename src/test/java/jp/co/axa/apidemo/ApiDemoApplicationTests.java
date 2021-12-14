package jp.co.axa.apidemo;

import jp.co.axa.apidemo.controllers.EmployeeController;
import jp.co.axa.apidemo.repositories.EmployeeRepository;
import jp.co.axa.apidemo.services.EmployeeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * test for ensuring spring context loaded correctly
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiDemoApplicationTests {

	@Autowired
	private EmployeeController controller;
	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private EmployeeRepository repository;
	@Autowired
	private CacheManager cacheManager;

	@Test
	public void contextLoads() {
		assertThat(controller).isNotNull();
		assertThat(employeeService).isNotNull();
		assertThat(repository).isNotNull();
		assertThat(cacheManager).isNotNull();
	}
}
