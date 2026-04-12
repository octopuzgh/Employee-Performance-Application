package com.octopuz.platform.service.interf;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.octopuz.platform.dto.EmployeeExcel;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.vo.EmployeeVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService extends IService<Employee> {
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    EmployeeVO addEmployee(EmployeeVO employeeVO);

    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    void deleteEmployee(Integer id);

    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    EmployeeVO updateEmployee(EmployeeVO employeeVO);

    EmployeeVO getEmployeeById(Integer id);

    EmployeeVO getEmployeeByEmpNo(String empNo);

    EmployeeVO getEmployeeByName(String name);

    List<EmployeeVO> getAllEmployees();

    Page<EmployeeVO> pageEmployees(Integer pageNum, Integer pageSize, String department, String position);

    List<EmployeeVO> getEmployeesByDepartment(String department);

    List<EmployeeExcel> convertToExcelList(List<EmployeeVO> employees);

    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    String importExcel(MultipartFile file);
}
