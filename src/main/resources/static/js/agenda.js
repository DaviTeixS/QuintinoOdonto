/* =====================================================================
   AGENDA: visões DIA e SEMANA
   - Dia:    GET {data-api}?data=YYYY-MM-DD[&dentistaId=]       -> [ConsultaDTO]
   - Semana: GET {data-api}?inicio=YYYY-MM-DD[&dentistaId=]     -> [ConsultaDTO] (7 dias, de domingo a sábado)
   O dia mostrado fica na URL (?data=) para não se perder ao trocar de visão.
   ===================================================================== */
(function () {
  'use strict';

  var MESES = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];
  var SEMANA = ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

  var secaoDia = document.getElementById('visaoDia');
  var secaoSemana = document.getElementById('visaoSemana');
  var secao = secaoDia || secaoSemana;
  if (!secao) return;

  var api = secao.getAttribute('data-api');
  var consultaBase = secao.getAttribute('data-consulta-base');
  var dentistaId = secao.getAttribute('data-dentista-id') || '';
  var perfil = secao.getAttribute('data-perfil') || 'consultorio';
  var btnAgendar = document.getElementById('btnAgendar');
  var agendarBase = btnAgendar ? btnAgendar.getAttribute('href') : '';
  var pedido = 0;

  function pad(n) { return String(n).padStart(2, '0'); }
  function iso(d) { return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()); }
  function lerIso(texto) {
    var p = /^(\d{4})-(\d{2})-(\d{2})$/.exec(texto || '');
    return p ? new Date(+p[1], +p[2] - 1, +p[3]) : new Date();
  }
  function soma(d, dias) { return new Date(d.getFullYear(), d.getMonth(), d.getDate() + dias); }

  function el(tag, classe, texto) {
    var e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined && texto !== null) e.textContent = texto;
    return e;
  }

  function buscarJson(url) {
    return fetch(url, { headers: { Accept: 'application/json' }, credentials: 'same-origin' }).then(function (r) {
      if (r.status === 401) { location.href = (document.body.getAttribute('data-base') || '').replace(/\/$/, '') + '/login'; return new Promise(function () {}); }
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    });
  }

  function gravarUrl(d) {
    var q = new URLSearchParams(location.search);
    q.set('data', iso(d));
    if (window.history && history.replaceState) history.replaceState(null, '', location.pathname + '?' + q.toString());
    if (btnAgendar) btnAgendar.setAttribute('href', agendarBase + '?data=' + iso(d));
  }

  function subtitulo(c) {
    // Figma: o dentista vê o procedimento; o consultório vê o dentista (ou o procedimento, se filtrou um dentista)
    return (perfil === 'dentista' || dentistaId) ? c.procedimento : c.dentistaNome + (c.procedimento ? ' • ' + c.procedimento : '');
  }

  var ICONE_CHEVRON = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 5 7 7-7 7"/></svg>';
  var ICONE_MAIS = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>';

  /* ================== DIA ================== */
  if (secaoDia) {
    var dia = lerIso(secaoDia.getAttribute('data-data'));
    var cabDia = document.getElementById('diaCabecalho');
    var totalDia = document.getElementById('diaTotal');
    var listaDia = document.getElementById('diaConsultas');

    var cartaoDia = function (c) {
      var a = el('a', 'cartao-dia');
      a.href = consultaBase + '/' + c.id;
      a.setAttribute('aria-label', 'Abrir consulta de ' + c.pacienteNome + ' às ' + String(c.hora).slice(0, 5));
      a.appendChild(el('span', 'cartao-dia__hora', String(c.hora).slice(0, 5)));
      var textos = el('span', 'cartao-dia__textos');
      textos.appendChild(el('span', 'cartao-dia__paciente', c.pacienteNome));
      textos.appendChild(el('span', 'cartao-dia__sub', subtitulo(c)));
      a.appendChild(textos);
      var seta = el('span', 'cartao-dia__seta');
      seta.innerHTML = ICONE_CHEVRON;
      a.appendChild(seta);
      return a;
    };

    var carregarDia = function () {
      var meu = ++pedido;
      cabDia.textContent = SEMANA[dia.getDay()] + ', ' + pad(dia.getDate()) + '/' + MESES[dia.getMonth()];
      totalDia.textContent = '';
      listaDia.replaceChildren(el('p', 'vazio', 'Carregando...'));
      gravarUrl(dia);
      buscarJson(api + '?data=' + iso(dia) + (dentistaId ? '&dentistaId=' + encodeURIComponent(dentistaId) : ''))
        .then(function (lista) {
          if (meu !== pedido) return;
          totalDia.textContent = lista.length === 0 ? 'Nenhuma consulta' : (lista.length === 1 ? '1 consulta' : lista.length + ' consultas');
          if (!lista.length) { listaDia.replaceChildren(el('p', 'vazio', 'Nenhuma consulta agendada para este dia.')); return; }
          listaDia.replaceChildren.apply(listaDia, lista.map(cartaoDia));
        })
        .catch(function () {
          if (meu === pedido) listaDia.replaceChildren(el('p', 'vazio', 'Não foi possível carregar as consultas.'));
        });
    };

    document.getElementById('diaAnterior').addEventListener('click', function () { dia = soma(dia, -1); carregarDia(); });
    document.getElementById('diaProximo').addEventListener('click', function () { dia = soma(dia, 1); carregarDia(); });
    carregarDia();
  }

  /* ================== SEMANA ================== */
  if (secaoSemana) {
    var ref = lerIso(secaoSemana.getAttribute('data-data'));
    var inicioSemana = soma(ref, -ref.getDay()); // domingo
    var cabSemana = document.getElementById('semanaCabecalho');
    var listaSemana = document.getElementById('semanaConsultas');
    var busca = document.getElementById('buscaPaciente');
    var consultasSemana = [];

    var semAcento = function (t) { return String(t || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase(); };

    var cartaoSemana = function (c) {
      var card = el('article', 'consulta consulta--lista');
      card.appendChild(el('div', 'consulta__hora', String(c.hora).slice(0, 5)));
      var dados = el('div', 'consulta__dados');
      dados.appendChild(el('p', 'consulta__paciente', c.pacienteNome));
      dados.appendChild(el('p', 'consulta__sub', subtitulo(c)));
      card.appendChild(dados);
      var mais = el('a', 'consulta__mais');
      mais.href = consultaBase + '/' + c.id;
      mais.setAttribute('aria-label', 'Abrir consulta de ' + c.pacienteNome);
      mais.innerHTML = ICONE_MAIS;
      card.appendChild(mais);
      return card;
    };

    var desenharSemana = function () {
      var termo = semAcento(busca.value.trim());
      var filtradas = consultasSemana.filter(function (c) { return !termo || semAcento(c.pacienteNome).indexOf(termo) !== -1; });
      if (!filtradas.length) {
        listaSemana.replaceChildren(el('p', 'vazio', termo ? 'Nenhum paciente encontrado nesta semana.' : 'Nenhuma consulta agendada nesta semana.'));
        return;
      }
      var grupos = {};
      var ordem = [];
      filtradas.forEach(function (c) {
        if (!grupos[c.data]) { grupos[c.data] = []; ordem.push(c.data); }
        grupos[c.data].push(c);
      });
      ordem.sort();
      var blocos = ordem.map(function (dataIso) {
        var d = lerIso(dataIso);
        var bloco = el('section', 'semana-dia');
        bloco.appendChild(el('h3', 'semana-dia__titulo', SEMANA[d.getDay()] + ', ' + pad(d.getDate()) + '/' + pad(d.getMonth() + 1)));
        var grade = el('div', 'semana-dia__lista');
        grupos[dataIso].forEach(function (c) { grade.appendChild(cartaoSemana(c)); });
        bloco.appendChild(grade);
        return bloco;
      });
      listaSemana.replaceChildren.apply(listaSemana, blocos);
    };

    var carregarSemana = function () {
      var meu = ++pedido;
      var fim = soma(inicioSemana, 6);
      cabSemana.textContent = 'Semana de ' + pad(inicioSemana.getDate()) + '/' + pad(inicioSemana.getMonth() + 1)
        + ' a ' + pad(fim.getDate()) + '/' + pad(fim.getMonth() + 1);
      listaSemana.replaceChildren(el('p', 'vazio', 'Carregando...'));
      gravarUrl(ref);
      buscarJson(api + '?inicio=' + iso(inicioSemana) + (dentistaId ? '&dentistaId=' + encodeURIComponent(dentistaId) : ''))
        .then(function (lista) { if (meu === pedido) { consultasSemana = lista; desenharSemana(); } })
        .catch(function () {
          if (meu === pedido) listaSemana.replaceChildren(el('p', 'vazio', 'Não foi possível carregar as consultas.'));
        });
    };

    busca.addEventListener('input', desenharSemana);
    document.getElementById('semanaAnterior').addEventListener('click', function () { ref = soma(ref, -7); inicioSemana = soma(ref, -ref.getDay()); carregarSemana(); });
    document.getElementById('semanaProxima').addEventListener('click', function () { ref = soma(ref, 7); inicioSemana = soma(ref, -ref.getDay()); carregarSemana(); });
    carregarSemana();
  }
})();
