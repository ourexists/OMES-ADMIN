package com.ourexists.mesedge.sync.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SyncPage {


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
