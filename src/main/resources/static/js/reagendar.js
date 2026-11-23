/* =====================================================================
   REAGENDAR CONSULTA
   Mostra os horários livres do dentista na nova data como botões.
   GET {data-api-horarios}?dentistaId=&procedimentoId=&data=&ignorar=  -> ["09:00", "09:30", ...]
   ===================================================================== */
(function () {
  'use strict';

  var form = document.getElementById('formReagendar');
  if (!form) return;

  var api = form.getAttribute('data-api-horarios');
  var dentista = form.getAttribute('data-dentista');
  var procedimento = form.getAttribute('data-procedimento');
  var consulta = form.getAttribute('data-consulta');
  var data = document.getElementById('data');
  var grade = document.getElementById('horariosGrade');
  var campoHora = document.getElementById('hora');
  var dica = document.getElementById('horariosDica');
  var confirmar = document.getElementById('btnConfirmar');
  var textoPadrao = dica.textContent;
  var pedido = 0;

  function el(tag, classe, texto) {
    var e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined) e.textContent = texto;
    return e;
  }

  function escolher(hora) {
    campoHora.value = hora;
    confirmar.disabled = !hora;
    grade.querySelectorAll('.horario').forEach(function (b) {
      var ativo = b.getAttribute('data-hora') === hora;
      b.classList.toggle('horario--ativo', ativo);
      b.setAttribute('aria-checked', String(ativo));
    });
  }

  function mensagem(texto) {
    grade.replaceChildren(el('p', 'vazio vazio--pequeno', texto));
    escolher('');
  }

  function carregar() {
    if (!data.value) { mensagem('Escolha a nova data.'); return; }
    var meu = ++pedido;
    mensagem('Carregando...');
    var url = api + '?dentistaId=' + encodeURIComponent(dentista)
      + '&procedimentoId=' + encodeURIComponent(procedimento)
      + '&data=' + encodeURIComponent(data.value)
      + '&ignorar=' + encodeURIComponent(consulta);
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
        var anterior = campoHora.getAttribute('data-escolhida') || '';
        if (!lista.length) {
          mensagem('Sem horários livres neste dia. Escolha outra data.');
          dica.textContent = textoPadrao;
          return;
        }
        grade.replaceChildren.apply(grade, lista.map(function (h) {
          var b = el('button', 'horario', h);
          b.type = 'button';
          b.setAttribute('role', 'radio');
          b.setAttribute('aria-checked', 'false');
          b.setAttribute('data-hora', h);
          b.addEventListener('click', function () { campoHora.setAttribute('data-escolhida', h); escolher(h); });
          return b;
        }));
        escolher(lista.indexOf(anterior) !== -1 ? anterior : '');
        dica.textContent = textoPadrao;
      })
      .catch(function () {
        if (meu !== pedido) return;
        mensagem('Não foi possível carregar os horários. Tente novamente.');
      });
  }

  campoHora.setAttribute('data-escolhida', campoHora.value || form.getAttribute('data-hora') || '');
  data.addEventListener('change', function () { campoHora.setAttribute('data-escolhida', ''); carregar(); });
  carregar();
})();
