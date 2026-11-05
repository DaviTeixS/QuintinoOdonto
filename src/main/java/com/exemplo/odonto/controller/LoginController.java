package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.config.SessaoUsuario;
import br.com.quintino.odonto.model.Usuario;
import br.com.quintino.odonto.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public LoginController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    // Mostrar a página de login
    @GetMapping("/login")
    public String paginaLogin(HttpSession sessao) {
        if (sessao.getAttribute(SessaoUsuario.CHAVE) != null) {
            return "redirect:/inicio"; // já está logado
        }
        return "login"; // Procura por login.html na pasta templates
    }

    // Conferir e-mail e senha
    @PostMapping("/login")
    public String entrar(@RequestParam("email") String email,
                         @RequestParam("senha") String senha,
                         HttpSession sessao,
                         Model model) {

        Usuario usuario = usuarios.findByEmail(email.trim().toLowerCase()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            model.addAttribute("erro", "E-mail ou senha inválidos.");
            model.addAttribute("email", email);
            return "login"; // mostra a página de novo, com o aviso
        }

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