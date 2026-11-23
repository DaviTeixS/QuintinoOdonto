package br.com.quintino.odonto.repository;

import br.com.quintino.odonto.model.HorarioAtendimento;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioAtendimentoRepository extends JpaRepository<HorarioAtendimento, Long> {

    List<HorarioAtendimento> findByDentistaIdOrderByDiaSemanaAscHoraInicioAsc(Long dentistaId);

    List<HorarioAtendimento> findByDentistaIdAndDiaSemana(Long dentistaId, DayOfWeek diaSemana);
}
