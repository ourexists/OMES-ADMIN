/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CorePage {
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/view/login")
    public String login() {
        return "login";
    }
}
