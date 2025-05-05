package com.zluolan.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zluolan.reggie.common.R;
import com.zluolan.reggie.entity.Employee;
import com.zluolan.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
//controller 注解，自动生成对应的对象，并且将对象交给spring管理
//responseBody注解，表示将controller返回的数据封装到responseBody中，返回给客户端
@RestController
//请求映射注解，表示将当前类中的方法映射到url上
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee, HttpServletRequest request) {
        log.info("新增员工，员工信息：{}", employee.toString());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));// 设置初始密码123456，需要进行md5加密处理
//         employee.setCreateTime(LocalDateTime.now());// 设置创建时间
//         employee.setUpdateTime(LocalDateTime.now());// 设置更新时间
//         // 获得当前登录用户id，因为雪花算法，id是long类型，所以需要强转
//         Long empId = (Long) request.getSession().getAttribute("employee");
//         employee.setCreateUser(empId);
//         employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * alt+shift+左键 光标选中多行
     * await getMemberList(params).then(res => {
     * if (String(res.code) === '1') {
     * this.tableData = res.data.records || []
     * this.counts = res.data.total
     * }
     * 员工关管理分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name); // 如果name不为空，就添加过滤条件
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime); // 按照更新时间降序排序
        //执行查询
        employeeService.page(pageInfo, queryWrapper); // 分页查询，将查询结果封装到pageInfo中
        return R.success(pageInfo);
    }

    /**
     * 启动，禁用员工账号
     *
     * @param employee
     * @param request
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("当前线程id为：{}", id);

//         Long empId = (Long) request.getSession().getAttribute("employee");
//         employee.setUpdateTime(LocalDateTime.now()); // 设置更新时间
//         employee.setUpdateUser(empId); // 设置更新人
        employeeService.updateById(employee); // 更新员工信息
        return R.success("员工信息修改成功");
    }

    /**
     * 编辑员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }


}
