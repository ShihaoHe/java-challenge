package jp.co.axa.apidemo.controllers;

import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RESTful APIs to support operations on employees' information
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * retrieve all employees
     *
     * @return {@link List<Employee>}
     */
    @GetMapping("/employees")
    public List<Employee> getEmployees() {
        return employeeService.retrieveEmployees();
    }

    /**
     * retrieve certain employee by id
     *
     * @param employeeId {@link Long}
     * @return {@link Employee}
     */
    @GetMapping("/employees/{employeeId}")
    public Employee getEmployee(@PathVariable(name="employeeId")Long employeeId) {
        return employeeService.getEmployee(employeeId);
    }

    /**
     * persist information of certain employee
     *
     * @param employee {@link Employee}
     */
    @PostMapping("/employees")
    public void saveEmployee(@RequestBody Employee employee){
        employeeService.saveEmployee(employee);
        log.info("Employee Saved Successfully");
    }

    /**
     * delete certain employee
     *
     * @param employeeId {@link Long}
     */
    @DeleteMapping("/employees/{employeeId}")
    public void deleteEmployee(@PathVariable(name="employeeId")Long employeeId){
        employeeService.deleteEmployee(employeeId);
        log.info("Employee Deleted Successfully");
    }

    /**
     * update certain employee by id
     *
     * @param employee {@link Employee}
     * @param employeeId {@link Long}
     */
    @PutMapping("/employees/{employeeId}")
    public void updateEmployee(@RequestBody Employee employee,
                               @PathVariable(name="employeeId")Long employeeId){
        employee.setId(employeeId);
        employeeService.updateEmployee(employee);
    }

}
