/* =====================================================================
   DETALHE DA CONSULTA: janela "Cancelar consulta?" (Figma: Confirmar cancelamento)
   ===================================================================== */
(function () {
  'use strict';

  var modal = document.getElementById('modalCancelar');
  var abrir = document.getElementById('btnCancelar');
  if (!modal || !abrir) return;

  function fechar() {
    if (typeof modal.close === 'function') modal.close(); else modal.removeAttribute('open');
    abrir.focus();
  }

  abrir.addEventListener('click', function () {
    if (typeof modal.showModal === 'function') modal.showModal(); else modal.setAttribute('open', '');
  });
  modal.querySelectorAll('[data-fechar]').forEach(function (b) { b.addEventListener('click', fechar); });
  // clicar fora da caixa também fecha
  modal.addEventListener('click', function (e) { if (e.target === modal) fechar(); });
})();
