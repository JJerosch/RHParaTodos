package proj.paratodos.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import proj.paratodos.security.UserPrincipal;

@Controller
public class PagesController {

    // ── Modelo auxiliar injetado nos templates ──────────────────────────────
    // Representação simples do usuário para o Thymeleaf / JavaScript
    public record CurrentUserDto(String email, String role) {}

    /** Injeta o usuário atual no modelo do Thymeleaf.
     *  Os templates usam: [[${currentUser.email}]] e [[${currentUser.role}]]
     */
    private void injectCurrentUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            model.addAttribute("currentUser",
                    new CurrentUserDto(principal.getUsername(), principal.getRole()));
        } else {
            // fallback (nunca deveria chegar aqui em rotas protegidas)
            model.addAttribute("currentUser", new CurrentUserDto("", "EMPLOYEE"));
        }
    }

    // ── Rotas públicas ──────────────────────────────────────────────────────

    @GetMapping("/")
    public String root() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth.getPrincipal() instanceof String)) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth.getPrincipal() instanceof String)) {
            return "redirect:/dashboard";
        }
        return "index"; // templates/index.html
    }

    @GetMapping("/2fa")
    public String twoFactorPage() {
        return "two-factor-auth";
    }

    // ── Rotas protegidas ────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        injectCurrentUser(model);
        return "dashboard";
    }

    @GetMapping("/employee-dashboard")
    public String employeeDashboardPage(Model model) {
        injectCurrentUser(model);
        return "employee-dashboard";
    }

    // ── Páginas futuras (placeholder — retornam dashboard até serem implementadas) ──

    @GetMapping("/employees")
    public String employeesPage(Model model) {
        injectCurrentUser(model);
        return "employees"; // templates/employees.html (a criar)
    }

    @GetMapping("/departments")
    public String departmentsPage(Model model) {
        injectCurrentUser(model);
        return "departments";
    }

    @GetMapping("/positions")
    public String positionsPage(Model model) {
        injectCurrentUser(model);
        return "positions";
    }

    @GetMapping("/recruitment")
    public String recruitmentPage(Model model) {
        injectCurrentUser(model);
        return "recruitment";
    }

    @GetMapping("/training")
    public String trainingPage(Model model) {
        injectCurrentUser(model);
        return "training";
    }

    @GetMapping("/performance")
    public String performancePage(Model model) {
        injectCurrentUser(model);
        return "performance";
    }

    @GetMapping("/payroll")
    public String payrollPage(Model model) {
        injectCurrentUser(model);
        return "payroll";
    }

    @GetMapping("/benefits")
    public String benefitsPage(Model model) {
        injectCurrentUser(model);
        return "benefits";
    }

    @GetMapping("/vacation")
    public String vacationPage(Model model) {
        injectCurrentUser(model);
        return "vacation";
    }

    @GetMapping("/timesheet")
    public String timesheetPage(Model model) {
        injectCurrentUser(model);
        return "timesheet";
    }

    @GetMapping("/reports")
    public String reportsPage(Model model) {
        injectCurrentUser(model);
        return "reports";
    }

    @GetMapping("/settings")
    public String settingsPage(Model model) {
        injectCurrentUser(model);
        return "settings";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        injectCurrentUser(model);
        return "profile";
    }

    @GetMapping("/payslips")
    public String payslipsPage(Model model) {
        injectCurrentUser(model);
        return "payslips";
    }
}