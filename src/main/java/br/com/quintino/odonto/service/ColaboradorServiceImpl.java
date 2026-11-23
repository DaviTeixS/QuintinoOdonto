package br.com.quintino.odonto.service;

import br.com.quintino.odonto.dto.CadastroForm;
import br.com.quintino.odonto.dto.ColaboradorDTO;
import br.com.quintino.odonto.dto.ItemListaDTO;
import br.com.quintino.odonto.model.Dentista;
import br.com.quintino.odonto.model.Funcionario;
import br.com.quintino.odonto.model.Perfil;
import br.com.quintino.odonto.repository.DentistaRepository;
import br.com.quintino.odonto.repository.FuncionarioRepository;
import br.com.quintino.odonto.util.Fmt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Colaboradores (dentistas e recepcionistas). O administrador do sistema não aparece nas listas. */
@Service
public class ColaboradorServiceImpl implements ColaboradorService {

    private static final String E_MAIL_REPETIDO = "Já existe um colaborador cadastrado com este e-mail.";

    private final FuncionarioRepository funcionarios;
    private final DentistaRepository dentistas;
    private final PasswordEncoder encoder;

    public ColaboradorServiceImpl(FuncionarioRepository funcionarios, DentistaRepository dentistas,
                                  PasswordEncoder encoder) {
        this.funcionarios = funcionarios;
        this.dentistas = dentistas;
        this.encoder = encoder;
    }

    private static String funcaoDe(Funcionario f) {
        return f instanceof Dentista ? "DENTISTA" : "RECEPCIONISTA";
    }

    private static boolean listavel(Funcionario f) {
        return f.isAtivo() && f.getPerfil() != Perfil.ADMINISTRATIVO;
    }

    private ColaboradorDTO paraDto(Funcionario f) {
        String cro = f instanceof Dentista d ? Conversores.nvl(d.getCro()) : "";
        return new ColaboradorDTO(f.getId(), f.getNome(), funcaoDe(f), f.getEmail(),
                Conversores.nvl(f.getTelefone()), Conversores.nvl(f.getCpf()), cro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemListaDTO> buscar(String termo) {
        String t = Fmt.I.semAcento(termo == null ? "" : termo.trim());
        return funcionarios.findAll(Sort.by("nome")).stream()
                .filter(ColaboradorServiceImpl::listavel)
                .filter(f -> Fmt.I.semAcento(f.getNome()).contains(t))
                .map(f -> new ItemListaDTO(f.getId(), f.getNome(), Fmt.I.funcao(funcaoDe(f)),
                        Fmt.I.iniciais(f.getNome())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemListaDTO> dentistas() {
        return dentistas.findByAtivoTrueOrderByNomeAsc().stream()
                .map(d -> new ItemListaDTO(d.getId(), d.getNome(), Conversores.nvl(d.getCro()),
                        Fmt.I.iniciais(d.getNome())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ColaboradorDTO> buscarPorId(Long id) {
        return funcionarios.findById(id).filter(ColaboradorServiceImpl::listavel).map(this::paraDto);
    }

    @Override
    @Transactional
    public ColaboradorDTO salvar(ColaboradorDTO c) {
        if (c.id() == null) {
            throw new IllegalArgumentException("Use o cadastro para criar um colaborador novo.");
        }
        Funcionario f = funcionarios.findById(c.id()).filter(ColaboradorServiceImpl::listavel)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado."));
        if (!funcaoDe(f).equals(c.funcao())) {
            throw new IllegalArgumentException(
                    "Não é possível mudar a função de um colaborador já cadastrado. Cadastre um novo colaborador.");
        }
        String email = c.email().trim().toLowerCase();
        if (funcionarios.existsByEmailIgnoreCaseAndIdNot(email, f.getId())) {
            throw new IllegalArgumentException(E_MAIL_REPETIDO);
        }
        f.setNome(c.nome());
        f.setEmail(email);
        f.setTelefone(c.celular());
        f.setCpf(c.cpf());
        if (f instanceof Dentista d) {
            d.setCro(c.cro());
        }
        return paraDto(funcionarios.save(f));
    }

    @Override
    @Transactional
    public void cadastrar(CadastroForm form) {
        String email = form.email().trim().toLowerCase();
        if (funcionarios.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(E_MAIL_REPETIDO);
        }
        // O campo "documento" do formulário aceita CPF (só números) ou o registro do CRO.
        String documento = Conversores.nvl(form.documento()).trim();
        boolean ehCpf = documento.matches("[\\d.\\-\\s]+");
        boolean dentista = "DENTISTA".equals(form.funcao());

        Funcionario f;
        if (dentista) {
            if (ehCpf) {
                throw new IllegalArgumentException(
                        "Para dentistas, informe o registro do CRO no campo de documento (ex.: CRO-SP 12345).");
            }
            Dentista d = new Dentista();
            d.setCro(documento);
            f = d;
        } else {
            if (!ehCpf) {
                throw new IllegalArgumentException("Para este cadastro, informe o CPF no campo de documento.");
            }
            f = new Funcionario();
            f.setPerfil(Perfil.RECEPCAO);
            f.setCpf(documento);
        }
        f.setCargo(dentista ? "Dentista" : "Recepcionista");
        f.setNome(form.nome().trim());
        f.setEmail(email);
        f.setTelefone(form.telefone());
        f.setSenhaHash(encoder.encode(form.senha()));
        f.setAtivo(true);
        funcionarios.save(f);
    }
}
