package br.com.quintino.odonto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacaoInterceptor autenticacao;

    public WebConfig(AutenticacaoInterceptor autenticacao) {
        this.autenticacao = autenticacao;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacao)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/cadastro", "/css/**", "/js/**", "/img/**",
                        "/h2-console/**", "/error", "/favicon.ico");
    }
}
