package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.dto.ProcedimentoDTO;
import java.util.List;
import java.util.Optional;

/** CONTRATO DO BACK-END: procedimentos do consultório. */
public interface ProcedimentoService {

    /** subtitulo = "Duração 30 min". */
    List<ItemListaDTO> buscar(String termo);

    Optional<ProcedimentoDTO> buscarPorId(Long id);

    /** id == null cria; id preenchido atualiza. */
    ProcedimentoDTO salvar(ProcedimentoDTO procedimento);
}
