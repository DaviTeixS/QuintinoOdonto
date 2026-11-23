package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.AgendamentoForm;
import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.ConsultaDetalheDTO;
import br.com.quintino.odonto.dto.DiaAgendaDTO;
import br.com.quintino.odonto.dto.HorarioDTO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Agenda do consultório: calendário, agendamento de consultas e horários de atendimento dos dentistas.
 * Os métodos que recebem "dentistaId" filtram pela agenda de um dentista (null = agenda geral).
 * Erros de regra de negócio são lançados como IllegalArgumentException com a mensagem para o usuário.
 */
public interface AgendaService {

    /** Dias do mês (1..31) que têm consultas e quantas são. Ignora canceladas. */
    List<DiaAgendaDTO> diasComConsulta(int ano, int mes, Long dentistaId);

    /** Consultas do dia (sem as canceladas) ordenadas por hora. */
    List<ConsultaDTO> consultasDoDia(LocalDate data, Long dentistaId);

    /** Consultas de [inicio, fim) (fim exclusivo), sem as canceladas, por data e hora (visão semanal da agenda). */
    List<ConsultaDTO> consultasDoPeriodo(LocalDate inicio, LocalDate fim, Long dentistaId);

    Optional<ConsultaDetalheDTO> buscarConsulta(Long id);

    /** RF12 + RF17: agenda e impede dois pacientes no mesmo horário do mesmo dentista. */
    ConsultaDetalheDTO agendar(AgendamentoForm form);

    /** RF13/RF14: altera/reagenda uma consulta agendada (mesmas regras do agendamento). */
    ConsultaDetalheDTO alterar(Long id, AgendamentoForm form);

    /** RF15: cancela uma consulta agendada, registrando o motivo. */
    void cancelar(Long id, String motivo);

    /** Marca uma consulta agendada como realizada. */
    void concluir(Long id);

    /** Horários de início livres ("HH:mm") do dentista no dia, para a duração do procedimento. */
    List<String> horariosLivres(Long dentistaId, Long procedimentoId, LocalDate data, Long ignorarConsultaId);

    /** RF10: horários de atendimento semanais do dentista. */
    List<HorarioDTO> horarios(Long dentistaId);

    void adicionarHorario(Long dentistaId, DayOfWeek dia, LocalTime inicio, LocalTime fim);

    void removerHorario(Long dentistaId, Long horarioId);

    /**
     * RF10: substitui todos os períodos semanais do dentista pelos informados (tela "Horários de atendimento",
     * botão "Salvar horários"). Erros de validação (fim antes do início, períodos sobrepostos) viram
     * IllegalArgumentException com a mensagem para o usuário.
     */
    void salvarHorarios(Long dentistaId, List<HorarioDTO> periodos);
}
