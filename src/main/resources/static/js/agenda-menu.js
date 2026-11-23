/* =====================================================================
   AGENDA: navegação da gaveta (Dia / Semana / Mês) e da lista "Todos os dentistas".
   O dia e o dentista que estão na URL são mantidos ao trocar de visão.
   ===================================================================== */
(function () {
  'use strict';

  var base = (document.body.getAttribute('data-base') || '').replace(/\/$/, '');

  function ir(visao, dentistaId) {
    var atual = new URLSearchParams(location.search);
    var q = new URLSearchParams();
    q.set('visao', visao || atual.get('visao') || 'mes');
    if (atual.get('data')) q.set('data', atual.get('data'));
    if (dentistaId === undefined) dentistaId = atual.get('dentistaId');
    if (dentistaId) q.set('dentistaId', dentistaId);
    location.href = base + '/agenda?' + q.toString();
  }

  document.querySelectorAll('a[data-visao]').forEach(function (a) {
    a.addEventListener('click', function (e) {
      e.preventDefault();
      ir(a.getAttribute('data-visao'));
    });
  });

  var filtro = document.getElementById('filtroDentista');
  if (filtro) {
    filtro.querySelectorAll('a[data-dentista]').forEach(function (a) {
      a.addEventListener('click', function (e) {
        e.preventDefault();
        ir(null, a.getAttribute('data-dentista'));
      });
    });
    document.addEventListener('click', function (e) { if (!filtro.contains(e.target)) filtro.removeAttribute('open'); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') filtro.removeAttribute('open'); });
  }
})();
