/* =====================================================================
   LISTA SUSPENSA "Dentistas responsáveis" (seleção múltipla)
   As opções vêm do back-end: GET /api/dentistas -> [{ id, titulo, subtitulo }]
   Cada dentista marcada vira um <input type="hidden" name="dentistaIds">.
   ===================================================================== */
(function () {
  'use strict';

  var raiz = document.getElementById('dentistas');
  if (!raiz) return;

  var botao = raiz.querySelector('.multi__botao');
  var texto = raiz.querySelector('.multi__texto');
  var painel = raiz.querySelector('.multi__painel');
  var saida = raiz.querySelector('.multi__saida');
  var api = raiz.getAttribute('data-api');
  var placeholder = raiz.getAttribute('data-placeholder');
  var marcados = (raiz.getAttribute('data-selecionados') || '').split(',').filter(Boolean);
  var dentistas = [];

  function el(tag, classe, texto) {
    var e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined) e.textContent = texto;
    return e;
  }

  function atualizar() {
    var ids = Array.prototype.map.call(painel.querySelectorAll('input:checked'), function (i) { return i.value; });
    var nomes = dentistas.filter(function (d) { return ids.indexOf(String(d.id)) !== -1; }).map(function (d) { return d.titulo; });
    texto.textContent = nomes.length ? nomes.join(', ') : placeholder;
    botao.setAttribute('data-vazio', String(!nomes.length));
    saida.replaceChildren.apply(saida, ids.map(function (id) {
      var h = document.createElement('input');
      h.type = 'hidden'; h.name = 'dentistaIds'; h.value = id;
      return h;
    }));
  }

  function alternar(abrir) {
    raiz.classList.toggle('aberto', abrir);
    botao.setAttribute('aria-expanded', String(abrir));
  }

  botao.addEventListener('click', function () { alternar(!raiz.classList.contains('aberto')); });
  document.addEventListener('click', function (e) { if (!raiz.contains(e.target)) alternar(false); });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') alternar(false); });

  fetch(api, { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
    .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
    .then(function (lista) {
      dentistas = lista;
      if (!lista.length) { painel.replaceChildren(el('p', 'vazio', 'Nenhum dentista cadastrado.')); return; }
      painel.replaceChildren.apply(painel, lista.map(function (d) {
        var rotulo = el('label', 'multi__opcao');
        var cx = document.createElement('input');
        cx.type = 'checkbox'; cx.value = d.id;
        cx.checked = marcados.indexOf(String(d.id)) !== -1;
        cx.addEventListener('change', atualizar);
        rotulo.appendChild(cx);
        rotulo.appendChild(el('span', null, d.titulo));
        if (d.subtitulo) rotulo.appendChild(el('small', null, d.subtitulo));
        return rotulo;
      }));
      atualizar();
    })
    .catch(function () { painel.replaceChildren(el('p', 'vazio', 'Não foi possível carregar os dentistas.')); });
})();
