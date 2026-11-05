package br.com.quintino.odonto.config;
import br.com.quintino.odonto.model.*; import br.com.quintino.odonto.repository.UsuarioRepository; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.*; import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class DadosIniciais {
@Bean
CommandLineRunner criarUsuarioInicial(
        UsuarioRepository repository,
        PasswordEncoder encoder) {

    return args -> repository.save(
        new Usuario(
            "Consultório",
            "consultorio@quintino.com",
            encoder.encode("admin123"),
            Perfil.CONSULTORIO
        )
    );
}
}
