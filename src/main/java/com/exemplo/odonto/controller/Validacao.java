package br.com.quintino.odonto.controller;

import br.com.quintino.odonto.util.Fmt;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Pattern;

/** Validações simples dos formulários (o back pode reforçar/substituir). */
final class Validacao {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Validacao() {
    }

    static String texto(Map<String, String> v, String campo) {
        String valor = v.get(campo);
        return valor == null ? "" : valor.trim();
    }

    static boolean email(String s) {
        return EMAIL.matcher(s).matches();
    }

    static boolean telefone(String s) {
        int n = Fmt.I.digitos(s).length();
        return n == 10 || n == 11;
    }

    static boolean cpf(String s) {
        return Fmt.I.digitos(s).length() == 11;
    }

    /** Retorna null se vazio/inválido. */
    static LocalDate data(String s) {
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
