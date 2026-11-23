/* =====================================================================
   FORMULÁRIO DE AGENDAMENTO
   - Dentista e procedimento se filtram: só aparecem procedimentos que o dentista realiza e
     só aparecem dentistas que realizam o procedimento escolhido.
   - GET {data-api-horarios}?dentistaId=&procedimentoId=&data=&ignorar=  -> ["09:00", "09:30", ...]
   ===================================================================== */
(function () {
  'use strict';
  var form = document.getElementById('formConsulta');
  if (!form) return;

  var api = form.getAttribute('data-api-horarios');
  var consultaId = form.getAttribute('data-consulta') || '';
  var horaInicial = form.getAttribute('data-hora') || '';
  var proc = document.getElementById('procedimentoId');
  var den = document.getElementById('dentistaId');
  var data = document.getElementById('data');
  var hora = document.getElementById('hora');
  var dica = document.getElementById('horaDica');
  var pedido = 0;

  function opcao(valor, texto, desabilitada) {
    var o = document.createElement('option');
    o.value = valor;
    o.textContent = texto;
    if (desabilitada) o.disabled = true;
    return o;
  }

  function filtrarDentistas() {
    var escolhida = proc.options[proc.selectedIndex];
    var lista = escolhida ? escolhida.getAttribute('data-dentistas') : null;
    var ids = lista ? lista.split(',') : null;
    for (var i = 0; i < den.options.length; i++) {
      var o = den.options[i];
      if (!o.value) continue;
      var permitido = !ids || ids.indexOf(o.value) !== -1;
      o.hidden = !permitido;
      o.disabled = !permitido;
    }
    var atual = den.options[den.selectedIndex];
    if (atual && atual.disabled) den.value = '';
  }

  function filtrarProcedimentos() {
    var d = den.value;
    for (var i = 0; i < proc.options.length; i++) {
      var o = proc.options[i];
      if (!o.value) continue;
      var lista = o.getAttribute('data-dentistas');
      var ids = lista ? lista.split(',') : null;
      var permitido = !d || !ids || ids.indexOf(d) !== -1;
      o.hidden = !permitido;
      o.disabled = !permitido;
    }
    var atual = proc.options[proc.selectedIndex];
    if (atual && atual.disabled) proc.value = '';
  }

  function mensagem(texto) {
    hora.replaceChildren(opcao('', texto));
    hora.value = '';
  }

  function carregarHorarios() {
    if (!proc.value || !den.value || !data.value) {
      mensagem('Selecione dentista, procedimento e data');
      dica.textContent = 'Selecione um horário livre para esta dentista.';
      return;
    }
    var meu = ++pedido;
    mensagem('Carregando...');
    var url = api + '?dentistaId=' + encodeURIComponent(den.value)
      + '&procedimentoId=' + encodeURIComponent(proc.value)
      + '&data=' + encodeURIComponent(data.value)
      + (consultaId ? '&ignorar=' + encodeURIComponent(consultaId) : '');
    fetch(url, { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
      .then(function (r) {
        if (r.status === 401) {
          location.href = (document.body.getAttribute('data-base') || '').replace(/\/$/, '') + '/login';
          return new Promise(function () {});
        }
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
      })
      .then(function (lista) {
        if (meu !== pedido) return;
        if (!lista.length) {
          mensagem('Sem horários livres neste dia');
          dica.textContent = 'O dentista não atende neste dia ou a agenda está cheia. Escolha outra data.';
          return;
        }
        var anterior = hora.dataset.escolhida || horaInicial;
        hora.replaceChildren(opcao('', 'Selecione o horário', true));
        lista.forEach(function (h) { hora.appendChild(opcao(h, h)); });
        hora.value = lista.indexOf(anterior) !== -1 ? anterior : '';
        dica.textContent = lista.length + ' horário(s) livre(s). Selecione um horário livre para esta dentista.';
      })
      .catch(function () {
        if (meu !== pedido) return;
        mensagem('Não foi possível carregar os horários');
        dica.textContent = 'Tente novamente.';
      });
  }

  hora.addEventListener('change', function () { hora.dataset.escolhida = hora.value; });
  proc.addEventListener('change', function () { filtrarDentistas(); filtrarProcedimentos(); carregarHorarios(); });
  den.addEventListener('change', function () { filtrarProcedimentos(); filtrarDentistas(); carregarHorarios(); });
  data.addEventListener('change', carregarHorarios);

  filtrarDentistas();
  filtrarProcedimentos();
  carregarHorarios();
})();
