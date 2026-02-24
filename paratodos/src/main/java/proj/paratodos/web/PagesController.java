package proj.paratodos.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {

    @GetMapping("/")
    public String root(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) return "redirect:/dashboard";
        return "index";
    }

    @GetMapping("/2fa")
    public String twoFactor() {
        return "two-factor-auth";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/employee-dashboard")
    public String employeeDashboard() {
        return "employee-dashboard";
    }
}