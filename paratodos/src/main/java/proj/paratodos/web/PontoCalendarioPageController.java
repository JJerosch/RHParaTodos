package proj.paratodos.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import proj.paratodos.security.UserPrincipal;

import java.util.Map;

@Controller
public class PontoCalendarioPageController {

    @GetMapping("/admin/ponto-calendario")
    @PreAuthorize("hasRole('ADMIN')")
    public String pagina(Authentication authentication, Model model) {

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal user) {

            model.addAttribute("currentUser", Map.of(
                    "email", user.getUsername(),
                    "role", user.getAuthorities().iterator().next().getAuthority()
            ));

        }

        return "ponto-calendario";
    }
}