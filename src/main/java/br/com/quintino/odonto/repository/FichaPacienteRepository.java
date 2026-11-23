package br.com.quintino.odonto.repository;

import br.com.quintino.odonto.model.FichaPaciente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FichaPacienteRepository extends JpaRepository<FichaPaciente, Long> {

    Optional<FichaPaciente> findByPacienteId(Long pacienteId);
}
