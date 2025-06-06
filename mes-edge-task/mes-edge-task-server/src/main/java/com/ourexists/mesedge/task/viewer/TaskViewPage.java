/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.viewer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TaskViewPage {

    @RequestMapping("/view/task_tables")
    public String task_tables() {
        return "task_tables";
    }

    @RequestMapping("/view/task_form_edit")
    public String task_form_edit() {
        return "task_form_edit";
    }
}
