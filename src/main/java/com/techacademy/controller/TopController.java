package com.techacademy.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.techacademy.entity.Employee;
import com.techacademy.service.UserDetail;

@Controller
public class TopController {

    /** ログイン画面表示 */
    @GetMapping(value = "/login")
    public String login() {
        return "login/login";
    }

    /** ログイン後のトップページ表示（権限に応じて振り分け） */
    @GetMapping(value = "/")
    public String top(@AuthenticationPrincipal UserDetail userDetail) {
        // 未ログイン状態でアクセスした場合はログイン画面へ
        if (userDetail == null) {
            return "redirect:/login";
        }

        Employee employee = userDetail.getEmployee();

        // 管理者権限の場合 → 従業員一覧へ
        if ("ADMIN".equals(employee.getRole().toString())) {
            return "redirect:/employees";
        }

        // 一般権限の場合 → 日報一覧へ
        return "redirect:/reports";
    }
}
