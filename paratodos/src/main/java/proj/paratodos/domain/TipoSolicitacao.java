package proj.paratodos.domain;

public enum TipoSolicitacao {

    // Funcionário
    ALTERACAO_FUNCIONARIO,
    ATIVACAO_FUNCIONARIO,
    DESATIVACAO_FUNCIONARIO,
    EXCLUSAO_FUNCIONARIO,

    // Departamento
    CRIACAO_DEPARTAMENTO,
    EDICAO_DEPARTAMENTO,
    DESATIVACAO_DEPARTAMENTO,
    EXCLUSAO_DEPARTAMENTO,

    // Cargo
    CRIACAO_CARGO,
    EDICAO_CARGO,
    DESATIVACAO_CARGO,
    EXCLUSAO_CARGO,

    // Recrutamento
    ABERTURA_VAGA,

    // Movimentação de pessoal
    PROMOCAO,
    TRANSFERENCIA,
    REAJUSTE_SALARIAL
}
