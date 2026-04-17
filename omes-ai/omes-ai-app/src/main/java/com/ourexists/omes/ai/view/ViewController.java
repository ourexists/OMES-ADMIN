package com.ourexists.omes.ai.view;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ViewController {

    private final ResourceLoader resourceLoader;
    private final String portalStaticBaseUrl;

    public ViewController(ResourceLoader resourceLoader,
                          @org.springframework.beans.factory.annotation.Value("${ai.portal-static-base-url:http://127.0.0.1:10010}") String portalStaticBaseUrl) {
        this.resourceLoader = resourceLoader;
        this.portalStaticBaseUrl = portalStaticBaseUrl == null ? "" : portalStaticBaseUrl.replaceAll("/+$", "");
    }

    @RequestMapping("/")
    public String index() {
        return "redirect:/view/inspect_ai_report";
    }

    @RequestMapping("/view/{viewName}")
    public String view(@PathVariable String viewName) {
        String templatePath = "classpath:/templates/" + viewName + ".html";
        if (resourceLoader.getResource(templatePath).exists()) {
            return viewName;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "View template not found: " + viewName);
    }

    @ModelAttribute("portalStaticBaseUrl")
    public String portalStaticBaseUrl() {
        return portalStaticBaseUrl;
    }
}
