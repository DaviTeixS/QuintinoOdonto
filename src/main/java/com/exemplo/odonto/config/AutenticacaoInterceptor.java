package br.com.quintino.odonto.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege as telas: sem login vai para /login (ou 401 nas chamadas /api).
 * Colaboradores e procedimentos são exclusivos do perfil CONSULTORIO.
 */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    private static final List<String> SO_CONSULTORIO = List.of(
            "/colaboradores", "/procedimentos",
            "/api/colaboradores", "/api/procedimentos", "/api/dentistas");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String contexto = request.getContextPath();
        String caminho = request.getRequestURI().substring(contexto.length());
        boolean api = caminho.startsWith("/api/");

        Object sessao = request.getSession().getAttribute(SessaoUsuario.CHAVE);
        if (sessao == null) {
            if (api) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(contexto + "/login");
            }
            return false;
        }

        SessaoUsuario usuario = (SessaoUsuario) sessao;
        boolean restrito = SO_CONSULTORIO.stream().anyMatch(caminho::startsWith);
        if (restrito && !usuario.isConsultorio()) {
            if (api) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                response.sendRedirect(contexto + "/inicio");
            }
            return false;
        }
        return true;
    }
}
