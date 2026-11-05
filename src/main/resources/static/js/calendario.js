/* =====================================================================
   CALENDÁRIO DA TELA INICIAL
   - GET {data-api-mes}?ano=2026&mes=11   -> [{ dia, total }]  (dias com consulta)
   - GET {data-api-dia}?data=2026-11-04   -> [ConsultaDTO]     (consultas do dia)
   ===================================================================== */
(function () {
  'use strict';

  var raiz = document.getElementById('calendario');
  if (!raiz) return;

  var apiMes = raiz.getAttribute('data-api-mes');
  var apiDia = raiz.getAttribute('data-api-dia');
  var fichaBase = raiz.getAttribute('data-ficha-base');
  var LIMITE_INICIAL = 4;

  var MESES = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
  var SEMANA = ['domingo', 'segunda-feira', 'terça-feira', 'quarta-feira', 'quinta-feira', 'sexta-feira', 'sábado'];

  var grade = document.getElementById('calGrade');
  var titulo = document.getElementById('calTitulo');
  var diaTitulo = document.getElementById('diaTitulo');
  var diaLista = document.getElementById('diaLista');
  var verTodos = document.getElementById('verTodos');

  var hoje = new Date();
  var exibido = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
  var selecionado = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
  var diasComConsulta = {};
  var consultasDia = [];
  var expandido = false;
  var pedidoMes = 0, pedidoDia = 0;

  function pad(n) { return String(n).padStart(2, '0'); }
  function iso(d) { return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()); }
  function mesmoDia(a, b) { return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate(); }

  function el(tag, classe, texto) {
    var e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined) e.textContent = texto;
    return e;
  }

  function buscarJson(url) {
    return fetch(url, { headers: { Accept: 'application/json' }, credentials: 'same-origin' }).then(function (r) {
      if (r.status === 401) { location.href = (document.body.getAttribute('data-base') || '').replace(/\/$/, '') + '/login'; return new Promise(function () {}); }
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    });
  }

  /* ---------- Grade do mês ---------- */
  function desenharGrade() {
    var ano = exibido.getFullYear(), mes = exibido.getMonth();
    titulo.textContent = MESES[mes] + ' ' + ano;

    var primeiro = new Date(ano, mes, 1);
    var inicio = new Date(ano, mes, 1 - primeiro.getDay());
    var totalDias = Math.ceil((primeiro.getDay() + new Date(ano, mes + 1, 0).getDate()) / 7) * 7;
    var frag = document.createDocumentFragment();

    for (var i = 0; i < totalDias; i++) {
      var d = new Date(inicio.getFullYear(), inicio.getMonth(), inicio.getDate() + i);
      var doMes = d.getMonth() === mes;
      var b = el('button', 'dia');
      b.type = 'button';
      if (!doMes) b.classList.add('dia--fora');
      if (mesmoDia(d, hoje)) b.classList.add('dia--hoje');
      if (mesmoDia(d, selecionado)) b.classList.add('dia--selecionado');
      b.setAttribute('data-data', iso(d));
      b.appendChild(el('span', 'dia__num', pad(d.getDate())));

      var total = doMes ? (diasComConsulta[d.getDate()] || 0) : 0;
      var marca = el('span', 'dia__marca' + (total ? '' : ' dia__marca--vazia'));
      b.appendChild(marca);
      b.setAttribute('aria-label', d.getDate() + ' de ' + MESES[d.getMonth()] + (total ? ', ' + total + ' consulta(s)' : ''));
      if (mesmoDia(d, selecionado)) b.setAttribute('aria-pressed', 'true');
      frag.appendChild(b);
    }
    grade.replaceChildren(frag);
  }

  function carregarMes() {
    var meu = ++pedidoMes;
    diasComConsulta = {};
    desenharGrade();
    buscarJson(apiMes + '?ano=' + exibido.getFullYear() + '&mes=' + (exibido.getMonth() + 1))
      .then(function (dias) {
        if (meu !== pedidoMes) return;
        dias.forEach(function (d) { diasComConsulta[d.dia] = d.total; });
        desenharGrade();
      })
      .catch(function () { /* o calendário continua utilizável sem os marcadores */ });
  }

  /* ---------- Consultas do dia ---------- */
  var ICONE_MAIS = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>';

  function tituloDoDia() {
    var d = selecionado;
    var rotulo = mesmoDia(d, hoje) ? 'Hoje, ' : '';
    diaTitulo.textContent = 'Consultas de ' + rotulo + SEMANA[d.getDay()] + ', ' + d.getDate() + ' de ' + MESES[d.getMonth()].toLowerCase();
  }

  function desenharConsultas() {
    var visiveis = expandido ? consultasDia : consultasDia.slice(0, LIMITE_INICIAL);
    if (!consultasDia.length) {
      diaLista.replaceChildren(el('p', 'vazio', 'Nenhuma consulta agendada para este dia.'));
    } else {
      diaLista.replaceChildren.apply(diaLista, visiveis.map(function (c) {
        var card = el('article', 'consulta');
        card.appendChild(el('div', 'consulta__hora', String(c.hora).slice(0, 5)));
        var dados = el('div', 'consulta__dados');
        dados.appendChild(el('p', 'consulta__paciente', c.pacienteNome));
        dados.appendChild(el('p', 'consulta__sub', c.dentistaNome + (c.procedimento ? ' • ' + c.procedimento : '')));
        card.appendChild(dados);
        var mais = el('a', 'consulta__mais');
        mais.href = fichaBase + '/' + c.pacienteId;
        mais.setAttribute('aria-label', 'Abrir ficha de ' + c.pacienteNome);
        mais.innerHTML = ICONE_MAIS;
        card.appendChild(mais);
        return card;
      }));
    }
    verTodos.hidden = consultasDia.length <= LIMITE_INICIAL;
    verTodos.textContent = expandido ? 'Mostrar menos' : 'Visualizar todos';
  }

  function carregarDia() {
    var meu = ++pedidoDia;
    expandido = false;
    tituloDoDia();
    diaLista.replaceChildren(el('p', 'vazio', 'Carregando...'));
    verTodos.hidden = true;
    buscarJson(apiDia + '?data=' + iso(selecionado))
      .then(function (consultas) { if (meu === pedidoDia) { consultasDia = consultas; desenharConsultas(); } })
      .catch(function () {
        if (meu === pedidoDia) diaLista.replaceChildren(el('p', 'vazio', 'Não foi possível carregar as consultas.'));
      });
  }

  /* ---------- Eventos ---------- */
  document.getElementById('calAnterior').addEventListener('click', function () {
    exibido = new Date(exibido.getFullYear(), exibido.getMonth() - 1, 1);
    carregarMes();
  });
  document.getElementById('calProximo').addEventListener('click', function () {
    exibido = new Date(exibido.getFullYear(), exibido.getMonth() + 1, 1);
    carregarMes();
  });

  grade.addEventListener('click', function (e) {
    var b = e.target.closest('.dia');
    if (!b) return;
    var p = b.getAttribute('data-data').split('-');
    selecionado = new Date(+p[0], +p[1] - 1, +p[2]);
    if (selecionado.getMonth() !== exibido.getMonth()) {
      exibido = new Date(selecionado.getFullYear(), selecionado.getMonth(), 1);
      carregarMes();
    } else {
      desenharGrade();
    }
    carregarDia();
  });

  verTodos.addEventListener('click', function () { expandido = !expandido; desenharConsultas(); });

  carregarMes();
  carregarDia();
})();
