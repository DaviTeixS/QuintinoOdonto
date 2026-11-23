package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.AgendamentoForm;
import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.ConsultaDetalheDTO;
import br.com.quintino.odonto.dto.DiaAgendaDTO;
import br.com.quintino.odonto.dto.HorarioDTO;
import br.com.quintino.odonto.model.Consulta;
import br.com.quintino.odonto.model.Dentista;
import br.com.quintino.odonto.model.HorarioAtendimento;
import br.com.quintino.odonto.model.Paciente;
import br.com.quintino.odonto.model.Procedimento;
import br.com.quintino.odonto.model.StatusConsulta;
import br.com.quintino.odonto.repository.ConsultaRepository;
import br.com.quintino.odonto.repository.DentistaRepository;
import br.com.quintino.odonto.repository.HorarioAtendimentoRepository;
import br.com.quintino.odonto.repository.PacienteRepository;
import br.com.quintino.odonto.repository.ProcedimentoRepository;
import br.com.quintino.odonto.util.Fmt;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaServiceImpl implements AgendaService {

    /** Os horários oferecidos para início de consulta ficam de 30 em 30 minutos. */
    private static final int PASSO_MINUTOS = 30;

    private final ConsultaRepository consultas;
    private final PacienteRepository pacientes;
    private final DentistaRepository dentistas;
    private final ProcedimentoRepository procedimentos;
    private final HorarioAtendimentoRepository horarios;

    public AgendaServiceImpl(ConsultaRepository consultas, PacienteRepository pacientes,
                             DentistaRepository dentistas, ProcedimentoRepository procedimentos,
                             HorarioAtendimentoRepository horarios) {
        this.consultas = consultas;
        this.pacientes = pacientes;
        this.dentistas = dentistas;
        this.procedimentos = procedimentos;
        this.horarios = horarios;
    }

    // ------------------------------------------------------------------ calendário

    private List<Consulta> listar(LocalDateTime inicio, LocalDateTime fim, Long dentistaId) {
        if (dentistaId == null) {
            return consultas.findByDataHoraGreaterThanEqualAndDataHoraLessThanAndStatusNotOrderByDataHoraAsc(
                    inicio, fim, StatusConsulta.CANCELADA);
        }
        return consultas
                .findByDentistaIdAndDataHoraGreaterThanEqualAndDataHoraLessThanAndStatusNotOrderByDataHoraAsc(
                        dentistaId, inicio, fim, StatusConsulta.CANCELADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaAgendaDTO> diasComConsulta(int ano, int mes, Long dentistaId) {
        if (mes < 1 || mes > 12 || ano < 1900 || ano > 2200) {
            return List.of();
        }
        LocalDateTime inicio = LocalDate.of(ano, mes, 1).atStartOfDay();
        Map<Integer, Long> porDia = listar(inicio, inicio.plusMonths(1), dentistaId).stream()
                .collect(Collectors.groupingBy(c -> c.getDataHora().getDayOfMonth(), TreeMap::new,
                        Collectors.counting()));
        return porDia.entrySet().stream().map(e -> new DiaAgendaDTO(e.getKey(), e.getValue())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaDTO> consultasDoDia(LocalDate data, Long dentistaId) {
        LocalDateTime inicio = data.atStartOfDay();
        return listar(inicio, inicio.plusDays(1), dentistaId).stream().map(Conversores::paraDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaDTO> consultasDoPeriodo(LocalDate inicio, LocalDate fim, Long dentistaId) {
        if (inicio == null || fim == null || !fim.isAfter(inicio) || inicio.plusDays(62).isBefore(fim)) {
            return List.of();
        }
        return listar(inicio.atStartOfDay(), fim.atStartOfDay(), dentistaId).stream()
                .map(Conversores::paraDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultaDetalheDTO> buscarConsulta(Long id) {
        return consultas.findById(id).map(Conversores::detalhe);
    }

    // ------------------------------------------------------------------ agendamento

    @Override
    @Transactional
    public ConsultaDetalheDTO agendar(AgendamentoForm form) {
        Consulta c = new Consulta();
        preencher(c, form, null);
        c.setStatus(StatusConsulta.AGENDADA);
        return Conversores.detalhe(consultas.save(c));
    }

    @Override
    @Transactional
    public ConsultaDetalheDTO alterar(Long id, AgendamentoForm form) {
        Consulta c = obter(id);
        if (c.getStatus() != StatusConsulta.AGENDADA) {
            throw new IllegalArgumentException("Só é possível alterar consultas que estão agendadas.");
        }
        preencher(c, form, id);
        return Conversores.detalhe(consultas.save(c));
    }

    @Override
    @Transactional
    public void cancelar(Long id, String motivo) {
        Consulta c = obter(id);
        if (c.getStatus() != StatusConsulta.AGENDADA) {
            throw new IllegalArgumentException("Só é possível cancelar consultas que estão agendadas.");
        }
        String m = motivo == null ? "" : motivo.trim();
        if (m.isEmpty()) {
            m = "Não informado"; // a tela de confirmação do Figma não pede o motivo; ele é opcional
        }
        if (m.length() > 500) {
            throw new IllegalArgumentException("O motivo do cancelamento pode ter no máximo 500 caracteres.");
        }
        c.setStatus(StatusConsulta.CANCELADA);
        c.setMotivoCancelamento(m);
        consultas.save(c);
    }

    @Override
    @Transactional
    public void concluir(Long id) {
        Consulta c = obter(id);
        if (c.getStatus() != StatusConsulta.AGENDADA) {
            throw new IllegalArgumentException("Só é possível concluir consultas que estão agendadas.");
        }
        if (c.getDataHora().toLocalDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Esta consulta ainda não aconteceu.");
        }
        c.setStatus(StatusConsulta.REALIZADA);
        consultas.save(c);
    }

    private Consulta obter(Long id) {
        return consultas.findById(id).orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada."));
    }

    /** Valida o formulário e copia os dados para a consulta (nova ou existente). */
    private void preencher(Consulta c, AgendamentoForm f, Long ignorarId) {
        if (f.pacienteId() == null) {
            throw new IllegalArgumentException("Selecione o paciente.");
        }
        if (f.procedimentoId() == null) {
            throw new IllegalArgumentException("Selecione o procedimento.");
        }
        if (f.dentistaId() == null) {
            throw new IllegalArgumentException("Selecione o dentista.");
        }
        if (f.data() == null || f.hora() == null) {
            throw new IllegalArgumentException("Informe a data e o horário da consulta.");
        }
        Paciente paciente = pacientes.findById(f.pacienteId()).filter(Paciente::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Paciente não encontrado. Cadastre o paciente antes de agendar."));
        Procedimento procedimento = procedimentos.findById(f.procedimentoId()).filter(Procedimento::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Procedimento não encontrado."));
        Dentista dentista = dentistas.findById(f.dentistaId()).filter(Dentista::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Dentista não encontrado."));
        boolean realiza = procedimento.getDentistas().stream().anyMatch(d -> d.getId().equals(dentista.getId()));
        if (!realiza) {
            throw new IllegalArgumentException("Este dentista não realiza o procedimento selecionado.");
        }

        LocalDateTime inicio = LocalDateTime.of(f.data(), f.hora());
        boolean mudouHorario = c.getDataHora() == null || !c.getDataHora().equals(inicio)
                || !c.getDentista().getId().equals(dentista.getId());
        if (mudouHorario && inicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar em uma data e hora que já passaram.");
        }
        validarDisponibilidade(dentista.getId(), procedimento.getDuracaoMinutos(), inicio, ignorarId);

        String sintomas = f.sintomas() == null ? "" : f.sintomas().trim();
        String dente = f.dente() == null ? "" : f.dente().trim();
        if (sintomas.length() > 1000) {
            throw new IllegalArgumentException("Os sintomas podem ter no máximo 1000 caracteres.");
        }
        if (dente.length() > 10) {
            throw new IllegalArgumentException("Informe o dente com até 10 caracteres (ex.: 26).");
        }
        c.setPaciente(paciente);
        c.setDentista(dentista);
        c.setProcedimento(procedimento);
        c.setDataHora(inicio);
        c.setSintomasInformados(sintomas.isEmpty() ? null : sintomas);
        c.setDente(dente.isEmpty() ? null : dente);
    }

    /** RF17: dentro do horário de atendimento do dentista e sem sobrepor outra consulta dele. */
    private void validarDisponibilidade(Long dentistaId, int duracao, LocalDateTime inicio, Long ignorarId) {
        LocalDateTime fim = inicio.plusMinutes(duracao);
        if (!fim.toLocalDate().equals(inicio.toLocalDate())) {
            throw new IllegalArgumentException("O atendimento precisa terminar no mesmo dia em que começa.");
        }
        if (!dentroDoAtendimento(dentistaId, inicio, fim)) {
            throw new IllegalArgumentException(
                    "O dentista não atende neste dia e horário. Escolha um horário dentro do atendimento dele.");
        }
        if (ocupado(dentistaId, inicio, fim, ignorarId)) {
            throw new IllegalArgumentException(
                    "Este horário já está ocupado para esta dentista. Escolha outro horário.");
        }
    }

    private boolean dentroDoAtendimento(Long dentistaId, LocalDateTime inicio, LocalDateTime fim) {
        return horarios.findByDentistaIdAndDiaSemana(dentistaId, inicio.getDayOfWeek()).stream()
                .anyMatch(h -> !inicio.toLocalTime().isBefore(h.getHoraInicio())
                        && !fim.toLocalTime().isAfter(h.getHoraFim()));
    }

    /** Há outra consulta (não cancelada) do dentista cujo período se sobrepõe a [inicio, fim)? */
    private boolean ocupado(Long dentistaId, LocalDateTime inicio, LocalDateTime fim, Long ignorarId) {
        LocalDateTime inicioDia = inicio.toLocalDate().atStartOfDay();
        for (Consulta outra : listar(inicioDia, inicioDia.plusDays(1), dentistaId)) {
            if (ignorarId != null && ignorarId.equals(outra.getId())) {
                continue;
            }
            LocalDateTime outraFim = outra.getDataHora().plusMinutes(outra.getProcedimento().getDuracaoMinutos());
            if (inicio.isBefore(outraFim) && outra.getDataHora().isBefore(fim)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> horariosLivres(Long dentistaId, Long procedimentoId, LocalDate data,
                                       Long ignorarConsultaId) {
        if (dentistaId == null || procedimentoId == null || data == null) {
            return List.of();
        }
        Optional<Procedimento> proc = procedimentos.findById(procedimentoId);
        if (proc.isEmpty() || proc.get().getDuracaoMinutos() <= 0) {
            return List.of();
        }
        int duracao = proc.get().getDuracaoMinutos();
        LocalDateTime agora = LocalDateTime.now();
        List<String> livres = new ArrayList<>();
        for (HorarioAtendimento h : horarios.findByDentistaIdAndDiaSemana(dentistaId, data.getDayOfWeek())) {
            int fimMin = h.getHoraFim().toSecondOfDay() / 60;
            for (int m = h.getHoraInicio().toSecondOfDay() / 60; m + duracao <= fimMin; m += PASSO_MINUTOS) {
                LocalTime t = LocalTime.ofSecondOfDay(m * 60L);
                LocalDateTime inicio = LocalDateTime.of(data, t);
                if (inicio.isBefore(agora)) {
                    continue;
                }
                if (!ocupado(dentistaId, inicio, inicio.plusMinutes(duracao), ignorarConsultaId)) {
                    livres.add(Fmt.I.hora(t));
                }
            }
        }
        livres.sort(String::compareTo);
        return livres;
    }

    // ------------------------------------------------------------------ horários de atendimento (RF10)

    @Override
    @Transactional(readOnly = true)
    public List<HorarioDTO> horarios(Long dentistaId) {
        return horarios.findByDentistaIdOrderByDiaSemanaAscHoraInicioAsc(dentistaId).stream()
                .map(h -> new HorarioDTO(h.getId(), h.getDiaSemana(), h.getHoraInicio(), h.getHoraFim()))
                .toList();
    }

    @Override
    @Transactional
    public void adicionarHorario(Long dentistaId, DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        if (dia == null || inicio == null || fim == null) {
            throw new IllegalArgumentException("Informe o dia da semana, o horário inicial e o final.");
        }
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("O horário final precisa ser depois do inicial.");
        }
        Dentista dentista = dentistas.findById(dentistaId)
                .orElseThrow(() -> new IllegalArgumentException("Dentista não encontrado."));
        boolean sobrepoe = horarios.findByDentistaIdAndDiaSemana(dentistaId, dia).stream()
                .anyMatch(h -> inicio.isBefore(h.getHoraFim()) && h.getHoraInicio().isBefore(fim));
        if (sobrepoe) {
            throw new IllegalArgumentException("Este período se sobrepõe a outro horário já cadastrado neste dia.");
        }
        horarios.save(new HorarioAtendimento(dentista, dia, inicio, fim));
    }

    @Override
    @Transactional
    public void removerHorario(Long dentistaId, Long horarioId) {
        HorarioAtendimento h = horarios.findById(horarioId)
                .filter(x -> x.getDentista().getId().equals(dentistaId))
                .orElseThrow(() -> new IllegalArgumentException("Horário não encontrado."));
        horarios.delete(h);
    }

    @Override
    @Transactional
    public void salvarHorarios(Long dentistaId, List<HorarioDTO> periodos) {
        Dentista dentista = dentistas.findById(dentistaId)
                .orElseThrow(() -> new IllegalArgumentException("Dentista não encontrado."));
        List<HorarioDTO> novos = periodos == null ? List.of() : periodos;
        for (HorarioDTO p : novos) {
            if (p.diaSemana() == null || p.inicio() == null || p.fim() == null) {
                throw new IllegalArgumentException("Informe o horário inicial e o final de todos os períodos.");
            }
            if (!p.fim().isAfter(p.inicio())) {
                throw new IllegalArgumentException(
                        "O horário final precisa ser depois do inicial (" + p.diaNome() + ").");
            }
        }
        for (DayOfWeek dia : DayOfWeek.values()) {
            List<HorarioDTO> doDia = novos.stream().filter(p -> p.diaSemana() == dia)
                    .sorted(Comparator.comparing(HorarioDTO::inicio)).toList();
            for (int i = 1; i < doDia.size(); i++) {
                if (doDia.get(i).inicio().isBefore(doDia.get(i - 1).fim())) {
                    throw new IllegalArgumentException(
                            "Os períodos de " + doDia.get(i).diaNome() + " se sobrepõem. Ajuste os horários.");
                }
            }
        }
        horarios.deleteAll(horarios.findByDentistaIdOrderByDiaSemanaAscHoraInicioAsc(dentistaId));
        horarios.flush();
        horarios.saveAll(novos.stream()
                .map(p -> new HorarioAtendimento(dentista, p.diaSemana(), p.inicio(), p.fim()))
                .toList());
    }
}
