package br.com.quintino.odonto.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

/**
 * Controle de acesso (RF01 a RF04, RNF01, RNF02):
 * - sem login: vai para /login (ou 401 nas chamadas /api);
 * - consultório (administrativo e recepção): cadastros, agendamentos, colaboradores e procedimentos;
 * - dentista: vê a própria agenda e as fichas, e é o único que registra observações.
 */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    /** Telas e APIs exclusivas do administrativo (colaboradores). */
    private static final Pattern SO_ADMIN = Pattern.compile(
            "^/colaboradores(/.*)?$|^/api/colaboradores(/.*)?$");

    /** Telas e APIs exclusivas do consultório, em qualquer método HTTP. */
    private static final Pattern SO_CONSULTORIO = Pattern.compile(
            "^/procedimentos(/.*)?$"
                    + "|^/api/(procedimentos|dentistas|horarios-livres)(/.*)?$"
                    + "|^/consultas/nova$"
                    + "|^/consultas/\\d+/(editar|cancelar|reagendar)$"
                    + "|^/pacientes/novo$"
                    + "|^/pacientes/\\d+/editar$");

    /** Gravações exclusivas do consultório (criar/alterar paciente e criar/alterar consulta). */
    private static final Pattern POST_SO_CONSULTORIO = Pattern.compile(
            "^/pacientes$|^/pacientes/\\d+$|^/consultas$|^/consultas/\\d+$");

    /** Observações clínicas: somente o dentista registra. */
    private static final Pattern SO_DENTISTA = Pattern.compile("^/pacientes/\\d+/observacoes/nova$");
    private static final Pattern POST_SO_DENTISTA = Pattern.compile("^/pacientes/\\d+/observacoes$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // caminho já decodificado e sem ";parametros", como o Spring MVC o enxerga (evita burlar as regras)
        String caminho = UrlPathHelper.defaultInstance.getPathWithinApplication(request).replaceAll("/+", "/");
        boolean api = caminho.startsWith("/api/");
        boolean post = "POST".equalsIgnoreCase(request.getMethod());
        String contexto = request.getContextPath();

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

        boolean soConsultorio = SO_CONSULTORIO.matcher(caminho).matches()
                || (post && POST_SO_CONSULTORIO.matcher(caminho).matches());
        boolean soDentista = SO_DENTISTA.matcher(caminho).matches()
                || (post && POST_SO_DENTISTA.matcher(caminho).matches());

        boolean soAdmin = SO_ADMIN.matcher(caminho).matches();

        boolean negado = (soAdmin && !usuario.isAdministrativo())
                || (soConsultorio && !usuario.isConsultorio())
                || (soDentista && !usuario.isDentista());
        if (negado) {
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
