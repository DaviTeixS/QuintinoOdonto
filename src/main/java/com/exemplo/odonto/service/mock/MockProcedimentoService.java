package br.com.quintino.odonto.service.mock;

import br.com.quintino.odonto.dto.ColaboradorDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.dto.ProcedimentoDTO;
import br.com.quintino.odonto.service.ProcedimentoService;
import br.com.quintino.odonto.util.Fmt;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockProcedimentoService implements ProcedimentoService {

    private final MockBanco banco;

    public MockProcedimentoService(MockBanco banco) {
        this.banco = banco;
    }

    @Override
    public List<ItemListaDTO> buscar(String termo) {
        String t = Fmt.I.semAcento(termo == null ? "" : termo.trim());
        return banco.procedimentos.stream()
                .filter(p -> Fmt.I.semAcento(p.nome()).contains(t))
                .sorted(Comparator.comparing(ProcedimentoDTO::nome, String.CASE_INSENSITIVE_ORDER))
                .map(p -> new ItemListaDTO(p.id(), p.nome(), "Duração " + Fmt.I.duracaoCurta(p.duracaoMinutos()),
                        Fmt.I.iniciais(p.nome())))
                .toList();
    }

    @Override
    public Optional<ProcedimentoDTO> buscarPorId(Long id) {
        return banco.procedimentos.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    @Override
    public ProcedimentoDTO salvar(ProcedimentoDTO p) {
        List<Long> ids = p.dentistaIds() == null ? List.of() : p.dentistaIds();
        List<String> nomes = banco.colaboradores.stream()
                .filter(c -> ids.contains(c.id()))
                .map(ColaboradorDTO::nome)
                .toList();
        ProcedimentoDTO completo = new ProcedimentoDTO(
                p.id() == null ? banco.seq.incrementAndGet() : p.id(),
                p.nome(), p.duracaoMinutos(), p.descricao(), ids, nomes);
        if (p.id() == null) {
            banco.procedimentos.add(completo);
        } else {
            banco.procedimentos.replaceAll(x -> x.id().equals(p.id()) ? completo : x);
        }
        return completo;
    }
}
