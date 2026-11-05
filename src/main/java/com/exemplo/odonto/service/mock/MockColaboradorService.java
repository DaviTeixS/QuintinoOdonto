package br.com.quintino.odonto.service.mock;

import br.com.quintino.odonto.dto.CadastroForm;
import br.com.quintino.odonto.dto.ColaboradorDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.service.ColaboradorService;
import br.com.quintino.odonto.util.Fmt;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockColaboradorService implements ColaboradorService {

    private final MockBanco banco;

    public MockColaboradorService(MockBanco banco) {
        this.banco = banco;
    }

    @Override
    public List<ItemListaDTO> buscar(String termo) {
        String t = Fmt.I.semAcento(termo == null ? "" : termo.trim());
        return banco.colaboradores.stream()
                .filter(c -> Fmt.I.semAcento(c.nome()).contains(t))
                .sorted(Comparator.comparing(ColaboradorDTO::nome, String.CASE_INSENSITIVE_ORDER))
                .map(c -> new ItemListaDTO(c.id(), c.nome(), Fmt.I.funcao(c.funcao()), Fmt.I.iniciais(c.nome())))
                .toList();
    }

    @Override
    public List<ItemListaDTO> dentistas() {
        return banco.colaboradores.stream()
                .filter(c -> "DENTISTA".equals(c.funcao()))
                .sorted(Comparator.comparing(ColaboradorDTO::nome, String.CASE_INSENSITIVE_ORDER))
                .map(c -> new ItemListaDTO(c.id(), c.nome(), c.cro(), Fmt.I.iniciais(c.nome())))
                .toList();
    }

    @Override
    public Optional<ColaboradorDTO> buscarPorId(Long id) {
        return banco.colaboradores.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public ColaboradorDTO salvar(ColaboradorDTO c) {
        if (c.id() == null) {
            ColaboradorDTO novo = new ColaboradorDTO(banco.seq.incrementAndGet(), c.nome(), c.funcao(),
                    c.email(), c.celular(), c.cpf(), c.cro());
            banco.colaboradores.add(novo);
            return novo;
        }
        banco.colaboradores.replaceAll(x -> x.id().equals(c.id()) ? c : x);
        return c;
    }

    @Override
    public void cadastrar(CadastroForm f) {
        boolean existe = banco.colaboradores.stream()
                .anyMatch(c -> c.email().equalsIgnoreCase(f.email()));
        if (existe) {
            throw new IllegalArgumentException("Já existe um colaborador cadastrado com este e-mail.");
        }
        boolean ehCpf = Fmt.I.digitos(f.documento()).length() == 11
                && f.documento().matches("[\\d.\\-\\s]+");
        String funcao = (f.funcao() == null || f.funcao().isBlank()) ? "RECEPCIONISTA" : f.funcao();
        salvar(new ColaboradorDTO(null, f.nome(), funcao, f.email(), f.telefone(),
                ehCpf ? f.documento() : "", ehCpf ? "" : f.documento()));
    }
}
