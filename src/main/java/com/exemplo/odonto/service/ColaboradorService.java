package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.CadastroForm;
import br.com.quintino.odonto.dto.ColaboradorDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import java.util.List;
import java.util.Optional;

/** CONTRATO DO BACK-END: colaboradores (dentistas e recepcionistas). */
public interface ColaboradorService {

    /** subtitulo = função ("Dentista", "Recepcionista"). */
    List<ItemListaDTO> buscar(String termo);

    /** Somente dentistas (alimenta a lista suspensa do cadastro de procedimento). */
    List<ItemListaDTO> dentistas();

    Optional<ColaboradorDTO> buscarPorId(Long id);

    ColaboradorDTO salvar(ColaboradorDTO colaborador);

    /**
     * Cria o colaborador + usuário de login (gerar hash BCrypt da senha).
     * Lançar IllegalArgumentException("mensagem") se o e-mail já existir:
     * a mensagem é exibida para o usuário na tela.
     */
    void cadastrar(CadastroForm form);
}
