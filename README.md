# RHParaTodos

Sistema web para gestao de Recursos Humanos, com modulo administrativo e area do colaborador. Possui backend Spring Boot com endpoints REST em `/api` e interface web em Thymeleaf.

## Stack
- Java 21
- Spring Boot 4.0.2 (Web, Security, Validation, Mail)
- Spring Data JPA + PostgreSQL
- Flyway (migracoes e seed)
- Thymeleaf (templates server-side)
- JWT em cookie
- Maven

## Funcionalidades principais
- Dashboard com KPIs, contratacoes recentes e aniversariantes
- Funcionarios: CRUD, validacoes (CPF/matricula/email), filtros e estatisticas
- Departamentos e cargos: cadastro, edicao, contagens e faixas salariais
- Recrutamento: vagas, candidaturas, etapas e contratacao automatica
- Solicitacoes com workflow (pendente/aprovada/rejeitada/cancelada)
- Promocoes e movimentacoes com atualizacao de cargo/dep/salario
- Beneficios por cargo, incidencias e estatisticas de custo
- Ferias e ocorrencias (atestado, licenca, faltas) com regras de ponto
- Ponto e timesheet com apuracao diaria e calendario
- Relatorios e configuracoes administrativas

## Modulos/paginas
Dashboard, Funcionarios, Departamentos, Cargos, Recrutamento, Promocoes,
Solicitacoes, Beneficios, Ferias, Ponto, Timesheet.

## Seguranca e perfis
Perfis previstos: `ADMIN`, `RH_CHEFE`, `RH_ASSISTENTE`, `DP_CHEFE`, `DP_ASSISTENTE`, `EMPLOYEE`.
As rotas sao autorizadas por modulo. A autenticacao usa JWT em cookie.

## Requisitos
- Java 21
- Maven
- PostgreSQL

## Configuracao
Arquivo principal: [paratodos/src/main/resources/application.properties](C:/Users/jpjer/OneDrive/Documentos/RHParaTodos/paratodos/src/main/resources/application.properties)

Exemplo (ajuste para o seu ambiente):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/paratodos
spring.datasource.username=postgres
spring.datasource.password=SUASENHA

app.jwt.secret-base64=SEU_SEGREDO_BASE64
app.jwt.expiration-ms=86400000

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu.email@gmail.com
spring.mail.password=SENHA_DE_APP
app.mail.from=seu.email@gmail.com
```

Observacao: o arquivo atual contem credenciais reais. Recomenda-se mover
segredos para variaveis de ambiente ou um arquivo `.properties` local fora
do controle de versao.

## Banco de dados e seeds
As migracoes Flyway estao em
[paratodos/src/main/resources/db/migration](C:/Users/jpjer/OneDrive/Documentos/RHParaTodos/paratodos/src/main/resources/db/migration).
Ha migrations de baseline, evolucoes e dados de exemplo (seed).

## Execucao local
1. Suba o PostgreSQL e crie o banco `paratodos`.
2. Ajuste `application.properties`.
3. Rode:
```powershell
cd paratodos
.\mvnw spring-boot:run
```
Por padrao o app sobe em `http://localhost:8080`.

## Testes
```powershell
cd paratodos
.\mvnw test
```
