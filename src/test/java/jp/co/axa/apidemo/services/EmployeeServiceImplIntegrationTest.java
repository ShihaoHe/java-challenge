package jp.co.axa.apidemo.services;

import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.repositories.EmployeeRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

/**
 * Integration test for cache
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeServiceImplIntegrationTest {

    @Autowired
    private CacheManager cacheManager;
    @MockBean
    private EmployeeRepository repository;
    @Autowired
    private EmployeeServiceImpl target;

    @After
    public void teardown() {
        Objects.requireNonNull(cacheManager.getCache("employees")).clear();
    }

    @Test
    public void test_retrieveEmployees() {
        // arrange
        final Employee employeeFirst = createEmployee("first name", "first department", 1000);
        final Employee employeeSecond = createEmployee("second name", "second department", 2000);
        when(repository.findAll()).thenReturn(Arrays.asList(employeeFirst, employeeSecond));

        // act
        target.retrieveEmployees();
        target.retrieveEmployees();

        // assert
        final Cache.ValueWrapper actual = getCache("all");
        assertThat(actual).isNotNull();
        assertThat(actual.get()).isInstanceOf(List.class);
        assertEmployeeIsExpected((Employee) ((List) actual.get()).get(0), employeeFirst);
        assertEmployeeIsExpected((Employee) ((List) actual.get()).get(1), employeeSecond);

        verify(repository, times(1)).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_getEmployee() {
        // arrange
        final Employee employee = createEmployee("first name", "first department", 1000);
        employee.setId(1L);
        when(repository.findById(eq(employee.getId()))).thenReturn(Optional.of(employee));

        // act
        target.getEmployee(employee.getId());
        target.getEmployee(employee.getId());

        // assert
        final Cache.ValueWrapper actual = getCache(employee.getId());
        assertThat(actual).isNotNull();
        assertThat(actual.get()).isInstanceOf(Employee.class);
        assertEmployeeIsExpected((Employee) actual.get(), employee);

        verify(repository, times(1)).findById(eq(employee.getId()));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_saveEmployee() {
        // arrange
        final Employee employeeFirst = createEmployee("first name", "first department", 1000);
        final Employee employeeSecond = createEmployee("second name", "second department", 2000);
        final Employee employeeCache = createEmployee("cache name", "cache department", 3000);

        final Employee savedEmployeeFirst = createEmployee("first name", "first department", 1000);
        savedEmployeeFirst.setId(1L);
        final Employee savedEmployeeSecond = createEmployee("second name", "second department", 2000);
        savedEmployeeSecond.setId(2L);

        when(repository.save(eq(employeeFirst))).thenReturn(savedEmployeeFirst);
        when(repository.save(employeeSecond)).thenReturn(savedEmployeeSecond);
        when(repository.findAll()).thenReturn(Collections.singletonList(employeeCache));

        // act
        target.retrieveEmployees();
        target.saveEmployee(employeeFirst);
        target.saveEmployee(employeeSecond);


        // assert
        final Cache.ValueWrapper actualFirst = getCache(savedEmployeeFirst.getId());
        assertThat(actualFirst).isNotNull();
        assertThat(actualFirst.get()).isInstanceOf(Employee.class);
        assertEmployeeIsExpected((Employee) actualFirst.get(), employeeFirst);

        final Cache.ValueWrapper actualSecond = getCache(savedEmployeeSecond.getId());
        assertThat(actualSecond).isNotNull();
        assertThat(actualSecond.get()).isInstanceOf(Employee.class);
        assertEmployeeIsExpected((Employee) actualSecond.get(), employeeSecond);

        assertThat(getCache("all")).isNull();

        verify(repository, times(1)).findAll();
        verify(repository, times(1)).save(eq(employeeFirst));
        verify(repository, times(1)).save(eq(employeeSecond));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_deleteEmployee() {
        // arrange
        final Employee employeeFirst = createEmployee("first name", "first department", 1000);
        employeeFirst.setId(1L);
        final Employee employeeSecond = createEmployee("second name", "second department", 2000);

        when(repository.findById(eq(employeeFirst.getId()))).thenReturn(Optional.of(employeeFirst));
        when(repository.findAll()).thenReturn(Collections.singletonList(employeeSecond));

        // act
        target.retrieveEmployees();
        target.getEmployee(employeeFirst.getId());
        target.deleteEmployee(employeeFirst.getId());

        // assert
        assertThat(getCache("all")).isNull();
        assertThat(getCache(employeeFirst.getId())).isNull();

        verify(repository, times(1)).findAll();
        verify(repository, times(1)).findById(employeeFirst.getId());
        verify(repository, times(1)).deleteById(employeeFirst.getId());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_updateEmployee() {
        // arrange
        final Employee employee = createEmployee("first name", "first department", 1000);
        employee.setId(1L);
        final Employee updatedEmployeeFirst = createEmployee("update first name", "update first department", 2000);
        updatedEmployeeFirst.setId(1L);
        final Employee updatedEmployeeSecond = createEmployee("update second name", "update second department", 3000);
        updatedEmployeeSecond.setId(1L);

        when(repository.findById(eq(employee.getId()))).thenReturn(Optional.of(employee));
        when(repository.findAll()).thenReturn(Collections.singletonList(employee));
        when(repository.save(any(Employee.class))).then(returnsFirstArg());

        // act
        target.retrieveEmployees();
        target.getEmployee(employee.getId());
        target.updateEmployee(updatedEmployeeFirst);
        target.updateEmployee(updatedEmployeeSecond);

        // assert
        final Cache.ValueWrapper actual = getCache(employee.getId());
        assertThat(actual).isNotNull();
        assertThat(actual.get()).isInstanceOf(Employee.class);
        assertEmployeeIsExpected((Employee) actual.get(), updatedEmployeeSecond);

        verify(repository, times(3)).findById(eq(employee.getId()));
        verify(repository, times(1)).findAll();
        verify(repository, times(1)).save(eq(updatedEmployeeFirst));
        verify(repository, times(1)).save(eq(updatedEmployeeSecond));
        verifyNoMoreInteractions(repository);
    }

    private Cache.ValueWrapper getCache(Object key) {
        return cacheManager.getCache("employees").get(key);
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