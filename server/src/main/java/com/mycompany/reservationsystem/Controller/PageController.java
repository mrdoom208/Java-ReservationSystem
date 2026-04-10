package com.mycompany.reservationsystem.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "Home";
    }

    @GetMapping("/loginpage")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("errorMessage", error);
            model.addAttribute("status", "error");
        }
        if (logout != null && !logout.isEmpty()) {
            model.addAttribute("errorMessage", logout);
            model.addAttribute("status", "success");
        }
        return "Login";
    }

    @GetMapping("/register")
    public String register() {
        return "Registration";
    }
}
