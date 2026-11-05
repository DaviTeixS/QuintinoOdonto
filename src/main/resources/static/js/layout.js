/* =====================================================================
   JS GLOBAL: menu lateral, máscaras de campos e confirmação de e-mail/senha
   ===================================================================== */
(function () {
  'use strict';

  /* ---------- Menu lateral ---------- */
  var botao = document.getElementById('menuBtn');
  var menu = document.getElementById('menuLateral');
  var overlay = document.getElementById('menuOverlay');
  var fechar = document.getElementById('menuFechar');

  function alternar(abrir) {
    if (!menu) return;
    menu.classList.toggle('aberto', abrir);
    overlay.classList.toggle('visivel', abrir);
    document.body.classList.toggle('sem-rolagem', abrir);
    botao.setAttribute('aria-expanded', String(abrir));
    if (abrir) { var primeiro = menu.querySelector('a'); if (primeiro) primeiro.focus(); }
    else { botao.focus(); }
  }

  if (botao && menu) {
    botao.addEventListener('click', function () { alternar(true); });
    overlay.addEventListener('click', function () { alternar(false); });
    if (fechar) fechar.addEventListener('click', function () { alternar(false); });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && menu.classList.contains('aberto')) alternar(false);
    });

    // marca o item da página atual
    var base = (document.body.getAttribute('data-base') || '').replace(/\/$/, '');
    var caminho = location.pathname.substring(base.length);
    menu.querySelectorAll('a[data-rota]').forEach(function (a) {
      var rota = a.getAttribute('data-rota');
      if (caminho === rota || caminho.indexOf(rota + '/') === 0) a.setAttribute('aria-current', 'page');
    });
  }

  /* ---------- Máscaras ---------- */
  function soDigitos(v) { return v.replace(/\D/g, ''); }

  function mascaraCpf(v) {
    var d = soDigitos(v).slice(0, 11);
    if (d.length > 9) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})/, '$1.$2.$3-$4');
    if (d.length > 6) return d.replace(/(\d{3})(\d{3})(\d{1,3})/, '$1.$2.$3');
    if (d.length > 3) return d.replace(/(\d{3})(\d{1,3})/, '$1.$2');
    return d;
  }

  function mascaraTelefone(v) {
    var d = soDigitos(v).slice(0, 11);
    if (d.length > 7) {
      var corte = d.length === 11 ? 7 : 6;
      return d.slice(0, 2) + ' ' + d.slice(2, corte) + '-' + d.slice(corte);
    }
    if (d.length > 2) return d.slice(0, 2) + ' ' + d.slice(2);
    return d;
  }

  // CPF/CRO: se só tem números, aplica máscara de CPF; se tem letras (CRO-SP 12345) deixa como está
  function mascaraDocumento(v) {
    return /^[\d.\-\s]*$/.test(v) ? mascaraCpf(v) : v;
  }

  var mascaras = { cpf: mascaraCpf, telefone: mascaraTelefone, documento: mascaraDocumento };

  document.querySelectorAll('input[data-mascara]').forEach(function (campo) {
    var fn = mascaras[campo.getAttribute('data-mascara')];
    if (!fn) return;
    campo.addEventListener('input', function () { campo.value = fn(campo.value); });
    if (campo.value) campo.value = fn(campo.value);
  });

  /* ---------- Campos de confirmação (e-mail e senha) ---------- */
  document.querySelectorAll('input[data-confirma]').forEach(function (campo) {
    var alvo = document.querySelector(campo.getAttribute('data-confirma'));
    if (!alvo) return;
    var tipo = campo.type === 'password' ? 'As senhas não são iguais.' : 'Os e-mails não são iguais.';
    function checar() {
      var igual = campo.value.toLowerCase() === alvo.value.toLowerCase() && (campo.type !== 'password' || campo.value === alvo.value);
      campo.setCustomValidity(campo.value && !igual ? tipo : '');
    }
    campo.addEventListener('input', checar);
    alvo.addEventListener('input', checar);
  });

  /* ---------- Impede duplo clique no botão salvar ---------- */
  document.querySelectorAll('form[data-envio-unico]').forEach(function (form) {
    form.addEventListener('submit', function () {
      var b = form.querySelector('button[type="submit"]');
      if (b && form.checkValidity()) { setTimeout(function () { b.disabled = true; }, 0); }
    });
  });
})();
