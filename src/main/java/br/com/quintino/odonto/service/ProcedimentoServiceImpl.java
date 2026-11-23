package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.dto.ProcedimentoDTO;
import br.com.quintino.odonto.model.Dentista;
import br.com.quintino.odonto.model.Procedimento;
import br.com.quintino.odonto.repository.DentistaRepository;
import br.com.quintino.odonto.repository.ProcedimentoRepository;
import br.com.quintino.odonto.util.Fmt;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Procedimentos oferecidos pelo consultório e os dentistas que os realizam. */
@Service
public class ProcedimentoServiceImpl implements ProcedimentoService {

    private final ProcedimentoRepository procedimentos;
    private final DentistaRepository dentistas;

    public ProcedimentoServiceImpl(ProcedimentoRepository procedimentos, DentistaRepository dentistas) {
        this.procedimentos = procedimentos;
        this.dentistas = dentistas;
    }

    private ProcedimentoDTO paraDto(Procedimento p) {
        List<Dentista> ordenados = p.getDentistas().stream()
                .sorted(Comparator.comparing(Dentista::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new ProcedimentoDTO(p.getId(), p.getNome(), p.getDuracaoMinutos(),
                Conversores.nvl(p.getDescricao()),
                ordenados.stream().map(Dentista::getId).toList(),
                ordenados.stream().map(Dentista::getNome).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemListaDTO> buscar(String termo) {
        String t = Fmt.I.semAcento(termo == null ? "" : termo.trim());
        return procedimentos.findByAtivoTrueOrderByNomeAsc().stream()
                .filter(p -> Fmt.I.semAcento(p.getNome()).contains(t))
                .map(p -> new ItemListaDTO(p.getId(), p.getNome(),
                        "Duração " + Fmt.I.duracaoCurta(p.getDuracaoMinutos()), Fmt.I.iniciais(p.getNome())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcedimentoDTO> buscarPorId(Long id) {
        return procedimentos.findById(id).filter(Procedimento::isAtivo).map(this::paraDto);
    }

    @Override
    @Transactional
    public ProcedimentoDTO salvar(ProcedimentoDTO dto) {
        Procedimento p;
        if (dto.id() == null) {
            p = new Procedimento();
            p.ativar();
        } else {
            p = procedimentos.findById(dto.id())
                    .orElseThrow(() -> new IllegalArgumentException("Procedimento não encontrado."));
        }
        p.setNome(dto.nome());
        p.setDescricao(dto.descricao());
        p.setDuracaoMinutos(dto.duracaoMinutos());
        List<Long> ids = dto.dentistaIds() == null ? List.of() : dto.dentistaIds();
        p.setDentistas(new HashSet<>(dentistas.findAllById(ids)));
        return paraDto(procedimentos.save(p));
    }
}
