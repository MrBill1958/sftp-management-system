package com.nearstar.sftpmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";  // This will serve src/main/resources/templates/index.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // You'll need to create this template
    }
}