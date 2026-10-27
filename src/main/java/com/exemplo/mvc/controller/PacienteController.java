package com.exemplo.mvc.controller;
import com.exemplo.mvc.model.Paciente;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.exemplo.mvc.repository.PacienteRepository;

    @Controller
    public class PacienteController {
    private final PacienteRepository pacienteRepository;

    // Injeção de dependência via construtor
    public PacienteController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @GetMapping("/pacientes")
    public String listarPacientes(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findAll());
        model.addAttribute("paciente", new Paciente()); // Objeto vazio para o formulário
        return "pacientes"; // Procura por paciente.html na pasta templates
    }

    // Salvar novo paciente no Banco de Dados
    @PostMapping("/pacientes")
    public String salvarPaciente(Paciente paciente) {
        pacienteRepository.save(paciente);
        return "redirect:/pacientes"; // Atualiza a página
    }
    // Carrega um paciente no formulário para edição
    @GetMapping("/pacientes/editar/{id}")
    public String editarPaciente(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente inválido: " + id));

        model.addAttribute("pacientes", pacienteRepository.findAll()); // a tabela continua aparecendo
        model.addAttribute("paciente", paciente);                       // formulário vem preenchido
        return "paciente";
    }

    // Exclui um paciente pelo id
    @PostMapping("/paciente/excluir/{id}")
    public String excluirPaciente(@PathVariable Long id) {
        pacienteRepository.deleteById(id);
        return "redirect:/pacientes";
    }
}