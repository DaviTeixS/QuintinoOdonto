package br.com.quintino.odonto.service.mock;

import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.dto.ObservacaoDTO;
import br.com.quintino.odonto.dto.PacienteDTO;
import br.com.quintino.odonto.dto.SaudeDTO;
import br.com.quintino.odonto.dto.TratamentoDTO;
import br.com.quintino.odonto.service.PacienteService;
import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockPacienteService implements PacienteService {

    private final MockBanco banco;

    public MockPacienteService(MockBanco banco) {
        this.banco = banco;
    }

    @Override
    public List<ItemListaDTO> buscar(String termo) {
        String t = Fmt.I.semAcento(termo == null ? "" : termo.trim());
        return banco.pacientes.stream()
                .filter(p -> Fmt.I.semAcento(p.nome()).contains(t))
                .sorted(Comparator.comparing(PacienteDTO::nome, String.CASE_INSENSITIVE_ORDER))
                .map(p -> new ItemListaDTO(p.id(), p.nome(), proxima(p.id()), Fmt.I.iniciais(p.nome())))
                .toList();
    }

    private String proxima(Long pacienteId) {
        LocalDate hoje = LocalDate.now();
        return banco.consultas.stream()
                .filter(c -> c.pacienteId().equals(pacienteId) && "AGENDADA".equals(c.status())
                        && !c.data().isBefore(hoje))
                .min(Comparator.comparing(ConsultaDTO::data).thenComparing(ConsultaDTO::hora))
                .map(c -> "Próxima: " + Fmt.I.diaMes(c.data()) + " - " + Fmt.I.hora(c.hora()))
                .orElse("Sem consulta agendada");
    }

    @Override
    public Optional<PacienteDTO> buscarPorId(Long id) {
        return banco.pacientes.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    @Override
    public PacienteDTO salvar(PacienteDTO p) {
        if (p.id() == null) {
            PacienteDTO novo = new PacienteDTO(banco.seq.incrementAndGet(), p.nome(), p.sexo(),
                    p.nascimento(), p.celular(), p.rg(), p.cpf());
            banco.pacientes.add(novo);
            return novo;
        }
        banco.pacientes.replaceAll(x -> x.id().equals(p.id()) ? p : x);
        return p;
    }

    @Override
    public List<ConsultaDTO> historicoConsultas(Long pacienteId) {
        return banco.consultas.stream()
                .filter(c -> c.pacienteId().equals(pacienteId))
                .sorted(Comparator.comparing(ConsultaDTO::data).thenComparing(ConsultaDTO::hora).reversed())
                .toList();
    }

    @Override
    public Optional<TratamentoDTO> tratamentoAtual(Long pacienteId) {
        return Optional.ofNullable(banco.tratamentos.get(pacienteId));
    }

    @Override
    public SaudeDTO saude(Long pacienteId) {
        return banco.saudes.getOrDefault(pacienteId, new SaudeDTO(List.of(), List.of()));
    }

    @Override
    public void salvarSaude(Long pacienteId, SaudeDTO saude) {
        banco.saudes.put(pacienteId, saude);
    }

    @Override
    public List<ObservacaoDTO> observacoes(Long pacienteId) {
        return List.copyOf(banco.observacoes.getOrDefault(pacienteId, List.of()));
    }

    @Override
    public void adicionarObservacao(Long pacienteId, String texto, String autor) {
        banco.observacoes.compute(pacienteId, (k, atual) -> {
            List<ObservacaoDTO> nova = new ArrayList<>(atual == null ? List.of() : atual);
            nova.add(0, new ObservacaoDTO(banco.seq.incrementAndGet(), texto, LocalDate.now(), autor));
            return nova;
        });
    }
}
