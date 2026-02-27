package proj.paratodos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.Cargo;
import proj.paratodos.domain.Departamento;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.dto.FuncionarioRequest;
import proj.paratodos.dto.FuncionarioResponse;
import proj.paratodos.dto.FuncionarioStatsResponse;
import proj.paratodos.repository.CargoRepository;
import proj.paratodos.repository.DepartamentoRepository;
import proj.paratodos.repository.FuncionarioRepository;

import java.time.LocalDate;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              DepartamentoRepository departamentoRepository,
                              CargoRepository cargoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.departamentoRepository = departamentoRepository;
        this.cargoRepository = cargoRepository;
    }

    public Page<FuncionarioResponse> search(String search, Long departamentoId, String status, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        String statusParam = (status != null && !status.isBlank()) ? status.trim() : null;

        return funcionarioRepository.search(searchParam, departamentoId, statusParam, pageable)
                .map(FuncionarioResponse::fromEntity);
    }

    public FuncionarioResponse findById(Long id) {
        Funcionario f = funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario nao encontrado: " + id));
        return FuncionarioResponse.fromEntity(f);
    }

    @Transactional
    public FuncionarioResponse create(FuncionarioRequest request) {
        validateUniqueness(request, null);

        Funcionario f = new Funcionario();
        mapRequestToEntity(request, f);

        f = funcionarioRepository.save(f);
        return FuncionarioResponse.fromEntity(f);
    }

    @Transactional
    public FuncionarioResponse update(Long id, FuncionarioRequest request) {
        Funcionario f = funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario nao encontrado: " + id));

        validateUniqueness(request, id);
        mapRequestToEntity(request, f);

        f = funcionarioRepository.save(f);
        return FuncionarioResponse.fromEntity(f);
    }

    @Transactional
    public void delete(Long id) {
        if (!funcionarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Funcionario nao encontrado: " + id);
        }
        funcionarioRepository.deleteById(id);
    }

    public FuncionarioStatsResponse getStats() {
        long total = funcionarioRepository.count();
        long ativos = funcionarioRepository.countByStatus("ATIVO");
        long ferias = funcionarioRepository.countByStatus("FERIAS");
        long desligadosMes = funcionarioRepository.countDesligadosDesde(
                LocalDate.now().withDayOfMonth(1));

        return new FuncionarioStatsResponse(total, ativos, ferias, desligadosMes);
    }

    private void validateUniqueness(FuncionarioRequest request, Long excludeId) {
        if (excludeId == null) {
            // Criacao: verifica se ja existe
            if (funcionarioRepository.existsByCpf(request.cpf())) {
                throw new IllegalArgumentException("CPF ja cadastrado: " + request.cpf());
            }
            if (funcionarioRepository.existsByMatricula(request.matricula())) {
                throw new IllegalArgumentException("Matricula ja cadastrada: " + request.matricula());
            }
            if (request.emailCorporativo() != null && !request.emailCorporativo().isBlank()
                    && funcionarioRepository.existsByEmailCorporativo(request.emailCorporativo())) {
                throw new IllegalArgumentException("Email corporativo ja cadastrado: " + request.emailCorporativo());
            }
        }
    }

    private void mapRequestToEntity(FuncionarioRequest req, Funcionario f) {
        f.setMatricula(req.matricula());
        f.setNomeCompleto(req.nomeCompleto());
        f.setCpf(req.cpf());
        f.setRg(req.rg());
        f.setDataNascimento(req.dataNascimento());
        f.setGenero(req.genero());
        f.setEstadoCivil(req.estadoCivil());
        f.setEmailPessoal(req.emailPessoal());
        f.setEmailCorporativo(req.emailCorporativo());
        f.setTelefone(req.telefone());
        f.setCelular(req.celular());
        f.setCep(req.cep());
        f.setLogradouro(req.logradouro());
        f.setNumero(req.numero());
        f.setComplemento(req.complemento());
        f.setBairro(req.bairro());
        f.setCidade(req.cidade());
        f.setEstado(req.estado());
        f.setBanco(req.banco());
        f.setAgencia(req.agencia());
        f.setConta(req.conta());
        f.setTipoConta(req.tipoConta());
        f.setPix(req.pix());
        f.setDataAdmissao(req.dataAdmissao());
        f.setDataDesligamento(req.dataDesligamento());
        f.setStatus(req.status() != null ? req.status() : "ATIVO");
        f.setTipoContrato(req.tipoContrato());
        f.setSalarioAtual(req.salarioAtual());

        // Relacionamentos
        if (req.departamentoId() != null) {
            Departamento dep = departamentoRepository.findById(req.departamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento nao encontrado: " + req.departamentoId()));
            f.setDepartamento(dep);
        } else {
            f.setDepartamento(null);
        }

        if (req.cargoId() != null) {
            Cargo cargo = cargoRepository.findById(req.cargoId())
                    .orElseThrow(() -> new IllegalArgumentException("Cargo nao encontrado: " + req.cargoId()));
            f.setCargo(cargo);
        } else {
            f.setCargo(null);
        }

        if (req.gestorId() != null) {
            Funcionario gestor = funcionarioRepository.findById(req.gestorId())
                    .orElseThrow(() -> new IllegalArgumentException("Gestor nao encontrado: " + req.gestorId()));
            f.setGestor(gestor);
        } else {
            f.setGestor(null);
        }
    }
}
