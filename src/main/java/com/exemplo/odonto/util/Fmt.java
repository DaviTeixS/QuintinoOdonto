package br.com.quintino.odonto.util;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Funções de formatação usadas nos templates (${@fmt.data(...)}) e nos DTOs.
 * É um bean chamado "fmt" para poder ser usado direto no Thymeleaf.
 */
@Component("fmt")
public class Fmt {

    /** Instância para usar dentro de classes comuns (records, serviços). */
    public static final Fmt I = new Fmt();

    private static final Set<String> LIGACOES = Set.of("de", "da", "do", "dos", "das", "e");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** "Carla Quintino" -> "CQ" | "Barbara de Oliveira" -> "BO" */
    public String iniciais(String nome) {
        if (nome == null || nome.isBlank()) {
            return "?";
        }
        List<String> partes = new ArrayList<>();
        for (String p : nome.trim().split("\\s+")) {
            if (!LIGACOES.contains(p.toLowerCase())) {
                partes.add(p);
            }
        }
        if (partes.isEmpty()) {
            return "?";
        }
        String r = partes.get(0).substring(0, 1);
        if (partes.size() > 1) {
            r += partes.get(1).substring(0, 1);
        } else if (partes.get(0).length() > 1) {
            r += partes.get(0).substring(1, 2);
        }
        return r.toUpperCase();
    }

    public String data(LocalDate d) {
        return d == null ? "" : d.format(DATA);
    }

    public String diaMes(LocalDate d) {
        return d == null ? "" : d.format(DIA_MES);
    }

    public String hora(LocalTime h) {
        return h == null ? "" : h.format(HORA);
    }

    /** 30 -> "30 min" | 60 -> "1h" | 90 -> "1h30" */
    public String duracaoCurta(int min) {
        if (min < 60) {
            return min + " min";
        }
        int h = min / 60;
        int m = min % 60;
        return m == 0 ? h + "h" : h + "h" + String.format("%02d", m);
    }

    /** 30 -> "30 minutos" | 60 -> "1 hora" | 90 -> "1 hora e 30 minutos" */
    public String duracaoLonga(int min) {
        if (min < 60) {
            return min + " minutos";
        }
        int h = min / 60;
        int m = min % 60;
        String horas = h == 1 ? "1 hora" : h + " horas";
        return m == 0 ? horas : horas + " e " + m + " minutos";
    }

    public String funcao(String f) {
        if (f == null) {
            return "";
        }
        return switch (f) {
            case "DENTISTA" -> "Dentista";
            case "RECEPCIONISTA" -> "Recepcionista";
            case "CONSULTORIO" -> "Consultório";
            default -> f;
        };
    }

    public String status(String s) {
        if (s == null) {
            return "";
        }
        return switch (s) {
            case "AGENDADA" -> "Agendado";
            case "REALIZADA" -> "Realizada";
            case "CANCELADA" -> "Cancelada";
            case "EM_ANDAMENTO" -> "Em andamento";
            case "CONCLUIDO" -> "Concluído";
            default -> s;
        };
    }

    /** Nome da variação de cor do selo no CSS (.selo--realizada etc.). */
    public String statusClasse(String s) {
        if (s == null) {
            return "agendada";
        }
        return switch (s) {
            case "REALIZADA", "CONCLUIDO" -> "realizada";
            case "CANCELADA" -> "cancelada";
            case "EM_ANDAMENTO" -> "andamento";
            default -> "agendada";
        };
    }

    public String semAcento(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }

    public String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
