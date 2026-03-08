package proj.paratodos.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import proj.paratodos.dto.TimesheetAdminResponse;
import proj.paratodos.service.PontoService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/timesheet")
public class TimesheetAdminController {

    private final PontoService pontoService;

    public TimesheetAdminController(PontoService pontoService) {
        this.pontoService = pontoService;
    }

    @GetMapping
    public TimesheetAdminResponse listar(
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        LocalDate hoje = LocalDate.now();

        if (inicio == null) {
            inicio = hoje.withDayOfMonth(1);
        }

        if (fim == null) {
            fim = hoje;
        }

        return pontoService.listarTimesheetAdmin(funcionarioId, inicio, fim);
    }
}