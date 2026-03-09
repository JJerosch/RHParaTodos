package proj.paratodos.dto;

import java.util.List;

public class MeuPontoResumoResponse {

    private String statusAtual;
    private String proximaAcao;
    private String horasHoje;
    private String horasSemana;
    private String horasMes;
    private String bancoHoras;
    private List<PontoMarcacaoResponse> marcacoesHoje;
    private String tipoDia;

    public MeuPontoResumoResponse() {
    }

    public MeuPontoResumoResponse(
            String statusAtual,
            String proximaAcao,
            String horasHoje,
            String horasSemana,
            String horasMes,
            String bancoHoras,
            List<PontoMarcacaoResponse> marcacoesHoje,
            String tipoDia
    ) {
        this.statusAtual = statusAtual;
        this.proximaAcao = proximaAcao;
        this.horasHoje = horasHoje;
        this.horasSemana = horasSemana;
        this.horasMes = horasMes;
        this.bancoHoras = bancoHoras;
        this.marcacoesHoje = marcacoesHoje;
        this.tipoDia = tipoDia;
    }

    public String getStatusAtual() {
        return statusAtual;
    }

    public void setStatusAtual(String statusAtual) {
        this.statusAtual = statusAtual;
    }

    public String getProximaAcao() {
        return proximaAcao;
    }

    public void setProximaAcao(String proximaAcao) {
        this.proximaAcao = proximaAcao;
    }

    public String getHorasHoje() {
        return horasHoje;
    }

    public void setHorasHoje(String horasHoje) {
        this.horasHoje = horasHoje;
    }

    public String getHorasSemana() {
        return horasSemana;
    }

    public void setHorasSemana(String horasSemana) {
        this.horasSemana = horasSemana;
    }

    public String getHorasMes() {
        return horasMes;
    }

    public void setHorasMes(String horasMes) {
        this.horasMes = horasMes;
    }

    public String getBancoHoras() {
        return bancoHoras;
    }

    public void setBancoHoras(String bancoHoras) {
        this.bancoHoras = bancoHoras;
    }

    public List<PontoMarcacaoResponse> getMarcacoesHoje() {
        return marcacoesHoje;
    }

    public void setMarcacoesHoje(List<PontoMarcacaoResponse> marcacoesHoje) {
        this.marcacoesHoje = marcacoesHoje;
    }

    public String getTipoDia() {
        return tipoDia;
    }

    public void setTipoDia(String tipoDia) {
        this.tipoDia = tipoDia;
    }
}