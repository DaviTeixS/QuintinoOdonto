package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.dto.ObservacaoDTO;
import br.com.quintino.odonto.dto.PacienteDTO;
import br.com.quintino.odonto.dto.SaudeDTO;
import br.com.quintino.odonto.dto.TratamentoDTO;
import java.util.List;
import java.util.Optional;

/**
 * CONTRATO DO BACK-END: pacientes e tudo que fica na ficha do paciente.
 * O front já chama estes métodos; basta criar uma classe @Service que implemente a interface.
 */
public interface PacienteService {

    /** Lista pacientes pelo nome (termo vazio = todos). subtitulo = "Próxima: dd/MM - HH:mm". */
    List<ItemListaDTO> buscar(String termo);

    Optional<PacienteDTO> buscarPorId(Long id);

    /** id == null cria; id preenchido atualiza. Retorna o paciente salvo (com id). */
    PacienteDTO salvar(PacienteDTO paciente);

    /** Histórico de consultas do paciente, da mais recente para a mais antiga. */
    List<ConsultaDTO> historicoConsultas(Long pacienteId);

    /** Tratamento em andamento do paciente (vazio se não houver). */
    Optional<TratamentoDTO> tratamentoAtual(Long pacienteId);

    SaudeDTO saude(Long pacienteId);

    void salvarSaude(Long pacienteId, SaudeDTO saude);

    /** Da mais recente para a mais antiga. */
    List<ObservacaoDTO> observacoes(Long pacienteId);

    void adicionarObservacao(Long pacienteId, String texto, String autor);
}
