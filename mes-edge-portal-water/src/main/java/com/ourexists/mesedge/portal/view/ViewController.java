/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/view/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/view/task_tables")
    public String task_tables() {
        return "task_tables";
    }

    @RequestMapping("/view/task_form_edit")
    public String task_form_edit() {
        return "task_form_edit";
    }

    @RequestMapping("/view/sync_tables")
    public String sync_tables() {
        return "sync_tables";
    }

    @RequestMapping("/view/sync_resource")
    public String sync_resource() {
        return "sync_resource";
    }

    @RequestMapping("/view/sync_form_edit")
    public String sync_form_edit() {
        return "sync_form_edit";
    }

    @RequestMapping("/view/report_datalist_tables")
    public String report_datalist_tables() {
        return "report_datalist_tables";
    }

    @RequestMapping("/view/report_mag_tables")
    public String report_mag_tables() {
        return "report_mag_tables";
    }

    @RequestMapping("/view/report_wps_tables")
    public String report_wps_tables() {
        return "report_wps_tables";
    }

    @RequestMapping("/view/report_dosing_tables")
    public String report_dosing_tables() {
        return "report_dosing_tables";
    }

    @RequestMapping("/view/report_od11_tables")
    public String report_od11_tables() {
        return "report_od11_tables";
    }

    @RequestMapping("/view/report_od12_tables")
    public String report_od12_tables() {
        return "report_od12_tables";
    }

    @RequestMapping("/view/report_od20_tables")
    public String report_od20_tables() {
        return "report_od20_tables";
    }

    @RequestMapping("/view/report_zs_tables")
    public String report_zs_tables() {
        return "report_zs_tables";
    }

    @RequestMapping("/view/account_tables")
    public String account_tables() {
        return "account_tables";
    }

    @RequestMapping("/view/platform_form_edit")
    public String platform_form_edit() {
        return "platform_form_edit";
    }

    @RequestMapping("/view/account_form_edit")
    public String account_form_edit() {
        return "account_form_edit";
    }

    @RequestMapping("/view/permission_tables")
    public String permission_tables() {
        return "permission_tables";
    }

    @RequestMapping("/view/permission_form_edit")
    public String permission_form_edit() {
        return "permission_form_edit";
    }

    @RequestMapping("/view/role_tables")
    public String role_tables() {
        return "role_tables";
    }

    @RequestMapping("/view/role_form_edit")
    public String role_form_edit() {
        return "role_form_edit";
    }

    @RequestMapping("/view/role_assign")
    public String role_assign() {
        return "role_assign";
    }

    @RequestMapping("/view/account_assign")
    public String account_assign() {
        return "account_assign";
    }
}
