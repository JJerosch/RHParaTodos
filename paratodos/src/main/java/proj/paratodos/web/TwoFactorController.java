package proj.paratodos.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proj.paratodos.security.RoleJwtSuccessHandler;
import proj.paratodos.security.TwoFactorLoginSuccessHandler;
import proj.paratodos.security.UserPrincipal;
import proj.paratodos.service.TwoFactorService;

@Controller
public class TwoFactorController {

    private final TwoFactorService twoFactorService;
    private final RoleJwtSuccessHandler roleJwtSuccessHandler;

    public TwoFactorController(TwoFactorService twoFactorService,
                               RoleJwtSuccessHandler roleJwtSuccessHandler) {
        this.twoFactorService = twoFactorService;
        this.roleJwtSuccessHandler = roleJwtSuccessHandler;
    }

    @PostMapping("/2fa")
    public String verify(@RequestParam("code") String code,
                         Authentication authentication,
                         HttpSession session,
                         HttpServletResponse response,
                         RedirectAttributes ra) {

        if (authentication == null) {
            return "redirect:/login";
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        boolean ok = twoFactorService.verify(principal, code);

        if (!ok) {
            ra.addFlashAttribute("error", "Código inválido ou expirado.");
            return "redirect:/2fa";
        }

        // Marca sessão como verificada e emite JWT
        session.setAttribute(TwoFactorLoginSuccessHandler.SESSION_2FA_OK, true);
        roleJwtSuccessHandler.issueJwtCookie(principal, response);

        return "redirect:" + roleJwtSuccessHandler.resolveRedirect(principal);
    }
}