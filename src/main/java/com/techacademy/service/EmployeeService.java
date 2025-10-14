package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportService reportService; // ★追加：関連日報の論理削除で使用

    public EmployeeService(EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder,
                           ReportService reportService) { // ★追加
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportService = reportService; // ★追加
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 従業員更新
    @Transactional
    public ErrorKinds update(Employee formEmployee) {

        // 既存データ取得
        Employee employee = findByCode(formEmployee.getCode());
        if (employee == null) {
            return ErrorKinds.DB_ACCESS_ERROR;
        }

        // 名前・権限を更新
        employee.setName(formEmployee.getName());
        employee.setRole(formEmployee.getRole());
        employee.setUpdatedAt(LocalDateTime.now());

        // パスワードが空欄でなければチェック・更新
        if (formEmployee.getPassword() != null && !formEmployee.getPassword().isEmpty()) {
            ErrorKinds result = employeePasswordCheck(formEmployee);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }
            // employeePasswordCheck 内で encode 済みの値を反映
            employee.setPassword(formEmployee.getPassword());
        }

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 従業員削除（論理削除）
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {

        // 自分を削除しようとした場合はエラー
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }

        Employee employee = findByCode(code);
        if (employee == null) {
            return ErrorKinds.DB_ACCESS_ERROR;
        }

        // 従業員の論理削除
        employee.setDeleteFlg(true);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee); // ★明示保存（可読性と安全性のため）

        // 関連日報も論理削除
        reportService.deleteByEmployee(employee); // ★追加呼び出し

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    @Transactional(readOnly = true)
    public Employee findByCode(String code) {
        Optional<Employee> option = employeeRepository.findById(code);
        return option.orElse(null);
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {

        // 半角英数字チェック
        if (isHalfSizeCheckError(employee)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 8文字～16文字チェック
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }
}
