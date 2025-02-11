package com.uniovi.controllers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Controller
public class CustomErrorController extends BasicErrorController {
    private static final String PATH = "error";
    @Autowired
    public CustomErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, serverProperties.getError(), errorViewResolvers);
    }

    @RequestMapping(value = "/error")
    public String error(Model model, HttpServletRequest webRequest) {
        Map<String, Object> errorAttributes = this.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        model.addAttribute(PATH, errorAttributes.get(PATH));
        model.addAttribute("message", errorAttributes.get("message"));
        model.addAttribute("status", errorAttributes.get("status"));
        model.addAttribute("trace", errorAttributes.get("trace")); // Add the stack trace
        return PATH;
    }
}
