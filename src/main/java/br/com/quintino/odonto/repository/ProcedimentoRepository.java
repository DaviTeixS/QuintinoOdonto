package br.com.quintino.odonto.repository;

import br.com.quintino.odonto.model.Procedimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, Long> {

    List<Procedimento> findByAtivoTrueOrderByNomeAsc();
}
