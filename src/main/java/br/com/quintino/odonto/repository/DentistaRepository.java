package br.com.quintino.odonto.repository;

import br.com.quintino.odonto.model.Dentista;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DentistaRepository extends JpaRepository<Dentista, Long> {

    List<Dentista> findByAtivoTrueOrderByNomeAsc();
}
