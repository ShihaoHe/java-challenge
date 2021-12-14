package jp.co.axa.apidemo.services;

import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.exception.ResourceNotFoundException;
import jp.co.axa.apidemo.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * service layer to support CRUD for employees' information
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final String NOT_FOUND_MSG = "id: %d does not exist";

    private final EmployeeRepository employeeRepository;

    /**
     * retrieve all employees and cache them with key all
     *
     * @return {@link List<Employee>}
     */
    @Cacheable(value = "employees", key="'all'")
    public List<Employee> retrieveEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * retrieve one certain employee according to employee id
     * use employee id as cache key
     *
     * @param employeeId {@link Long}
     * @return {@link Employee} retrieve employee
     * @throws {@link ResourceNotFoundException} if employee does not exist
     */
    @Cacheable(value = "employees", key = "#employeeId")
    public Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(
                () -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, employeeId))
        );
    }

    /**
     * persist employee to DB, cache the result
     * cache key would be employee id which would be issued and returned after insert
     * since added new employee, it's necessary to invalidate cache for the result of retrieving all employees
     *
     * @param employee {@link Employee} to persist
     * @return {@link Employee} persisted one
     */
    @CachePut(value = "employees", key = "#result.id")
    @CacheEvict(value = "employees", key = "'all'")
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * delete certain employee according to employee id
     * since deleted certain employee, it's necessary to invalidate cache
     * for that certain one and the result of retrieving all employees
     *
     * @param employeeId {@link Long}
     */
    @Caching(evict = {
            @CacheEvict(value = "employees", key = "#employeeId"),
            @CacheEvict(value = "employees", key = "'all'")
    })
    public void deleteEmployee(Long employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    /**
     * update certain employee according to employee id and cache updated one
     * since updated certain employee, it's necessary to invalidate cache for retrieving all employees
     *
     * @param employee {@link Employee} to update
     * @return {@link Employee} updated one
     */
    @CachePut(value = "employees", key = "#result.id")
    @CacheEvict(value = "employees", key = "'all'")
    public Employee updateEmployee(Employee employee) {
        employeeRepository.findById(employee.getId()).orElseThrow(
                () -> new ResourceNotFoundException(String.format(NOT_FOUND_MSG, employee.getId()))
        );

        return employeeRepository.save(employee);
    }
}