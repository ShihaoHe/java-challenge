package jp.co.axa.apidemo.repositories;


import jp.co.axa.apidemo.entities.Employee;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for DB
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeRepositoryIntegrationTest {

    @Autowired
    private EmployeeRepository target;

    @After
    public void teardown() {
        target.deleteAll();
    }

    @Test
    public void test_retrieveEmployees() {
        // arrange
        final Employee expectedEmployeeFirst = createEmployee("first name", "first department", 1000);
        final Employee expectedEmployeeSecond = createEmployee("second name", "second department", 2000);
        target.save(expectedEmployeeFirst);
        target.save(expectedEmployeeSecond);

        // act
        final List<Employee> actual = target.findAll();

        // assert
        assertThat(actual).hasSize(2);
        assertEmployeeIsExpected(actual.get(0), expectedEmployeeFirst);
        assertEmployeeIsExpected(actual.get(1), expectedEmployeeSecond);
    }

    @Test
    public void test_getEmployee_validId() {
        // arrange
        final Employee expectedEmployee = createEmployee("first name", "first department", 1000);
        final Long id = target.save(expectedEmployee).getId();

        // act
        final Optional<Employee> actual = target.findById(id);

        // assert
        assertThat(actual).isPresent();
        assertEmployeeIsExpected(actual.get(), expectedEmployee);
    }

    public void test_getEmployee_inValidId() {
        // arrange
        final Employee expectedEmployee = createEmployee("first name", "first department", 1000);
        final Long id = target.save(expectedEmployee).getId();

        // act
        final Optional<Employee> actual = target.findById(id + 1);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    public void test_saveEmployee() {
        // arrange
        final Employee expectedEmployee = createEmployee("first name", "first department", 1000);

        // act
        final Employee savedEmployee = target.save(expectedEmployee);

        // assert
        final Optional<Employee> actual = target.findById(savedEmployee.getId());
        assertThat(actual).isNotEmpty();
        assertThat(savedEmployee).isNotNull();
        assertEmployeeIsExpected(actual.get(), savedEmployee);
    }

    @Test
    public void test_deleteEmployee() {
        // arrange
        final Employee expectedEmployee = createEmployee("first name", "first department", 1000);
        final Employee savedEmployee = target.save(expectedEmployee);

        // act
        target.deleteById(savedEmployee.getId());

        // assert
        assertThat(target.findById(savedEmployee.getId())).isEmpty();
    }


    @Test
    public void test_updateEmployee() {
        // arrange
        final Employee expectedEmployee = createEmployee("first name", "first department", 1000);
        final Employee updateEmployee = createEmployee("first name update", "first department update", 2000);
        final Employee savedEmployee = target.save(expectedEmployee);
        updateEmployee.setId(savedEmployee.getId());

        // act
        target.save(updateEmployee);

        // assert
        final Optional<Employee> actual = target.findById(savedEmployee.getId());
        assertThat(actual).isNotEmpty();
        assertEmployeeIsExpected(actual.get(), updateEmployee);
    }

    private void assertEmployeeIsExpected(Employee actual, Employee expectedEmployee) {
        assertThat(actual.getName()).isEqualTo(expectedEmployee.getName());
        assertThat(actual.getDepartment()).isEqualTo(expectedEmployee.getDepartment());
        assertThat(actual.getSalary()).isEqualTo(expectedEmployee.getSalary());
    }

    private Employee createEmployee(String name, String department, Integer salary) {
        final Employee employee = new Employee();
        employee.setName(name);
        employee.setDepartment(department);
        employee.setSalary(salary);

        return employee;
    }
}