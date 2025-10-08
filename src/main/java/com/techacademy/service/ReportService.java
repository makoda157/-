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

    /** 一覧取得（権限別） */
    @Transactional(readOnly = true)
    public List<Report> listFor(Employee loginUser) {
        if (loginUser.getRole().isAdmin()) {
            return reportRepository.findAllByOrderByReportDateDesc();
        } else {
            return reportRepository.findByEmployeeOrderByReportDateDesc(loginUser);
        }
    }

    /** IDで1件取得 */
    @Transactional(readOnly = true)
    public Report findById(Integer id) {
        Optional<Report> opt = reportRepository.findById(id);
        return opt.orElse(null);
    }

    /** 新規登録 */
    @Transactional
    public ErrorKinds create(Report report, Employee owner) {
        try {
            report.setEmployee(owner);
            reportRepository.save(report);
            return ErrorKinds.SUCCESS;
        } catch (Exception e) {
            return ErrorKinds.DB_ACCESS_ERROR;
        }
    }

    /** 更新処理 */
    @Transactional
    public ErrorKinds update(Report src, Report dest, boolean isAdmin, String loginCode) {
        try {
            // 権限チェック：一般権限は自分の報告のみ更新可
            if (!isAdmin && !dest.getEmployee().getCode().equals(loginCode)) {
                return ErrorKinds.AUTHORITY_ERROR;
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

            // 権限チェック：一般は自分の報告のみ削除可
            if (!isAdmin && !report.getEmployee().getCode().equals(loginCode)) {
                return ErrorKinds.AUTHORITY_ERROR;
            }

            report.setDeleteFlg(true);
            reportRepository.save(report); // 永続化
            return ErrorKinds.SUCCESS;

        } catch (Exception e) {
            return ErrorKinds.DB_ACCESS_ERROR;
        }
    }

    /** 従業員削除時：関連日報も論理削除 */
    @Transactional
    public void deleteByEmployee(Employee employee) {
        List<Report> list = reportRepository.findByEmployeeOrderByReportDateDesc(employee);
        for (Report r : list) {
            r.setDeleteFlg(true);
            reportRepository.save(r);
        }
    }
}

