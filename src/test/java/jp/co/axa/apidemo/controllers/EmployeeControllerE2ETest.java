package jp.co.axa.apidemo.controllers;

import io.restassured.http.ContentType;
import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.repositories.EmployeeRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * e2e test for RESTful API
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerE2ETest {

    private static final String URL_RESOURCE = "http://localhost:%d/api/v1%s";
    private static final String URL_AUTHORIZATION = "http://localhost:%d/oauth/token";

    @Value("${customized.credential.clientId}")
    private String clientId;
    @Value("${customized.credential.clientSecret}")
    private String clientSecret;
    @Value("${customized.credential.username}")
    private String username;
    @Value("${customized.credential.password}")
    private String password;

    @Autowired
    private EmployeeRepository repository;
    @Autowired
    private CacheManager cacheManager;

    @LocalServerPort
    private int port;

    @After
    public void teardown() {
        repository.deleteAll();
        cacheManager.getCache("employees").clear();
    }

    @Test
    public void test_getEmployees() {
        // arrange
        final Employee employeeFirst = createEmployee("first name", "first department", 1000);
        final Employee employeeSecond = createEmployee("second name", "second department", 2000);
        final Employee savedEmployeeFirst = repository.save(employeeFirst);
        final Employee savedEmployeeSecond = repository.save(employeeSecond);

        final String accessToken = getCredentials();

       final Cache.ValueWrapper t =  cacheManager.getCache("employees").get("all");

        // act && assert
        given()
                .header("Authorization", "bearer " + accessToken).when()
                .get(String.format(URL_RESOURCE, port, "/employees")).then()
                .statusCode(200)
                .body(
                        "id", hasItems(savedEmployeeFirst.getId().intValue(), savedEmployeeSecond.getId().intValue()),
                        "name", hasItems("first name", "second name"),
                        "department", hasItems("first department", "second department"),
                        "salary", hasItems(1000, 2000)
                );
    }

    @Test
    public void test_getEmployee() {
        // arrange
        final Employee employee = createEmployee("first name", "first department", 1000);
        final Employee savedEmployee = repository.save(employee);

        final String accessToken = getCredentials();

        // act && assert
        given()
                .header("Authorization", "bearer " + accessToken).when()
                .get(String.format(URL_RESOURCE, port, "/employees/" + savedEmployee.getId())).then()
                .statusCode(200)
                .body(
                        "id", equalTo(savedEmployee.getId().intValue()),
                        "name", equalTo("first name"),
                        "department", equalTo("first department"),
                        "salary", equalTo(1000)
                );
    }

    @Test
    public void test_saveEmployee() {
        // arrange
        final String accessToken = getCredentials();
        final String requestBody =
                "{" +
                "  \"name\": \"first name\"," +
                "  \"department\": \"first department\"," +
                "  \"salary\": 1000\n" +
                "}";

        // act && assert
        given()
                .header("Authorization", "bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(requestBody).when()
                .post(String.format(URL_RESOURCE, port, "/employees")).then()
                .statusCode(200);

        given()
                .header("Authorization", "bearer " + accessToken).when()
                .get(String.format(URL_RESOURCE, port, "/employees/")).then()
                .statusCode(200)
                .body(
                        "name", hasItems("first name"),
                        "department", hasItems("first department"),
                        "salary", hasItems(1000)
                );

    }

    @Test
    public void test_deleteEmployee() {
        // arrange
        final Employee employee = createEmployee("first name", "first department", 1000);
        final Employee savedEmployee = repository.save(employee);

        final String accessToken = getCredentials();

        // act && assert
        given()
                .header("Authorization", "bearer " + accessToken).when()
                .delete(String.format(URL_RESOURCE, port, "/employees/" + savedEmployee.getId())).then()
                .statusCode(200);

        given()
                .header("Authorization", "bearer " + accessToken).when()
                .get(String.format(URL_RESOURCE, port, "/employees/")).then()
                .statusCode(200)
                .body("isEmpty()", is(true));
    }

    @Test
    public void test_updateEmployee() {
        // arrange
        final Employee employee = createEmployee("first name", "first department", 1000);
        final Employee savedEmployee = repository.save(employee);
        final String accessToken = getCredentials();
        final String requestBody =
                "{" +
                        "  \"name\": \"first name update\"," +
                        "  \"department\": \"first department update\"," +
                        "  \"salary\": 2000\n" +
                        "}";

        // act && assert
        given()
                .header("Authorization", "bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(requestBody).when()
                .put(String.format(URL_RESOURCE, port, "/employees/" + savedEmployee.getId())).then()
                .statusCode(200);

        given()
                .header("Authorization", "bearer " + accessToken).when()
                .get(String.format(URL_RESOURCE, port, "/employees/")).then()
                .statusCode(200)
                .body(
                        "name", hasItems("first name update"),
                        "department", hasItems("first department update"),
                        "salary", hasItems(2000)
                );

    }

    private String getCredentials() {
        final String authorization = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        return given()
                .auth()
                .preemptive().basic(clientId, clientSecret)
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("username", username)
                .formParam("password", password)
                .post(String.format(URL_AUTHORIZATION, port))
                .then().statusCode(200)
                .extract().response().jsonPath().get("access_token");
    }


    private Employee createEmployee(String name, String department, Integer salary) {
        final Employee employee = new Employee();
        employee.setName(name);
        employee.setDepartment(department);
        employee.setSalary(salary);

        return employee;
    }
}