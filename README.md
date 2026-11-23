# Quintino Odonto

Sistema interno da clínica para organizar agendas dos dentistas, pacientes e prontuário.
Spring Boot 3.2 · Spring MVC + Thymeleaf · Spring Data JPA · banco H2 (em memória).

## Como executar
1. JDK 17 ou superior.
2. Pela IDE: execute a classe `br.com.quintino.odonto.QuintinoOdontoApplication`.
   Pelo terminal (com Maven instalado): `mvn spring-boot:run`.
3. Acesse http://localhost:8080 e entre com um dos usuários de teste.
4. Banco (opcional): http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:quintino`, usuário `sa`, senha vazia.

Os dados de demonstração são recriados a cada inicialização (`config/DadosIniciais`), porque o H2 está em memória.

| Perfil | E-mail | Senha |
|---|---|---|
| Administrativo | consultorio@quintino.com | admin123 |
| Recepção | maria.silva@email.com | 123456 |
| Dentista | carla.quintino@email.com | 123456 |
| Dentista | barbara.oliveira@email.com | 123456 |

## O que cada perfil faz
- **Administrativo e Recepção ("consultório")**: cadastram/editam pacientes, colaboradores, procedimentos e horários de
  atendimento; veem a agenda geral; agendam, alteram/reagendam e cancelam consultas.
- **Dentista**: vê só a própria agenda (diária e mensal), as fichas dos pacientes (saúde, sintomas, histórico) e é o único
  que registra observações.
- A regra de acesso fica em `config/AutenticacaoInterceptor`; os menus/botões somem conforme o perfil.

## Estrutura (pacote `br.com.quintino.odonto`)
- `model` — entidades JPA do diagrama de classes (`Funcionario`, `Dentista`, `Paciente`, `FichaPaciente`, `Procedimento`,
  `HorarioAtendimento`, `Consulta`, `Observacao`, enums `Perfil` e `StatusConsulta`).
- `repository` — interfaces `JpaRepository` (uma por entidade).
- `service` — regras de negócio: interfaces (contrato usado pelas telas) + `*Impl` com JPA.
- `controller` — telas (Thymeleaf) e `ApiController` (JSON do calendário e das listas).
- `config` — login por sessão, controle de acesso, dados iniciais. `dto` — objetos trocados com as telas.

## Requisitos x implementação
| Requisito | Situação |
|---|---|
| RF01–RF04 login, perfil, redirecionamento e restrição de acesso | Feito |
| RF05–RF06 cadastrar/editar pacientes | Feito |
| RF07–RF08 cadastrar/editar colaboradores | Feito (a função de um colaborador já cadastrado não muda) |
| RF09 cadastrar procedimentos | Feito (também edição) |
| RF10 horários de atendimento do dentista | Feito (Colaboradores > editar > Gerenciar horários: uma linha por dia, períodos em botões e "Salvar horários") |
| RF11 agenda geral | Feito (calendário na tela inicial e tela Agendas, com a lista "Todos os dentistas" para ver a agenda de um dentista) |
| RF12–RF15 agendar, reagendar, alterar, cancelar | Feito (`/consultas`: tela própria de Reagendar com horários livres em botões e janela de confirmação do cancelamento; o motivo é opcional) |
| RF16 e RF18 fichas e histórico de consultas | Feito |
| RF17 sem dois pacientes no mesmo horário do mesmo dentista | Feito (sobreposição considera a duração do procedimento) |
| RF19–RF25 agenda do dentista, procedimento previsto, saúde/sintomas, observações, histórico | Feito (agenda do dentista nas visões Dia, Semana e Mês em `/agenda`) |
| RNF01/RNF02/RNF04 autenticação e acesso por perfil | Feito |
| RNF08/RNF09 mensagens claras e validação antes de gravar | Feito (validação manual nos controllers/serviços) |
| RNF10 observação não pode ser alterada | Feito (`@Immutable`, sem setters, sem tela de edição) |
| RNF11 auditoria (usuário, data e hora) | **Parcial**: só as observações registram autor e data/hora |
| RNF05/RNF06/RNF12 desempenho e navegadores | Não medido formalmente |

## Diferenças em relação ao diagrama de classes
- `HorarioAtendimento` (o código antigo chamava `HoraAtendimento`).
- `Funcionario` guarda `senhaHash` (BCrypt); a classe `Usuario` foi removida (o login usa `Funcionario`).
- `Dentista` herda de `Funcionario` (herança JOINED, tabela `dentistas`).
- Campos extras usados pelas telas: `Paciente.rg`/`sexo`, `FichaPaciente.sintomas`, `Consulta.dente`,
  `Observacao.paciente`/`autor`. A observação fica ligada à consulta mais recente do paciente, quando existe.
- "Tratamento" (tela do paciente) não é entidade: é derivado das consultas com dente informado.

## Limitações conhecidas
- Não há testes automatizados.
- Paciente/colaborador/procedimento não têm tela de exclusão ou desativação (existe o campo `ativo`).
- O cadastro público (`/cadastro`) cria uma conta de recepção já ativa; em uso real convém exigir aprovação.
- Formulários não têm proteção CSRF (não usa Spring Security completo).
- Dois agendamentos simultâneos no mesmo horário podem passar pela checagem (não há trava de concorrência).
