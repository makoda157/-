package com.techacademy.repository;

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
}
