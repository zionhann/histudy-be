package edu.handong.csee.histudy.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class WebController implements ErrorController {

    @GetMapping
    public String handleError() {
        return "index.html";
    }
}
