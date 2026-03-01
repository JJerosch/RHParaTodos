package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.PromocaoRequest;
import proj.paratodos.dto.PromocaoResponse;
import proj.paratodos.security.UserPrincipal;
import proj.paratodos.service.PromocaoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
public class PromocaoController {

    private final PromocaoService promocaoService;

    public PromocaoController(PromocaoService promocaoService) {
        this.promocaoService = promocaoService;
    }

    @GetMapping
    public List<PromocaoResponse> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return promocaoService.findByStatus(status);
        }
        return promocaoService.findAll();
    }

    @GetMapping("/stats")
    public PromocaoService.PromocaoStatsResponse stats() {
        return promocaoService.getStats();
    }

    @GetMapping("/{id}")
    public PromocaoResponse getById(@PathVariable Long id) {
        return promocaoService.findById(id);
    }

    @PostMapping
    public ResponseEntity<PromocaoResponse> create(@Valid @RequestBody PromocaoRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        PromocaoResponse created = promocaoService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/approve")
    public PromocaoResponse approve(@PathVariable Long id,
                                    @RequestBody(required = false) Map<String, String> body,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        String observacao = body != null ? body.get("observacao") : null;
        return promocaoService.approve(id, principal.getId(), observacao);
    }

    @PutMapping("/{id}/reject")
    public PromocaoResponse reject(@PathVariable Long id,
                                   @RequestBody(required = false) Map<String, String> body,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        String observacao = body != null ? body.get("observacao") : null;
        return promocaoService.reject(id, principal.getId(), observacao);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
