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
    //分页查询
    Page<Employee> pageEmployees(Integer pageNum, Integer pageSize);
    //按部门查询
    List<Employee> getByDepartment(String department);

    Employee getByName(String name);

    //按工号查询
    Employee getByEmpNo(String empNo);
    //转换为VO
    EmployeeVO convertToVO(Employee employee);
    List<EmployeeVO> convertToVOList(List<Employee> employees);
    Page<EmployeeVO> convertToVOPage(Page<Employee> page);

    List<EmployeeExcel> convertTOExcelList(List<Employee> employees);

    @CacheEvict(value = {"analysis:rank","analysis:dept-avg","analysis:company-avg"}, allEntries = true)
    String importExcel(MultipartFile file);
}
