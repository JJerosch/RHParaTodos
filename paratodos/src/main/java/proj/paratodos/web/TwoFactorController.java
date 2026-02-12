package proj.paratodos.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proj.paratodos.security.UserPrincipal;
import proj.paratodos.security.TwoFactorLoginSuccessHandler;
import proj.paratodos.service.TwoFactorService;

@Controller
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    public TwoFactorController(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @PostMapping("/2fa")
    public String verify(@RequestParam("code") String code,
                         Authentication authentication,
                         HttpSession session,
                         RedirectAttributes ra) {

        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();

        boolean ok = twoFactorService.verify(p, code);
        if (!ok) {
            ra.addFlashAttribute("error", "Código inválido ou expirado.");
            return "redirect:/2fa";
        }

        session.setAttribute(TwoFactorLoginSuccessHandler.SESSION_2FA_OK, true);
        return "redirect:/dashboard";
    }
}

