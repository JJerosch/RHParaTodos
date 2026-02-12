package proj.paratodos.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import proj.paratodos.security.TwoFactorLoginSuccessHandler;

@Controller
public class AuthPagesController {

    @GetMapping("/")
    public String root(Authentication auth, HttpSession session) {
        if (auth == null) return "redirect:/login";
        // filtro vai mandar pra /2fa se precisar
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        // use o template que você quiser:
        // se seu login é o index.html, retorne "index"
        return "index";
    }

    @GetMapping("/2fa")
    public String twoFactor() {
        return "two-factor-auth";
    }
}
