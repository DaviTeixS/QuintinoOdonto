package br.com.quintino.odonto.repository;

import br.com.quintino.odonto.model.Observacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservacaoRepository extends JpaRepository<Observacao, Long> {

    List<Observacao> findByPacienteIdOrderByDataHoraDescIdDesc(Long pacienteId);
}
