package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

import java.util.List;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 日報一覧 */
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetail user, Model model) {
        Employee loginUser = user.getEmployee();

        // ★修正：一覧取得の二重呼び出しをやめ、1回の結果を使い回す
        List<Report> list = reportService.listFor(loginUser);
        model.addAttribute("reportList", list);
        model.addAttribute("listSize", list.size());

        return "reports/list";
    }

    /** 日報詳細 */
    @GetMapping("/{id}/")
    public String detail(@PathVariable("id") Integer id,
                         @AuthenticationPrincipal UserDetail user,
                         Model model,
                         RedirectAttributes redirectAttrs) {

        Report report = reportService.findById(id);
        if (report == null) {
            // ★修正：存在しない場合は一覧へ戻す
            redirectAttrs.addFlashAttribute("detailError", "該当の日報が存在しません。");
            return "redirect:/reports";
        }

        // ★修正：権限チェック（一般ユーザーは自分のもの以外を見られない）
        boolean isAdmin = user.getEmployee().getRole().isAdmin();
        if (!isAdmin && !report.getEmployee().getCode().equals(user.getEmployee().getCode())) {
            redirectAttrs.addFlashAttribute("authorityError", "権限がありません。");
            return "redirect:/reports";
        }

        model.addAttribute("report", report);
        return "reports/detail";
    }

    /** 新規登録フォーム */
    @GetMapping("/add")
    public String addForm(@ModelAttribute Report report) {
        return "reports/new";
    }

    /** 新規登録処理 */
    @PostMapping("/add")
    public String add(@Validated @ModelAttribute Report report,
                      BindingResult res,
                      @AuthenticationPrincipal UserDetail user,
                      Model model) {

        if (res.hasErrors()) {
            return "reports/new";
        }

        ErrorKinds result = reportService.create(report, user.getEmployee());
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return "reports/new";
        }

        return "redirect:/reports";
    }

    /** 更新フォーム */
    @GetMapping("/{id}/update")
    public String editForm(@PathVariable("id") Integer id,
                           @AuthenticationPrincipal UserDetail user,
                           Model model,
                           RedirectAttributes redirectAttrs) {

        Report report = reportService.findById(id);
        if (report == null) {
            redirectAttrs.addFlashAttribute("updateError", "該当の日報が存在しません。");
            return "redirect:/reports";
        }

        // ★修正：フォーム表示時にも権限チェック
        boolean isAdmin = user.getEmployee().getRole().isAdmin();
        if (!isAdmin && !report.getEmployee().getCode().equals(user.getEmployee().getCode())) {
            redirectAttrs.addFlashAttribute("authorityError", "権限がありません。");
            return "redirect:/reports";
        }

        model.addAttribute("report", report);
        return "reports/edit";
    }

    /** 更新処理 */
    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id,
                         @Validated @ModelAttribute Report form,
                         BindingResult res,
                         @AuthenticationPrincipal UserDetail user,
                         Model model) {

        Report dbReport = reportService.findById(id);
        if (dbReport == null) {
            model.addAttribute("updateError", "該当の日報が存在しません。");
            return "reports/edit";
        }

        if (res.hasErrors()) {
            model.addAttribute("report", dbReport);
            return "reports/edit";
        }

        boolean isAdmin = user.getEmployee().getRole().isAdmin();
        ErrorKinds result = reportService.update(form, dbReport, isAdmin, user.getEmployee().getCode());
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", dbReport);
            return "reports/edit";
        }

        return "redirect:/reports/" + id + "/";
    }

    /** 削除処理（論理削除） */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id,
                         @AuthenticationPrincipal UserDetail user,
                         Model model) {

        boolean isAdmin = user.getEmployee().getRole().isAdmin();
        ErrorKinds result = reportService.delete(id, isAdmin, user.getEmployee().getCode());
        if (ErrorMessage.contains(result)) {
            model.addAttribute("deleteError", ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findById(id));
            return "reports/detail";
        }

        return "redirect:/reports";
    }
}
