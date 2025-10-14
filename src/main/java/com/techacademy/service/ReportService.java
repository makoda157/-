package com.techacademy.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /** 一覧取得（権限別、削除済み除外） */
    @Transactional(readOnly = true)
    public List<Report> listFor(Employee loginUser) {
        if (loginUser.getRole().isAdmin()) {
            return reportRepository.findByDeleteFlgFalseOrderByReportDateDesc();
        } else {
            return reportRepository.findByEmployeeAndDeleteFlgFalseOrderByReportDateDesc(loginUser);
        }
    }

    /** IDで1件取得（削除済みは除外） */
    @Transactional(readOnly = true)
    public Report findById(Integer id) {
        Optional<Report> opt = reportRepository.findById(id);
        Report report = opt.orElse(null);

        // 削除済みデータは非表示
        if (report != null && report.isDeleteFlg()) {
            return null;
        }

        return report;
    }

    /** 新規登録（同一社員×同一日付の重複禁止） */
    @Transactional
    public ErrorKinds create(Report report, Employee owner) {
        try {
            boolean exists = reportRepository.existsByEmployeeAndReportDateAndDeleteFlgFalse(owner, report.getReportDate());
            if (exists) {
                return ErrorKinds.DATECHECK_ERROR;
            }

            report.setEmployee(owner);
            report.setDeleteFlg(false);
            reportRepository.save(report);
            return ErrorKinds.SUCCESS;

        } catch (Exception e) {
            return ErrorKinds.DB_ACCESS_ERROR;
        }
    }

    /** 更新処理（同一社員×同一日付の重複禁止） */
    @Transactional
    public ErrorKinds update(Report src, Report dest, boolean isAdmin, String loginCode) {
        try {
            if (!isAdmin && !dest.getEmployee().getCode().equals(loginCode)) {
                return ErrorKinds.AUTHORITY_ERROR;
            }

            boolean exists = reportRepository.existsByEmployeeAndReportDateAndIdNotAndDeleteFlgFalse(
                    dest.getEmployee(), src.getReportDate(), dest.getId());
            if (exists) {
                return ErrorKinds.DATECHECK_ERROR;
            }

            dest.setReportDate(src.getReportDate());
            dest.setTitle(src.getTitle());
            dest.setContent(src.getContent());
            reportRepository.save(dest);
            return ErrorKinds.SUCCESS;

        } catch (Exception e) {
            return ErrorKinds.DB_ACCESS_ERROR;
        }
    }

    /** 論理削除 */
    @Transactional
    public ErrorKinds delete(Integer id, boolean isAdmin, String loginCode) {
        try {
            Report report = findById(id);
            if (report == null) {
                return ErrorKinds.DB_ACCESS_ERROR;
            }

            if (!isAdmin && !report.getEmployee().getCode().equals(loginCode)) {
                return ErrorKinds.AUTHORITY_ERROR;
            }

            report.setDeleteFlg(true);
            reportRepository.save(report);
            return ErrorKinds.SUCCESS;

        } catch (Exception e) {
            return ErrorKinds.DB_ACCESS_ERROR;
        }
    }

    /** 従業員削除時：関連日報も論理削除 */
    @Transactional
    public void deleteByEmployee(Employee employee) {
        List<Report> list = reportRepository.findByEmployeeAndDeleteFlgFalseOrderByReportDateDesc(employee);
        for (Report r : list) {
            r.setDeleteFlg(true);
            reportRepository.save(r);
        }
    }
}
