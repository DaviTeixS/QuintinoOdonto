/* =====================================================================
   HORÁRIOS DE ATENDIMENTO (Figma: "horários do dentista")
   - Cada período é um botão azul "08:00 – 12:00" com um × para remover.
   - "+ Período" abre dois campos de hora para criar um novo período no dia.
   - "Salvar horários" envia todos os períodos de uma vez (campo repetido "periodo" = "MONDAY|08:00|12:00").
   - A lista de dentistas no topo troca a tela para o outro dentista.
   ===================================================================== */
(function () {
  'use strict';

  var form = document.getElementById('formHorarios');
  if (!form) return;

  var saida = document.getElementById('periodosSaida');
  var seletor = document.getElementById('dentistaSel');
  var alterado = false;

  function el(tag, classe, texto) {
    var e = document.createElement(tag);
    if (classe) e.className = classe;
    if (texto !== undefined) e.textContent = texto;
    return e;
  }

  function minutos(hhmm) { var p = hhmm.split(':'); return (+p[0]) * 60 + (+p[1]); }

  function periodosDoDia(linha) {
    return Array.prototype.map.call(linha.querySelectorAll('.periodo'), function (p) {
      var partes = p.getAttribute('data-valor').split('|');
      return { inicio: partes[1], fim: partes[2] };
    });
  }

  function atualizarVazio(linha) {
    var tem = linha.querySelectorAll('.periodo').length > 0;
    var vazio = linha.querySelector('.dia-linha__vazio');
    if (vazio) vazio.hidden = tem;
  }

  function criarChip(linha, inicio, fim) {
    var dia = linha.getAttribute('data-dia');
    var chip = el('span', 'periodo');
    chip.setAttribute('data-valor', dia + '|' + inicio + '|' + fim);
    chip.appendChild(el('span', 'periodo__texto', inicio + ' – ' + fim));
    var x = el('button', 'periodo__remover', '×');
    x.type = 'button';
    x.setAttribute('aria-label', 'Remover período ' + inicio + ' às ' + fim);
    chip.appendChild(x);
    return chip;
  }

  function ordenar(linha) {
    var chips = Array.prototype.slice.call(linha.querySelectorAll('.periodo'));
    chips.sort(function (a, b) { return a.getAttribute('data-valor').localeCompare(b.getAttribute('data-valor')); });
    var ref = linha.querySelector('.dia-linha__vazio');
    chips.forEach(function (c) { linha.querySelector('.dia-linha__periodos').insertBefore(c, ref); });
  }

  function abrirEditor(linha, botao) {
    if (linha.querySelector('.novo-periodo')) return;
    var editor = el('span', 'novo-periodo');
    var de = el('input'); de.type = 'time'; de.required = true; de.setAttribute('aria-label', 'Início do período'); de.value = '08:00';
    var ate = el('input'); ate.type = 'time'; ate.required = true; ate.setAttribute('aria-label', 'Fim do período'); ate.value = '12:00';
    var ok = el('button', 'novo-periodo__ok', 'Adicionar'); ok.type = 'button';
    var cancelar = el('button', 'periodo__remover', '×'); cancelar.type = 'button'; cancelar.setAttribute('aria-label', 'Cancelar novo período');
    var aviso = el('span', 'novo-periodo__aviso'); aviso.setAttribute('role', 'alert');
    editor.appendChild(de); editor.appendChild(el('span', null, 'às')); editor.appendChild(ate);
    editor.appendChild(ok); editor.appendChild(cancelar); editor.appendChild(aviso);
    botao.hidden = true;
    botao.parentNode.insertBefore(editor, botao);
    de.focus();

    function fechar() { editor.remove(); botao.hidden = false; botao.focus(); }

    ok.addEventListener('click', function () {
      if (!de.value || !ate.value) { aviso.textContent = 'Informe os dois horários.'; return; }
      var i = minutos(de.value), f = minutos(ate.value);
      if (f <= i) { aviso.textContent = 'O final precisa ser depois do início.'; return; }
      var conflito = periodosDoDia(linha).some(function (p) { return i < minutos(p.fim) && minutos(p.inicio) < f; });
      if (conflito) { aviso.textContent = 'Este período se sobrepõe a outro do mesmo dia.'; return; }
      linha.querySelector('.dia-linha__periodos').insertBefore(criarChip(linha, de.value, ate.value), editor);
      alterado = true;
      ordenar(linha);
      atualizarVazio(linha);
      fechar();
    });
    cancelar.addEventListener('click', fechar);
  }

  form.querySelectorAll('.dia-linha').forEach(function (linha) {
    atualizarVazio(linha);
    linha.addEventListener('click', function (e) {
      var remover = e.target.closest('.periodo__remover');
      var chip = remover && remover.closest('.periodo');
      if (chip) { chip.remove(); alterado = true; atualizarVazio(linha); return; }
      var adicionar = e.target.closest('[data-adicionar]');
      if (adicionar) abrirEditor(linha, adicionar);
    });
  });

  form.addEventListener('submit', function () {
    saida.replaceChildren();
    form.querySelectorAll('.periodo').forEach(function (p) {
      var h = document.createElement('input');
      h.type = 'hidden'; h.name = 'periodo'; h.value = p.getAttribute('data-valor');
      saida.appendChild(h);
    });
    alterado = false;
  });

  if (seletor) {
    var atual = seletor.value;
    seletor.addEventListener('change', function () {
      if (alterado && !window.confirm('Há alterações não salvas. Trocar de dentista mesmo assim?')) {
        seletor.value = atual;
        return;
      }
      location.href = seletor.getAttribute('data-base') + '/' + seletor.value + '/horarios';
    });
  }
})();
