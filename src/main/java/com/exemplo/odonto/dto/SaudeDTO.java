package br.com.quintino.odonto.dto;

import java.util.List;

/** Um item por linha: problemas de saúde e sintomas informados. */
public record SaudeDTO(List<String> problemas, List<String> sintomas) {
}
