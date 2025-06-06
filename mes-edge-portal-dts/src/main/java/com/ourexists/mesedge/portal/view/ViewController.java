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
}
