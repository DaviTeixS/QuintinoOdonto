package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.config.SessaoUsuario;
import br.com.quintino.odonto.model.Funcionario;
import br.com.quintino.odonto.repository.FuncionarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final FuncionarioRepository funcionarios;
    private final PasswordEncoder passwordEncoder;

    public LoginController(FuncionarioRepository funcionarios, PasswordEncoder passwordEncoder) {
        this.funcionarios = funcionarios;
        this.passwordEncoder = passwordEncoder;
    }

    // Mostrar a página de login
    @GetMapping("/login")
    public String paginaLogin(HttpSession sessao) {
        if (sessao.getAttribute(SessaoUsuario.CHAVE) != null) {
            return "redirect:/inicio"; // já está logado
        }
        return "login";
    }

    // Conferir e-mail e senha (RF01/RF02: identifica o perfil do usuário)
    @PostMapping("/login")
    public String entrar(@RequestParam("email") String email,
                         @RequestParam("senha") String senha,
                         HttpServletRequest request,
                         Model model) {
        Funcionario usuario = funcionarios.findByEmailIgnoreCase(email.trim())
                .filter(Funcionario::isAtivo)
                .orElse(null);
        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            model.addAttribute("erro", "E-mail ou senha inválidos.");
            model.addAttribute("email", email);
            return "login";
        }
        request.getSession().invalidate();            // nova sessão a cada login
        HttpSession sessao = request.getSession(true);
        sessao.setAttribute(SessaoUsuario.CHAVE,
                new SessaoUsuario(usuario.getId(), usuario.getNome(), usuario.getPerfil()));
        return "redirect:/inicio";
    }

    // Sair
    @PostMapping("/logout")
    public String sair(HttpSession sessao) {
        sessao.invalidate();
        return "redirect:/login";
    }
}
