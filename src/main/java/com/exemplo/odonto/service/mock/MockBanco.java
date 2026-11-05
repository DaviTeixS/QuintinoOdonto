package br.com.quintino.odonto.service.mock;

import br.com.quintino.odonto.dto.ColaboradorDTO;
import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.ObservacaoDTO;
import br.com.quintino.odonto.dto.PacienteDTO;
import br.com.quintino.odonto.dto.ProcedimentoDTO;
import br.com.quintino.odonto.dto.SaudeDTO;
import br.com.quintino.odonto.dto.TratamentoDTO;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * DADOS DE MENTIRA (em memória) para o front funcionar antes do back-end existir.
 * Só é carregado com o profile "mock". Quando o back real estiver pronto, apague a pasta
 * service/mock e a linha spring.profiles.active=mock do application.properties.
 */
@Component
@Profile("mock")
public class MockBanco {

    public final AtomicLong seq = new AtomicLong(1000);
    public final List<PacienteDTO> pacientes = new CopyOnWriteArrayList<>();
    public final List<ColaboradorDTO> colaboradores = new CopyOnWriteArrayList<>();
    public final List<ProcedimentoDTO> procedimentos = new CopyOnWriteArrayList<>();
    public final List<ConsultaDTO> consultas = new CopyOnWriteArrayList<>();
    public final Map<Long, TratamentoDTO> tratamentos = new ConcurrentHashMap<>();
    public final Map<Long, SaudeDTO> saudes = new ConcurrentHashMap<>();
    public final Map<Long, List<ObservacaoDTO>> observacoes = new ConcurrentHashMap<>();

    public String nomePaciente(Long id) {
        return pacientes.stream().filter(p -> p.id().equals(id)).map(PacienteDTO::nome).findFirst().orElse("");
    }

    public String nomeColaborador(Long id) {
        return colaboradores.stream().filter(c -> c.id().equals(id)).map(ColaboradorDTO::nome).findFirst().orElse("");
    }

    private ConsultaDTO consulta(long pacienteId, long dentistaId, String procedimento,
                                 LocalDate data, int hora, int minuto, String status, String dente) {
        return new ConsultaDTO(seq.incrementAndGet(), pacienteId, nomePaciente(pacienteId),
                dentistaId, nomeColaborador(dentistaId), procedimento,
                data, LocalTime.of(hora, minuto), status, dente);
    }

    @PostConstruct
    void popular() {
        LocalDate hoje = LocalDate.now();

        pacientes.add(new PacienteDTO(1L, "Julia Beatriz Dos Santos", "Feminino", LocalDate.of(2005, 5, 23),
                "11 93512-6648", "456325894-9", "236957106-36"));
        pacientes.add(new PacienteDTO(2L, "Davi Teixeira", "Masculino", LocalDate.of(1998, 3, 14),
                "11 98888-1111", "123456789-0", "111444777-35"));
        pacientes.add(new PacienteDTO(3L, "Kauane Mineiro", "Feminino", LocalDate.of(2001, 8, 2),
                "11 97777-2222", "987654321-0", "529982247-25"));
        pacientes.add(new PacienteDTO(4L, "Ana Souza", "Feminino", LocalDate.of(1990, 11, 30),
                "11 96666-3333", "112233445-5", "390533447-05"));
        pacientes.add(new PacienteDTO(5L, "Marcos Lima", "Masculino", LocalDate.of(1985, 1, 19),
                "11 95555-4444", "554433221-1", "168995350-09"));

        colaboradores.add(new ColaboradorDTO(1L, "Carla Quintino", "DENTISTA", "carla.quintino@email.com",
                "11 90000-0000", "000.000.000-00", "CRO-SP 00000"));
        colaboradores.add(new ColaboradorDTO(2L, "Barbara de Oliveira", "DENTISTA", "barbara.oliveira@email.com",
                "11 91111-1111", "111.111.111-11", "CRO-SP 11111"));
        colaboradores.add(new ColaboradorDTO(3L, "Maria Fernanda Silva", "RECEPCIONISTA", "maria.silva@email.com",
                "11 92222-2222", "222.222.222-22", ""));
        colaboradores.add(new ColaboradorDTO(4L, "Marcela Novaes", "RECEPCIONISTA", "marcela.novaes@email.com",
                "11 93333-3333", "333.333.333-33", ""));

        procedimentos.add(new ProcedimentoDTO(1L, "Limpeza", 30, "Remoção de placa e tártaro",
                List.of(1L, 2L), List.of("Carla Quintino", "Barbara de Oliveira")));
        procedimentos.add(new ProcedimentoDTO(2L, "Avaliação", 30, "Avaliação geral da saúde bucal",
                List.of(1L, 2L), List.of("Carla Quintino", "Barbara de Oliveira")));
        procedimentos.add(new ProcedimentoDTO(3L, "Manutenção Aparelho", 30, "Ajuste do aparelho ortodôntico",
                List.of(2L), List.of("Barbara de Oliveira")));
        procedimentos.add(new ProcedimentoDTO(4L, "Restauração", 60, "Restauração de dente com cárie",
                List.of(1L), List.of("Carla Quintino")));

        // Consultas de hoje e próximos dias (o calendário sempre mostra algo)
        consultas.add(consulta(2, 1, "Limpeza", hoje, 10, 0, "AGENDADA", null));
        consultas.add(consulta(1, 2, "Manutenção aparelho", hoje, 12, 0, "AGENDADA", null));
        consultas.add(consulta(3, 1, "Avaliação", hoje, 12, 30, "AGENDADA", null));
        consultas.add(consulta(4, 1, "Limpeza", hoje.plusDays(2), 9, 0, "AGENDADA", null));
        consultas.add(consulta(5, 2, "Avaliação", hoje.plusDays(3), 15, 0, "AGENDADA", null));

        // Histórico da Julia (igual ao Figma)
        ConsultaDTO avaliacao = consulta(1, 1, "Avaliação", LocalDate.of(2025, 12, 5), 9, 0, "REALIZADA", "26");
        ConsultaDTO restauracao = consulta(1, 1, "Restauração", LocalDate.of(2026, 9, 15), 10, 0, "REALIZADA", "26");
        ConsultaDTO retorno = consulta(1, 1, "Retorno e avaliação", hoje.plusDays(14), 14, 0, "AGENDADA", "26");
        consultas.add(avaliacao);
        consultas.add(restauracao);
        consultas.add(retorno);
        consultas.add(consulta(1, 1, "Limpeza", LocalDate.of(2026, 6, 2), 9, 30, "REALIZADA", null));
        consultas.add(consulta(1, 1, "Canal", LocalDate.of(2026, 2, 10), 11, 0, "REALIZADA", null));

        tratamentos.put(1L, new TratamentoDTO(1L, "Restauração do dente 26", "EM_ANDAMENTO",
                List.of(avaliacao, restauracao, retorno)));

        saudes.put(1L, new SaudeDTO(
                List.of("Hipertensão", "Alergia a Dipirona", "Uso contínuo de losartana"),
                List.of("Dor no dente 26 há 3 dias", "Sensibilidade ao frio")));

        observacoes.put(1L, new CopyOnWriteArrayList<>(List.of(
                new ObservacaoDTO(1L, "Paciente relatou medo de anestesia. Conversar antes do procedimento",
                        LocalDate.of(2026, 9, 15), "Carla Quintino"),
                new ObservacaoDTO(2L, "Retorno em 30 dias para avaliar a restauração do dente 26",
                        LocalDate.of(2026, 9, 15), "Carla Quintino"),
                new ObservacaoDTO(3L, "Higiene bucal boa. Reforçar o uso do fio dental.",
                        LocalDate.of(2026, 6, 2), "Carla Quintino"))));
    }
}
