package br.com.quintino.odonto.config;

import br.com.quintino.odonto.model.Consulta;
import br.com.quintino.odonto.model.Dentista;
import br.com.quintino.odonto.model.FichaPaciente;
import br.com.quintino.odonto.model.Funcionario;
import br.com.quintino.odonto.model.HorarioAtendimento;
import br.com.quintino.odonto.model.Observacao;
import br.com.quintino.odonto.model.Paciente;
import br.com.quintino.odonto.model.Perfil;
import br.com.quintino.odonto.model.Procedimento;
import br.com.quintino.odonto.model.StatusConsulta;
import br.com.quintino.odonto.repository.ConsultaRepository;
import br.com.quintino.odonto.repository.DentistaRepository;
import br.com.quintino.odonto.repository.FichaPacienteRepository;
import br.com.quintino.odonto.repository.FuncionarioRepository;
import br.com.quintino.odonto.repository.HorarioAtendimentoRepository;
import br.com.quintino.odonto.repository.ObservacaoRepository;
import br.com.quintino.odonto.repository.PacienteRepository;
import br.com.quintino.odonto.repository.ProcedimentoRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dados de demonstração. Como o H2 está em memória, a carga roda a cada inicialização.
 * Logins de teste (veja o README):
 *   consultorio@quintino.com / admin123   (administrativo)
 *   maria.silva@email.com / 123456        (recepção)
 *   carla.quintino@email.com / 123456     (dentista)
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final FuncionarioRepository funcionarios;
    private final DentistaRepository dentistas;
    private final PacienteRepository pacientes;
    private final FichaPacienteRepository fichas;
    private final ProcedimentoRepository procedimentos;
    private final HorarioAtendimentoRepository horarios;
    private final ConsultaRepository consultas;
    private final ObservacaoRepository observacoes;
    private final PasswordEncoder encoder;

    public DadosIniciais(FuncionarioRepository funcionarios, DentistaRepository dentistas,
                         PacienteRepository pacientes, FichaPacienteRepository fichas,
                         ProcedimentoRepository procedimentos, HorarioAtendimentoRepository horarios,
                         ConsultaRepository consultas, ObservacaoRepository observacoes,
                         PasswordEncoder encoder) {
        this.funcionarios = funcionarios;
        this.dentistas = dentistas;
        this.pacientes = pacientes;
        this.fichas = fichas;
        this.procedimentos = procedimentos;
        this.horarios = horarios;
        this.consultas = consultas;
        this.observacoes = observacoes;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (funcionarios.count() > 0) {
            return; // já populado
        }
        String senhaPadrao = encoder.encode("123456");

        // ----- funcionários
        funcionario("Consultório", "consultorio@quintino.com", "(11) 90000-0001", "000.000.000-01",
                "Administrador", Perfil.ADMINISTRATIVO, encoder.encode("admin123"));
        funcionario("Maria Fernanda Silva", "maria.silva@email.com", "11 92222-2222", "222.222.222-22",
                "Recepcionista", Perfil.RECEPCAO, senhaPadrao);
        funcionario("Marcela Novaes", "marcela.novaes@email.com", "11 93333-3333", "333.333.333-33",
                "Recepcionista", Perfil.RECEPCAO, senhaPadrao);
        Dentista carla = dentista("Carla Quintino", "carla.quintino@email.com", "11 90000-0000",
                "000.000.000-00", "CRO-SP 00000", senhaPadrao);
        Dentista barbara = dentista("Barbara de Oliveira", "barbara.oliveira@email.com", "11 91111-1111",
                "111.111.111-11", "CRO-SP 11111", senhaPadrao);

        // ----- horários de atendimento (segunda a sexta; Carla também atende sábado de manhã)
        for (Dentista d : List.of(carla, barbara)) {
            for (DayOfWeek dia : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                horarios.save(new HorarioAtendimento(d, dia, LocalTime.of(8, 0), LocalTime.of(12, 0)));
                horarios.save(new HorarioAtendimento(d, dia, LocalTime.of(13, 0), LocalTime.of(18, 0)));
            }
        }
        horarios.save(new HorarioAtendimento(carla, DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)));

        // ----- procedimentos
        Procedimento limpeza = procedimento("Limpeza", "Remoção de placa e tártaro", 30, carla, barbara);
        Procedimento avaliacao = procedimento("Avaliação", "Avaliação geral da saúde bucal", 30, carla, barbara);
        Procedimento aparelho = procedimento("Manutenção Aparelho", "Ajuste do aparelho ortodôntico", 30, barbara);
        Procedimento restauracao = procedimento("Restauração", "Restauração de dente com cárie", 60, carla);
        Procedimento canal = procedimento("Canal", "Tratamento de canal", 90, carla);
        Procedimento retorno = procedimento("Retorno e avaliação", "Retorno para avaliar o tratamento", 30,
                carla, barbara);

        // ----- pacientes
        Paciente julia = paciente("Julia Beatriz Dos Santos", "Feminino", LocalDate.of(2005, 5, 23),
                "11 93512-6648", "456325894-9", "236.957.106-36");
        Paciente davi = paciente("Davi Teixeira", "Masculino", LocalDate.of(1998, 3, 14),
                "11 98888-1111", "123456789-0", "111.444.777-35");
        Paciente kauane = paciente("Kauane Mineiro", "Feminino", LocalDate.of(2001, 8, 2),
                "11 97777-2222", "987654321-0", "529.982.247-25");
        Paciente ana = paciente("Ana Souza", "Feminino", LocalDate.of(1990, 11, 30),
                "11 96666-3333", "112233445-5", "390.533.447-05");
        Paciente marcos = paciente("Marcos Lima", "Masculino", LocalDate.of(1985, 1, 19),
                "11 95555-4444", "554433221-1", "168.995.350-09");

        FichaPaciente ficha = new FichaPaciente(julia);
        ficha.setProblemasDeSaude("Hipertensão\nAlergia a Dipirona\nUso contínuo de losartana");
        ficha.setSintomas("Dor no dente 26 há 3 dias\nSensibilidade ao frio");
        fichas.save(ficha);

        // ----- consultas (histórico da Julia + agenda dos próximos dias)
        LocalDate hoje = LocalDate.now();
        Consulta avaliacaoJulia = consulta(julia, carla, avaliacao, LocalDate.of(2025, 12, 5).atTime(9, 0),
                StatusConsulta.REALIZADA, "26");
        consulta(julia, carla, canal, LocalDate.of(2026, 2, 10).atTime(11, 0), StatusConsulta.REALIZADA, null);
        consulta(julia, carla, limpeza, LocalDate.of(2026, 6, 2).atTime(9, 30), StatusConsulta.REALIZADA, null);
        Consulta restauracaoJulia = consulta(julia, carla, restauracao,
                LocalDate.of(2026, 9, 15).atTime(10, 0), StatusConsulta.REALIZADA, "26");
        consulta(julia, carla, retorno, diaUtil(hoje.plusDays(14)).atTime(14, 0), StatusConsulta.AGENDADA, "26");

        consulta(davi, carla, limpeza, hoje.atTime(10, 0), StatusConsulta.AGENDADA, null);
        consulta(julia, barbara, aparelho, hoje.atTime(14, 0), StatusConsulta.AGENDADA, null);
        consulta(kauane, carla, avaliacao, hoje.atTime(15, 0), StatusConsulta.AGENDADA, null);
        consulta(ana, carla, limpeza, diaUtil(hoje.plusDays(2)).atTime(9, 0), StatusConsulta.AGENDADA, null);
        consulta(marcos, barbara, avaliacao, diaUtil(hoje.plusDays(3)).atTime(15, 0), StatusConsulta.AGENDADA, null);

        // ----- observações da Julia (só incluídas, nunca alteradas)
        observacoes.save(new Observacao(julia, restauracaoJulia,
                "Paciente relatou medo de anestesia. Conversar antes do procedimento", "Carla Quintino",
                LocalDateTime.of(2026, 9, 15, 10, 40)));
        observacoes.save(new Observacao(julia, restauracaoJulia,
                "Retorno em 30 dias para avaliar a restauração do dente 26", "Carla Quintino",
                LocalDateTime.of(2026, 9, 15, 10, 50)));
        observacoes.save(new Observacao(julia, avaliacaoJulia,
                "Higiene bucal boa. Reforçar o uso do fio dental.", "Carla Quintino",
                LocalDateTime.of(2025, 12, 5, 9, 30)));
    }

    private static LocalDate diaUtil(LocalDate d) {
        return switch (d.getDayOfWeek()) {
            case SATURDAY -> d.plusDays(2);
            case SUNDAY -> d.plusDays(1);
            default -> d;
        };
    }

    private void funcionario(String nome, String email, String telefone, String cpf, String cargo,
                             Perfil perfil, String senhaHash) {
        Funcionario f = new Funcionario();
        f.setNome(nome);
        f.setEmail(email);
        f.setTelefone(telefone);
        f.setCpf(cpf);
        f.setCargo(cargo);
        f.setPerfil(perfil);
        f.setSenhaHash(senhaHash);
        funcionarios.save(f);
    }

    private Dentista dentista(String nome, String email, String telefone, String cpf, String cro,
                              String senhaHash) {
        Dentista d = new Dentista();
        d.setNome(nome);
        d.setEmail(email);
        d.setTelefone(telefone);
        d.setCpf(cpf);
        d.setCargo("Dentista");
        d.setCro(cro);
        d.setSenhaHash(senhaHash);
        return dentistas.save(d);
    }

    private Procedimento procedimento(String nome, String descricao, int minutos, Dentista... quem) {
        Procedimento p = new Procedimento();
        p.setNome(nome);
        p.setDescricao(descricao);
        p.setDuracaoMinutos(minutos);
        Set<Dentista> responsaveis = new HashSet<>(List.of(quem));
        p.setDentistas(responsaveis);
        return procedimentos.save(p);
    }

    private Paciente paciente(String nome, String sexo, LocalDate nascimento, String telefone, String rg,
                              String cpf) {
        Paciente p = new Paciente();
        p.setNome(nome);
        p.setSexo(sexo);
        p.setDataNascimento(nascimento);
        p.setTelefone(telefone);
        p.setRg(rg);
        p.setCpf(cpf);
        return pacientes.save(p);
    }

    private Consulta consulta(Paciente paciente, Dentista dentista, Procedimento procedimento,
                              LocalDateTime dataHora, StatusConsulta status, String dente) {
        Consulta c = new Consulta();
        c.setPaciente(paciente);
        c.setDentista(dentista);
        c.setProcedimento(procedimento);
        c.setDataHora(dataHora);
        c.setStatus(status);
        c.setDente(dente);
        return consultas.save(c);
    }
}
