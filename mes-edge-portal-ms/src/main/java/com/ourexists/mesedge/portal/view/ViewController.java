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

    @RequestMapping("/view/material_tables")
    public String material_tables() {
        return "material_tables";
    }

    @RequestMapping("/view/material_classify_tables")
    public String material_classify_tables() {
        return "material_classify_tables";
    }

    @RequestMapping("/view/material_classify_form_edit")
    public String material_classify_form_edit() {
        return "material_classify_form_edit";
    }

    @RequestMapping("/view/material_form_edit")
    public String material_form_edit() {
        return "material_form_edit";
    }

    @RequestMapping("/view/mps_form_edit")
    public String mps_form_edit() {
        return "mps_form_edit";
    }

    @RequestMapping("/view/mps_tables")
    public String mps_tables() {
        return "mps_tables";
    }

    @RequestMapping("/view/mo_tables")
    public String mo_tables() {
        return "mo_tables";
    }

    @RequestMapping("/view/mo_form_edit")
    public String mo_form_edit() {
        return "mo_form_edit";
    }

    @RequestMapping("/view/bom_tree")
    public String bom_tree() {
        return "bom_tree";
    }

    @RequestMapping("/view/bom_form_edit")
    public String bom_form_edit() {
        return "bom_form_edit";
    }

    @RequestMapping("/view/bomc_form_edit")
    public String bomc_form_edit() {
        return "bomc_form_edit";
    }

    @RequestMapping("/view/mo_exec_edit")
    public String mo_exec_edit() {
        return "mo_exec_edit";
    }

    @RequestMapping("/view/mo_form_add")
    public String mo_form_add() {
        return "mo_form_add";
    }

    @RequestMapping("/view/line_tables")
    public String line_tables() {
        return "line_tables";
    }

    @RequestMapping("/view/line_down")
    public String line_down() {
        return "line_down";
    }

    @RequestMapping("/view/task_tables")
    public String task_tables() {
        return "task_tables";
    }

    @RequestMapping("/view/task_form_edit")
    public String task_form_edit() {
        return "task_form_edit";
    }

    @RequestMapping("/view/line_form_edit")
    public String line_form_edit() {
        return "line_form_edit";
    }

    @RequestMapping("/view/mps_queue")
    public String mps_queue() {
        return "mps_queue";
    }

    @RequestMapping("/view/line_flow")
    public String line_flow() {
        return "line_flow";
    }

    @RequestMapping("/view/qa_tables")
    public String qa_tables() {
        return "qa_tables";
    }

    @RequestMapping("/view/qa_form_edit")
    public String qa_form_edit() {
        return "qa_form_edit";
    }

    @RequestMapping("/view/line_flow_edit")
    public String line_flow_edit() {
        return "line_flow_edit";
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

    @RequestMapping("/view/devg_tables")
    public String devg_tables() {
        return "devg_tables";
    }

    @RequestMapping("/view/devg_form_edit")
    public String devg_form_edit() {
        return "devg_form_edit";
    }

    @RequestMapping("/view/devg_flow")
    public String devg_flow() {
        return "devg_flow";
    }

    @RequestMapping("/view/dev_form_edit")
    public String dev_form_edit() {
        return "dev_form_edit";
    }

    @RequestMapping("/view/matcount_tables")
    public String matcount_tables() {
        return "matcount_tables";
    }

    @RequestMapping("/view/produtcount_tables")
    public String produtcount_tables() {
        return "produtcount_tables";
    }

    @RequestMapping("/view/fzdata_tables")
    public String fzdata_tables() {
        return "fzdata_tables";
    }

    @RequestMapping("/view/fzdata_detail")
    public String fzdata_detail() {
        return "fzdata_detail";
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
}
