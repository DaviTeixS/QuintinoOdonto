/* =====================================================================
   LISTAS COM BUSCA (pacientes, colaboradores, procedimentos)
   A lista vem do back-end (GET data-api?busca=...). O campo de busca refaz
   a consulta enquanto a pessoa digita (com pequena espera para não
   sobrecarregar o servidor).

   Uso no HTML:
     <input id="busca" ...>
     <section id="lista" data-api="/api/pacientes" data-base="/pacientes"
              data-principal="true"  (item inteiro vira link para data-base/{id})
              data-vazio="Nenhum paciente encontrado."></section>
   Cada item vem como { id, titulo, subtitulo, iniciais }.
   ===================================================================== */
(function () {
  'use strict';

  var lista = document.getElementById('lista');
  var campo = document.getElementById('busca');
  if (!lista) return;

  var api = lista.getAttribute('data-api');
  var base = lista.getAttribute('data-base');
  var principal = lista.getAttribute('data-principal') === 'true';
  var msgVazio = lista.getAttribute('data-vazio') || 'Nenhum resultado encontrado.';
  var contador = 0; // descarta respostas antigas se o usuário digitar rápido

  function el(tag, classe, texto) {
    var e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined && texto !== null) e.textContent = texto;
    return e;
  }

  function mensagem(texto) {
    lista.replaceChildren(el('p', 'vazio', texto));
  }

  function criarItem(item) {
    var art = el('article', 'item-lista');

    var conteudo = principal ? el('a', 'item-lista__principal') : el('div', 'item-lista__principal');
    if (principal) conteudo.href = base + '/' + item.id;
    conteudo.appendChild(el('span', 'avatar', item.iniciais));

    var textos = el('div', 'item-lista__textos');
    textos.appendChild(el('p', 'item-lista__titulo', item.titulo));
    if (item.subtitulo) textos.appendChild(el('p', 'item-lista__sub', item.subtitulo));
    conteudo.appendChild(textos);
    art.appendChild(conteudo);

    var editar = el('a', 'item-lista__acao', 'Editar');
    editar.href = base + '/' + item.id + '/editar';
    editar.setAttribute('aria-label', 'Editar ' + item.titulo);
    art.appendChild(editar);
    return art;
  }

  function carregar(termo) {
    var minha = ++contador;
    fetch(api + '?busca=' + encodeURIComponent(termo), { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
      .then(function (r) {
        if (r.status === 401) { location.href = (document.body.getAttribute('data-base') || '').replace(/\/$/, '') + '/login'; return null; }
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
      })
      .then(function (itens) {
        if (itens === null || minha !== contador) return;
        if (!itens.length) { mensagem(termo ? msgVazio : msgVazio); return; }
        lista.replaceChildren.apply(lista, itens.map(criarItem));
      })
      .catch(function () {
        if (minha === contador) mensagem('Não foi possível carregar a lista. Tente novamente.');
      });
  }

  var espera;
  if (campo) {
    campo.addEventListener('input', function () {
      clearTimeout(espera);
      espera = setTimeout(function () { carregar(campo.value.trim()); }, 250);
    });
  }

  mensagem('Carregando...');
  carregar(campo ? campo.value.trim() : '');
})();
