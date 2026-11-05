package br.com.quintino.odonto.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Deixa o usuário logado disponível em todos os templates como ${usuario}. */
@ControllerAdvice
public class LayoutAdvice {

    @ModelAttribute("usuario")
    public SessaoUsuario usuario(HttpSession sessao) {
        return (SessaoUsuario) sessao.getAttribute(SessaoUsuario.CHAVE);
    }
}
