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
        System.out.println("=== loginpage called ===");
        System.out.println("error: " + error);
        System.out.println("logout: " + logout);
        if (error != null && !error.isEmpty()) {
            System.out.println("Adding error message: " + error);
            model.addAttribute("errorMessage", error);
            model.addAttribute("status", "error");
        }
        if (logout != null && !logout.isEmpty()) {
            System.out.println("Adding logout message: " + logout);
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
