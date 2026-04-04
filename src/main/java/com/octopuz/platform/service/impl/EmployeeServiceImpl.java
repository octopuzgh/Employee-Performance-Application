package com.octopuz.platform.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private KafkaSender KafkaSender;

    @Override
    public Page<Employee> pageEmployees(Integer pageNum, Integer pageSize){
        return this.page(new Page<>(pageNum, pageSize));
    }

    @Override
    public List<Employee> getByDepartment(String department){
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getDepartment, department);
        return this.list(employeeLambdaQueryWrapper);

    }
    @Override
    public Employee getByName(String name){
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getName, name);
        return this.getOne(employeeLambdaQueryWrapper);

    }
    @Override
    public Employee getByEmpNo(String empNo){
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getEmpNo, empNo);
        return this.getOne(employeeLambdaQueryWrapper);


    }
    @Override
    public EmployeeVO convertToVO(Employee employee){
        if(employee==null) return null;
        return EmployeeVO.builder()
                .id(employee.getId())
                .empNo(employee.getEmpNo())
                .name(employee.getName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .email(employee.getEmail())
                .build();
    }
    @Override
    public  List<EmployeeVO> convertToVOList(List<Employee> employees){
        if(employees==null) return List.of();
        return employees.stream().map(this::convertToVO).toList();
    }
    @Override
    public Page<EmployeeVO> convertToVOPage(Page<Employee> page){
        if(page==null) return null;
        Page<EmployeeVO> employeeVOPage = new Page<>();
        employeeVOPage.setCurrent(page.getCurrent());
        employeeVOPage.setSize(page.getSize());
        employeeVOPage.setTotal(page.getTotal());
        employeeVOPage.setRecords(convertToVOList(page.getRecords()));
        return employeeVOPage;
    }
    @Override
    public List<EmployeeExcel> convertTOExcelList(List<Employee>  employees){
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
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    @Override
    public boolean save(Employee employee){
        if (employee == null) {
            return false;
        }
        boolean saved = super.save(employee);
        if(saved){
            KafkaSender.sendOperationLog("CREATE_EMPLOYEE",
                    String.format("员工%s创建成功,工号为%s", employee.getName(), employee.getEmpNo()));
        }
        return saved;
    }
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    @Override
    public boolean updateById(Employee employee){
        Employee oldEmployee = this.getById(employee.getId());
        if(oldEmployee==null) return false;

        boolean updated = super.updateById(employee);
        if(updated){
            KafkaSender.sendOperationLog("UPDATE_EMPLOYEE",
                    String.format("员工%s更新成功,工号为%s", employee.getName(), employee.getEmpNo()));
        }
        return updated;
    }
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    @Override
    public boolean removeById(Serializable id){
        Employee oldEmployee = this.getById(id);
        boolean removed = super.removeById(id);
        if(removed && oldEmployee!=null){
            KafkaSender.sendOperationLog("DELETE_EMPLOYEE",
                    String.format("员工%s删除成功,工号为%s", oldEmployee.getName(), oldEmployee.getEmpNo()));
        }
        return removed;
    }
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
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
                boolean saved = this.saveBatch(excelListener.getEmployees());
                if(!saved){
                    return "导入员工数据异常，请检查数据格式是否正确";
                } else {
                    int size = excelListener.getEmployees().size();
                    log.info("导入员工数据成功,共导入{}条", excelListener.getEmployees().size());
                    KafkaSender.sendOperationLog("IMPORT_EXCEL_EMPLOYEES",
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
