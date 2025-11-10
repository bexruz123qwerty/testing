package com.onlineshop.test.service;

import com.onlineshop.test.dto.request.EmployeeRequest;
import com.onlineshop.test.dto.response.EmployeeResponse;
import com.onlineshop.test.entity.Department;
import com.onlineshop.test.entity.Employee;
import com.onlineshop.test.exception.EmployeeNotFoundException;
import com.onlineshop.test.mapper.EmployeeMapper;
import com.onlineshop.test.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeMapper employeeMapper;
    @Mock
    EmployeeRepository employeeRepository;
    @InjectMocks
    EmployeeService employeeService;

    Department department;
    Employee employee;
    EmployeeResponse response;
    EmployeeRequest request;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "IT", "Tashkent");
        employee = new Employee(1L, "Bexruz", "Java Developer", 3000L, department, null);
        response = new EmployeeResponse(1L, "Bexruz", "Java Developer", 3000L, "IT", null);

        request = new EmployeeRequest();
        request.setName("Bexruz");
        request.setPosition("Java Developer");
        request.setSalary(3000L);
        request.setDepartmentId(1L);
    }

    @Test
    void getAllEmployees_shouldReturnListOfResponses() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        var result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("Bexruz", result.get(0).name());
        verify(employeeRepository).findAll();
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    void getAllEmployees_shouldReturnEmptyList_whenNoEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of());
        var result = employeeService.getAllEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeById_shouldReturnResponse_whenEmployeeExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        var result = employeeService.getEmployeeById(1L);

        assertEquals("Bexruz", result.name());
        assertEquals("IT", result.departmentName());
    }

    @Test
    void getEmployeeById_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(99L));
    }

    @Test
    void createEmployee_shouldSaveAndReturnResponse() {
        when(employeeMapper.toEntity(request)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);
        when(employeeRepository.save(employee)).thenReturn(employee);

        var result = employeeService.createEmployee(request);

        assertNotNull(result);
        assertEquals("Bexruz", result.name());
        verify(employeeMapper).toEntity(request);
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_shouldUpdateExistingEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        var result = employeeService.updateEmployee(1L, request);

        assertEquals("Bexruz", result.name());
        assertEquals(3000L, result.salary());
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_shouldThrow_whenEmployeeNotFound() {
        when(employeeRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.updateEmployee(123L, request));
    }

    @Test
    void deleteEmployee_shouldDelete_whenExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void deleteEmployee_shouldThrow_whenNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(999L));
    }

    @Test
    void updateEmployee_shouldNotChangeDepartmentAndManager() {
        Employee manager = new Employee(2L, "Boss", "Manager", 5000L, department, null);
        employee.setManager(manager);
        request.setName("NewName");
        request.setPosition("NewPosition");
        request.setSalary(4000L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee))
                .thenReturn(new EmployeeResponse(1L, "NewName", "NewPosition", 4000L, "IT", "Boss"));

        var result = employeeService.updateEmployee(1L, request);

        assertEquals("Boss", result.managerName());
        verify(employeeRepository).save(employee);
    }
}