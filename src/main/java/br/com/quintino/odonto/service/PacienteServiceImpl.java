package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.dto.ObservacaoDTO;
import br.com.quintino.odonto.dto.PacienteDTO;
import br.com.quintino.odonto.dto.SaudeDTO;
import br.com.quintino.odonto.dto.TratamentoDTO;
import br.com.quintino.odonto.model.Consulta;
import br.com.quintino.odonto.model.FichaPaciente;
import br.com.quintino.odonto.model.Observacao;
import br.com.quintino.odonto.model.Paciente;
import br.com.quintino.odonto.model.StatusConsulta;
import br.com.quintino.odonto.repository.ConsultaRepository;
import br.com.quintino.odonto.repository.FichaPacienteRepository;
import br.com.quintino.odonto.repository.ObservacaoRepository;
import br.com.quintino.odonto.repository.PacienteRepository;
import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Pacientes e tudo que fica na ficha do paciente (saúde, consultas, tratamento e observações). */
@Service
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacientes;
    private final FichaPacienteRepository fichas;
    private final ConsultaRepository consultas;
    private final ObservacaoRepository observacoes;

    public PacienteServiceImpl(PacienteRepository pacientes, FichaPacienteRepository fichas,
                               ConsultaRepository consultas, ObservacaoRepository observacoes) {
        this.pacientes = pacientes;
        this.fichas = fichas;
        this.consultas = consultas;
        this.observacoes = observacoes;
    }

    private PacienteDTO paraDto(Paciente p) {
        return new PacienteDTO(p.getId(), p.getNome(), Conversores.nvl(p.getSexo()), p.getDataNascimento(),
                Conversores.nvl(p.getTelefone()), Conversores.nvl(p.getRg()), Conversores.nvl(p.getCpf()));
    }

    private String proxima(Long pacienteId) {
        return consultas.findFirstByPacienteIdAndStatusAndDataHoraGreaterThanEqualOrderByDataHoraAsc(
                        pacienteId, StatusConsulta.AGENDADA, LocalDateTime.now())
                .map(c -> "Próxima: " + Fmt.I.diaMes(c.getDataHora().toLocalDate())
                        + " - " + Fmt.I.hora(c.getDataHora().toLocalTime()))
                .orElse("Sem consulta agendada");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemListaDTO> buscar(String termo) {
        String t = Fmt.I.semAcento(termo == null ? "" : termo.trim());
        return pacientes.findByAtivoTrueOrderByNomeAsc().stream()
                .filter(p -> Fmt.I.semAcento(p.getNome()).contains(t))
                .map(p -> new ItemListaDTO(p.getId(), p.getNome(), proxima(p.getId()),
                        Fmt.I.iniciais(p.getNome())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PacienteDTO> buscarPorId(Long id) {
        return pacientes.findById(id).filter(Paciente::isAtivo).map(this::paraDto);
    }

    @Override
    @Transactional
    public PacienteDTO salvar(PacienteDTO dto) {
        Paciente p;
        boolean duplicado;
        if (dto.id() == null) {
            p = new Paciente();
            duplicado = pacientes.existsByCpf(dto.cpf());
        } else {
            p = pacientes.findById(dto.id())
                    .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
            duplicado = pacientes.existsByCpfAndIdNot(dto.cpf(), dto.id());
        }
        if (duplicado) {
            throw new IllegalArgumentException("Já existe um paciente cadastrado com este CPF.");
        }
        p.setNome(dto.nome());
        p.setSexo(dto.sexo());
        p.setDataNascimento(dto.nascimento());
        p.setTelefone(dto.celular());
        p.setRg(dto.rg());
        p.setCpf(dto.cpf());
        return paraDto(pacientes.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaDTO> historicoConsultas(Long pacienteId) {
        return consultas.findByPacienteIdOrderByDataHoraDesc(pacienteId).stream()
                .map(Conversores::paraDto).toList();
    }

    /**
     * O diagrama de classes não tem a entidade "tratamento": ele é derivado das consultas.
     * Considera as consultas com dente informado e, se houver uma agendada, usa o dente dela;
     * as etapas são todas as consultas (não canceladas) desse dente, da mais antiga para a mais nova.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<TratamentoDTO> tratamentoAtual(Long pacienteId) {
        List<Consulta> comDente = consultas.findByPacienteIdOrderByDataHoraDesc(pacienteId).stream()
                .filter(c -> c.getStatus() != StatusConsulta.CANCELADA)
                .filter(c -> c.getDente() != null && !c.getDente().isBlank())
                .toList();
        Optional<Consulta> proxima = comDente.stream()
                .filter(c -> c.getStatus() == StatusConsulta.AGENDADA)
                .min(Comparator.comparing(Consulta::getDataHora));
        if (proxima.isEmpty()) {
            return Optional.empty();
        }
        String dente = proxima.get().getDente();
        List<ConsultaDTO> etapas = comDente.stream()
                .filter(c -> dente.equals(c.getDente()))
                .sorted(Comparator.comparing(Consulta::getDataHora))
                .map(Conversores::paraDto)
                .toList();
        return Optional.of(new TratamentoDTO(pacienteId, "Tratamento do dente " + dente, "EM_ANDAMENTO", etapas));
    }

    private static List<String> linhas(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return Arrays.stream(texto.split("\\R")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaudeDTO saude(Long pacienteId) {
        return fichas.findByPacienteId(pacienteId)
                .map(f -> new SaudeDTO(linhas(f.getProblemasDeSaude()), linhas(f.getSintomas())))
                .orElse(new SaudeDTO(List.of(), List.of()));
    }

    @Override
    @Transactional
    public void salvarSaude(Long pacienteId, SaudeDTO saude) {
        FichaPaciente ficha = fichas.findByPacienteId(pacienteId).orElseGet(() -> {
            Paciente p = pacientes.findById(pacienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
            return new FichaPaciente(p);
        });
        ficha.setProblemasDeSaude(String.join("\n", saude.problemas()));
        ficha.setSintomas(String.join("\n", saude.sintomas()));
        fichas.save(ficha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacaoDTO> observacoes(Long pacienteId) {
        return observacoes.findByPacienteIdOrderByDataHoraDescIdDesc(pacienteId).stream()
                .map(o -> new ObservacaoDTO(o.getId(), o.getTexto(), o.getDataHora().toLocalDate(),
                        Conversores.nvl(o.getAutor())))
                .toList();
    }

    /** Só inclui: a observação não pode ser alterada nem removida depois de salva (RNF10). */
    @Override
    @Transactional
    public void adicionarObservacao(Long pacienteId, String texto, String autor) {
        Paciente paciente = pacientes.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
        Consulta consulta = consultas
                .findFirstByPacienteIdAndStatusNotAndDataHoraLessThanEqualOrderByDataHoraDesc(
                        pacienteId, StatusConsulta.CANCELADA, LocalDateTime.now())
                .orElse(null);
        observacoes.save(new Observacao(paciente, consulta, texto, autor));
    }
}
