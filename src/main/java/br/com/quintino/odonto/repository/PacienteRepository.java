package br.com.quintino.odonto.repository;

import br.com.quintino.odonto.model.Paciente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findByAtivoTrueOrderByNomeAsc();

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
