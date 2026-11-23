package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.dto.CadastroForm;
import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Validação do formulário de cadastro de colaborador (público e interno). */
final class CadastroValidador {

    private CadastroValidador() {
    }

    static void validar(Map<String, String> v, boolean exigeFuncao, List<String> erros) {
        String documento = Validacao.texto(v, "documento");
        String email = Validacao.texto(v, "email");
        String senha = v.getOrDefault("senha", "");
        LocalDate nascimento = Validacao.data(Validacao.texto(v, "nascimento"));

        if (Validacao.texto(v, "nome").length() < 3) {
            erros.add("Informe o nome completo.");
        }
        boolean soNumeros = documento.matches("[\\d.\\-\\s]+");
        if (documento.isEmpty() || (soNumeros && Fmt.I.digitos(documento).length() != 11)) {
            erros.add("Informe um CPF com 11 dígitos ou o registro do CRO (ex.: CRO-SP 12345).");
        }
        if (!Validacao.telefone(Validacao.texto(v, "telefone"))) {
            erros.add("Informe o telefone com DDD (10 ou 11 dígitos).");
        }
        if (nascimento == null || nascimento.isAfter(LocalDate.now())) {
            erros.add("Informe uma data de nascimento válida.");
        }
        if (!Validacao.email(email)) {
            erros.add("Informe um e-mail válido.");
        } else if (!email.equalsIgnoreCase(Validacao.texto(v, "confirmaEmail"))) {
            erros.add("Os e-mails não são iguais.");
        }
        if (senha.length() < 6) {
            erros.add("A senha precisa ter pelo menos 6 caracteres.");
        } else if (!senha.equals(v.getOrDefault("confirmaSenha", ""))) {
            erros.add("As senhas não são iguais.");
        }
        if (exigeFuncao) {
            String f = Validacao.texto(v, "funcao");
            if (!f.equals("DENTISTA") && !f.equals("RECEPCIONISTA")) {
                erros.add("Selecione a função do colaborador.");
            }
        }
    }

    static CadastroForm montar(Map<String, String> v) {
        String funcao = Validacao.texto(v, "funcao");
        return new CadastroForm(
                Validacao.texto(v, "nome"),
                Validacao.texto(v, "documento"),
                Validacao.texto(v, "telefone"),
                Validacao.data(Validacao.texto(v, "nascimento")),
                Validacao.texto(v, "email").toLowerCase(),
                v.getOrDefault("senha", ""),
                funcao.isEmpty() ? null : funcao);
    }

    /** Copia os campos digitados, sem as senhas, para repreencher o formulário após um erro. */
    static Map<String, String> semSenhas(Map<String, String> v) {
        Map<String, String> copia = new java.util.HashMap<>(v);
        copia.remove("senha");
        copia.remove("confirmaSenha");
        return copia;
    }
}
