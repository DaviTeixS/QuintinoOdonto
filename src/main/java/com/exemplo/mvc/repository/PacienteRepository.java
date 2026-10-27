package com.exemplo.mvc.repository;

import com.exemplo.mvc.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // Métodos CRUD básicos já estão inclusos aqui automaticamente
}