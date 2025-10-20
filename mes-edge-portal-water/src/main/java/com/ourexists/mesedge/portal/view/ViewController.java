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
}
