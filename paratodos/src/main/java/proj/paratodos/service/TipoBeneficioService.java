package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.Cargo;
import proj.paratodos.domain.TipoBeneficio;
import proj.paratodos.dto.BeneficioStatsResponse;
import proj.paratodos.dto.TipoBeneficioRequest;
import proj.paratodos.dto.TipoBeneficioResponse;
import proj.paratodos.repository.CargoRepository;
import proj.paratodos.repository.TipoBeneficioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;

@Service
public class TipoBeneficioService {

    private final TipoBeneficioRepository tipoBeneficioRepository;
    private final CargoRepository cargoRepository;

    public TipoBeneficioService(TipoBeneficioRepository tipoBeneficioRepository,
                                 CargoRepository cargoRepository) {
        this.tipoBeneficioRepository = tipoBeneficioRepository;
        this.cargoRepository = cargoRepository;
    }

    public List<TipoBeneficioResponse> findAll() {
        return tipoBeneficioRepository.findAllByOrderByNomeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public TipoBeneficioResponse findById(Long id) {
        TipoBeneficio t = tipoBeneficioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de beneficio nao encontrado: " + id));
        return toResponse(t);
    }

    @Transactional
    public TipoBeneficioResponse create(TipoBeneficioRequest request) {
        validateNatureza(request);

        TipoBeneficio t = new TipoBeneficio();
        mapRequestToEntity(request, t);
        t = tipoBeneficioRepository.save(t);
        return toResponse(t);
    }

    @Transactional
    public TipoBeneficioResponse update(Long id, TipoBeneficioRequest request) {
        TipoBeneficio t = tipoBeneficioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de beneficio nao encontrado: " + id));

        validateNatureza(request);
        mapRequestToEntity(request, t);
        t = tipoBeneficioRepository.save(t);
        return toResponse(t);
    }

    @Transactional
    public void delete(Long id) {
        if (!tipoBeneficioRepository.existsById(id)) {
            throw new IllegalArgumentException("Tipo de beneficio nao encontrado: " + id);
        }
        tipoBeneficioRepository.deleteById(id);
    }

    public BeneficioStatsResponse getStats() {
        long tipos = tipoBeneficioRepository.count();
        long beneficiarios = tipoBeneficioRepository.countTotalBeneficiarios();
        BigDecimal custoMensal = tipoBeneficioRepository.sumCustoMensal();
        BigDecimal custoPorFunc = beneficiarios > 0
                ? custoMensal.divide(BigDecimal.valueOf(beneficiarios), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new BeneficioStatsResponse(tipos, beneficiarios, custoMensal, custoPorFunc);
    }

    private void validateNatureza(TipoBeneficioRequest req) {
        String nat = req.natureza();
        if (nat == null || (!nat.equals("PROVENTO") && !nat.equals("DESCONTO") && !nat.equals("INFORMATIVO"))) {
            throw new IllegalArgumentException("Natureza deve ser PROVENTO, DESCONTO ou INFORMATIVO");
        }
        // Check constraint: incidencias so permitidas para PROVENTO
        if (!"PROVENTO".equals(nat)) {
            if (Boolean.TRUE.equals(req.incideFerias()) || Boolean.TRUE.equals(req.incideDecimo())) {
                throw new IllegalArgumentException("Incidencia em ferias/decimo so e permitida para natureza PROVENTO");
            }
        }
    }

    private void mapRequestToEntity(TipoBeneficioRequest req, TipoBeneficio t) {
        t.setNome(req.nome());
        t.setDescricao(req.descricao());
        t.setPossuiDescontoFolha(req.possuiDescontoFolha() != null ? req.possuiDescontoFolha() : true);
        t.setValorPadrao(req.valorPadrao());
        t.setAtivo(req.ativo() != null ? req.ativo() : true);
        t.setNatureza(req.natureza());
        t.setIncideFerias(req.incideFerias() != null ? req.incideFerias() : false);
        t.setIncideDecimo(req.incideDecimo() != null ? req.incideDecimo() : false);

        // Vincular cargos
        if (req.cargoIds() != null) {
            List<Cargo> cargos = cargoRepository.findAllById(req.cargoIds());
            t.setCargos(new HashSet<>(cargos));
        } else {
            t.setCargos(new HashSet<>());
        }
    }

    private TipoBeneficioResponse toResponse(TipoBeneficio t) {
        long beneficiarios = tipoBeneficioRepository.countBeneficiariosByTipoId(t.getId());
        return TipoBeneficioResponse.fromEntity(t, beneficiarios);
    }
}
