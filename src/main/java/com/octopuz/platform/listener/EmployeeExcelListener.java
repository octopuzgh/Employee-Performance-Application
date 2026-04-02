package com.octopuz.platform.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.octopuz.platform.dto.EmployeeExcel;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.service.impl.EmployeeServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;

import java.util.ArrayList;

@Slf4j
@Data
@AllArgsConstructor

public class EmployeeExcelListener extends AnalysisEventListener<EmployeeExcel> {

    private final EmployeeServiceImpl employeeService;
    private final ArrayList<Employee> employees = new ArrayList<>();
    private final ArrayList<String> errorList = new ArrayList<>();

    @Override
    public void invoke(EmployeeExcel data, AnalysisContext context) {
        try{
            //校验数据
            if (data.getEmpNo() == null || data.getEmpNo().isEmpty()) {
                errorList.add("第"+(context.readRowHolder().getRowIndex()+1)+"行数据有误，请检查数据！");
                return;
            } else if (data.getName() == null || data.getName().isEmpty()) {
                errorList.add("第"+(context.readRowHolder().getRowIndex()+1)+"行数据有误，请检查数据！");
                return;
            }
            Employee employee = Employee.builder()
                    .empNo(data.getEmpNo())
                    .name(data.getName())
                    .department(data.getDepartment())
                    .position(data.getPosition())
                    .hireDate(data.getHireDate())
                    .email(data.getEmail())
                    .build();
            employees.add(employee);

        }catch (Exception e){
            log.error("数据导入失败：{}", e.getMessage());
        }
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完毕，共解析{}条数据",employees.size());

    }
    @CacheEvict(value = {"analysis:rank","analysis:dept-avg","analysis:company-avg"}, allEntries = true)
    public boolean saveEmployees() {
        log.info("开始保存数据");
        if(employees.isEmpty()){
            return false;
        }
        return employeeService.saveOrUpdateBatch(employees);
    }
}
