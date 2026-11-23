package br.com.quintino.odonto.repository;

import br.com.quintino.odonto.model.Consulta;
import br.com.quintino.odonto.model.StatusConsulta;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPacienteIdOrderByDataHoraDesc(Long pacienteId);

    /** Consultas de todos os dentistas no intervalo [inicio, fim), exceto as do status informado. */
    List<Consulta> findByDataHoraGreaterThanEqualAndDataHoraLessThanAndStatusNotOrderByDataHoraAsc(
            LocalDateTime inicio, LocalDateTime fim, StatusConsulta status);

    /** O mesmo, mas só de um dentista. */
    List<Consulta> findByDentistaIdAndDataHoraGreaterThanEqualAndDataHoraLessThanAndStatusNotOrderByDataHoraAsc(
            Long dentistaId, LocalDateTime inicio, LocalDateTime fim, StatusConsulta status);

    /** Próxima consulta com o status informado a partir de um instante. */
    Optional<Consulta> findFirstByPacienteIdAndStatusAndDataHoraGreaterThanEqualOrderByDataHoraAsc(
            Long pacienteId, StatusConsulta status, LocalDateTime a);

    /** Consulta mais recente (que já começou) diferente do status informado. */
    Optional<Consulta> findFirstByPacienteIdAndStatusNotAndDataHoraLessThanEqualOrderByDataHoraDesc(
            Long pacienteId, StatusConsulta status, LocalDateTime ate);
}
