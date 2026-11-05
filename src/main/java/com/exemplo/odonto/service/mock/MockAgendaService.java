package br.com.quintino.odonto.service.mock;

import br.com.quintino.odonto.dto.ConsultaDTO;
import br.com.quintino.odonto.dto.DiaAgendaDTO;
import br.com.quintino.odonto.service.AgendaService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockAgendaService implements AgendaService {

    private final MockBanco banco;

    public MockAgendaService(MockBanco banco) {
        this.banco = banco;
    }

    @Override
    public List<DiaAgendaDTO> diasComConsulta(int ano, int mes) {
        Map<Integer, Long> porDia = banco.consultas.stream()
                .filter(c -> !"CANCELADA".equals(c.status())
                        && c.data().getYear() == ano && c.data().getMonthValue() == mes)
                .collect(Collectors.groupingBy(c -> c.data().getDayOfMonth(), TreeMap::new, Collectors.counting()));
        return porDia.entrySet().stream().map(e -> new DiaAgendaDTO(e.getKey(), e.getValue())).toList();
    }

    @Override
    public List<ConsultaDTO> consultasDoDia(LocalDate data) {
        return banco.consultas.stream()
                .filter(c -> c.data().equals(data) && !"CANCELADA".equals(c.status()))
                .sorted(Comparator.comparing(ConsultaDTO::hora))
                .toList();
    }
}
