package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    /** 全件取得（削除フラグ除外、日付降順） */
    List<Report> findAllByOrderByReportDateDesc();

    /** 特定従業員の日報一覧（削除フラグ除外、日付降順） */
    List<Report> findByEmployeeOrderByReportDateDesc(Employee employee);

    /** 業務チェック：同一社員×同一日付（未削除）存在確認（新規用） */
    boolean existsByEmployeeAndReportDateAndDeleteFlgFalse(Employee employee, LocalDate reportDate);

    /** 業務チェック：同一社員×同一日付（未削除）存在確認（更新用：自分以外） */
    boolean existsByEmployeeAndReportDateAndIdNotAndDeleteFlgFalse(
            Employee employee, LocalDate reportDate, Integer id);
}
