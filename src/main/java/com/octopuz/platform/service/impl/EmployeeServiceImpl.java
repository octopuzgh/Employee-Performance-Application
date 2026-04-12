package com.octopuz.platform.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.converter.EmployeeConverter;
import com.octopuz.platform.dto.EmployeeExcel;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.listener.EmployeeExcelListener;
import com.octopuz.platform.mapper.EmployeeMapper;
import com.octopuz.platform.service.interf.EmployeeService;
import com.octopuz.platform.utils.KafkaSender;
import com.octopuz.platform.vo.EmployeeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private KafkaSender kafkaSender;
    @Resource
    private EmployeeConverter employeeConverter;

    @Override
    public EmployeeVO addEmployee(EmployeeVO employeeVO) {
        if (employeeVO.getId() != null) {
            throw new IllegalArgumentException("员工ID必须为空");
        }

        Employee employee = employeeConverter.toEntity(employeeVO);

        boolean saved = save(employee);
        if (!saved) {
            throw new RuntimeException("添加员工失败");
        }

        kafkaSender.sendOperationLog("CREATE_EMPLOYEE",
                String.format("员工%s创建成功,工号为%s", employee.getName(), employee.getEmpNo()));

        return employeeConverter.toVO(employee);
    }
    @Override
    public void deleteEmployee(Integer id) {
        Employee employee = getById(id);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }

        boolean removed = removeById(id);
        if (!removed) {
            throw new RuntimeException("删除员工失败");
        }

        kafkaSender.sendOperationLog("DELETE_EMPLOYEE",
                String.format("员工%s删除成功,工号为%s", employee.getName(), employee.getEmpNo()));
    }
    @Override
    public EmployeeVO updateEmployee(EmployeeVO employeeVO) {
        if (employeeVO.getId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }

        Employee existing = getById(employeeVO.getId());
        if (existing == null) {
            throw new IllegalArgumentException("员工不存在");
        }

        Employee employee = employeeConverter.toEntity(employeeVO);

        boolean updated = updateById(employee);
        if (!updated) {
            throw new RuntimeException("更新员工失败");
        }

        kafkaSender.sendOperationLog("UPDATE_EMPLOYEE",
                String.format("员工%s更新成功,工号为%s", employee.getName(), employee.getEmpNo()));

        return employeeConverter.toVO(employee);
    }
    @Override
    public EmployeeVO getEmployeeById(Integer id) {
        Employee employee = getById(id);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        return employeeConverter.toVO(employee);
    }
    @Override
    public EmployeeVO getEmployeeByEmpNo(String empNo) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getEmpNo, empNo);
        Employee employee = getOne(wrapper);

        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        return employeeConverter.toVO(employee);
    }


    @Override
    public EmployeeVO getEmployeeByName(String name) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getName, name);
        Employee employee = getOne(wrapper);

        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        return employeeConverter.toVO(employee);
    }
    @Override
    public List<EmployeeVO> searchEmployeesByName(String name) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Employee::getName, name.trim());
        List<Employee> employees = list(wrapper);
        return employeeConverter.toVOList(employees);
    }
    @Override
    public List<EmployeeVO> getAllEmployees() {
        List<Employee> employees = list();
        return employeeConverter.toVOList(employees);
    }
    @Override
    public Page<EmployeeVO> pageEmployees(Integer pageNum, Integer pageSize, String department, String position) {
        if (pageNum == null || pageNum <= 0) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (pageSize == null || pageSize <= 0) {
            throw new IllegalArgumentException("页大小必须大于0");
        }
        if (pageSize > 100) {
            throw new IllegalArgumentException("页大小不能超过100");
        }

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(department)) {
            wrapper.eq(Employee::getDepartment, department);
        }
        if (StringUtils.hasText(position)) {
            wrapper.eq(Employee::getPosition, position);
        }

        Page<Employee> page = new Page<>(pageNum, pageSize);
        Page<Employee> resultPage = page(page, wrapper);

        Page<EmployeeVO> voPage = new Page<>();
        voPage.setCurrent(resultPage.getCurrent());
        voPage.setSize(resultPage.getSize());
        voPage.setTotal(resultPage.getTotal());
        voPage.setRecords(employeeConverter.toVOList(resultPage.getRecords()));

        return voPage;
    }
    @Override
    public List<EmployeeVO> getEmployeesByDepartment(String department) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getDepartment, department);
        List<Employee> employees = list(wrapper);
        return employeeConverter.toVOList(employees);
    }




    @Override
    public List<EmployeeExcel> convertToExcelList(List<EmployeeVO> employees){
        if(employees==null) return List.of();
        return employees.stream().map(employee -> EmployeeExcel.builder()
                .empNo(employee.getEmpNo())
                .name(employee.getName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .email(employee.getEmail())
                .build()).toList();
    }

    @Override
    public String importExcel(MultipartFile file){
        try {
            if (file.isEmpty()) {
                return  "上传文件不能为空";
            }
            //生成锁key,使用uuid利于处理高并发
            String lockKey = "lock:import-excel-lock:employee";
            RLock lock = redissonClient.getLock(lockKey);
            boolean isLocked = lock.tryLock(0,-1, TimeUnit.SECONDS);
            if(!isLocked){
                return "请勿重复导入";
            }


            try {
                EmployeeExcelListener excelListener = new EmployeeExcelListener();
                EasyExcel.read(file.getInputStream(), EmployeeExcel.class, excelListener)
                        .sheet()
                        .doRead();
                //检查
                if (!excelListener.getErrorList().isEmpty()) {
                    return  "导入员工数据异常，请检查数据格式是否正确，并查看错误信息" + excelListener.getErrorList();
                }
                if (excelListener.getEmployees().isEmpty()) {
                    return  "导入员工数据异常，请检查数据格式是否正确";
                }
                //保存
                boolean saved = saveBatch(excelListener.getEmployees());
                if(!saved){
                    return "导入员工数据异常，请检查数据格式是否正确";
                } else {
                    int size = excelListener.getEmployees().size();
                    log.info("导入员工数据成功,共导入{}条", size);
                    kafkaSender.sendOperationLog("IMPORT_EXCEL_EMPLOYEES",
                            "导入员工数据成功,共导入"+size+"条");
                    return null;
                }
            } finally {
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("导入员工数据异常：{}", e.getMessage());
            return  "导入员工数据异常" + e.getMessage();
        }
    }
}
