(function () {
  'use strict';

  // 后端地址，可配置：window.ALGORITHM_API_BASE_URL = 'http://xxx:8080'
  var API_BASE_URL = window.ALGORITHM_API_BASE_URL || 'http://localhost:8080';

  var currentTab = 'helloworld';

  // Tab 切换
  var tabBtns = document.querySelectorAll('.tab-btn');
  var panels = document.querySelectorAll('.tab-panel');
  for (var i = 0; i < tabBtns.length; i++) {
    (function (btn) {
      btn.addEventListener('click', function () {
        switchTab(btn.getAttribute('data-tab'));
      });
    })(tabBtns[i]);
  }

  function switchTab(tabId) {
    currentTab = tabId;
    for (var i = 0; i < tabBtns.length; i++) {
      tabBtns[i].classList.toggle('active', tabBtns[i].getAttribute('data-tab') === tabId);
    }
    for (var j = 0; j < panels.length; j++) {
      panels[j].classList.toggle('active', panels[j].getAttribute('id') === 'tab-' + tabId);
    }
  }
  window.switchTab = switchTab;

  function setResult(id, html) {
    var el = document.getElementById(id);
    if (el) el.innerHTML = html;
  }

  function errorHtml(msg) {
    return '<p class="error">请求失败：' + escapeHtml(msg) + '</p>';
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }

  // ---- HelloWorld ----
  var btnHello = document.getElementById('btn-helloworld');
  if (btnHello) btnHello.addEventListener('click', fetchHelloWorld);

  function fetchHelloWorld() {
    setResult('result-helloworld', '<p class="loading">加载中...</p>');
    fetch(API_BASE_URL + '/api/helloworld')
      .then(function (r) { return r.json(); })
      .then(function (json) {
        var msg = (json.data && json.data.message) ? json.data.message : '(无数据)';
        setResult('result-helloworld', '<p><strong>message:</strong> ' + escapeHtml(msg) + '</p>');
      })
      .catch(function (e) {
        setResult('result-helloworld', errorHtml(e.message || String(e)));
      });
  }
  window.fetchHelloWorld = fetchHelloWorld;

  // ---- Hash ----
  var btnHash = document.getElementById('btn-hash');
  if (btnHash) btnHash.addEventListener('click', fetchHash);

  function fetchHash() {
    var input = document.getElementById('hash-input').value;
    var algo = document.getElementById('hash-algo').value;
    setResult('result-hash', '<p class="loading">计算中...</p>');
    var url = API_BASE_URL + '/api/hash?input=' + encodeURIComponent(input) + '&algo=' + encodeURIComponent(algo);
    fetch(url)
      .then(function (r) { return r.json(); })
      .then(function (json) {
        if (json.code !== 200) {
          setResult('result-hash', errorHtml(json.msg || '错误'));
          return;
        }
        var d = json.data || {};
        setResult('result-hash',
          '<p><strong>input:</strong> ' + escapeHtml(d.input || '') + '</p>' +
          '<p><strong>algo:</strong> ' + escapeHtml(d.algo || '') + '</p>' +
          '<p><strong>hash:</strong> <code>' + escapeHtml(d.hash || '') + '</code></p>');
      })
      .catch(function (e) {
        setResult('result-hash', errorHtml(e.message || String(e)));
      });
  }
  window.fetchHash = fetchHash;

  // ---- BubbleSort ----
  var btnBubble = document.getElementById('btn-bubble-sort');
  if (btnBubble) btnBubble.addEventListener('click', fetchBubbleSort);

  function fetchBubbleSort() {
    setResult('result-bubble-sort', '<p class="loading">生成中...</p>');
    fetch(API_BASE_URL + '/api/bubble-sort')
      .then(function (r) { return r.json(); })
      .then(function (json) {
        var d = json.data || {};
        var orig = d.original || [];
        var sorted = d.sorted || [];
        var rows = '';
        for (var i = 0; i < orig.length; i++) {
          rows += '<tr><td>' + i + '</td><td>' + orig[i] + '</td><td>'
            + (sorted[i] !== undefined ? sorted[i] : '') + '</td></tr>';
        }
        setResult('result-bubble-sort',
          '<p><strong>count:</strong> ' + escapeHtml(String(d.count || orig.length)) + '</p>' +
          '<table><thead><tr><th>下标</th><th>原始</th><th>排序后</th></tr></thead><tbody>'
          + rows + '</tbody></table>');
      })
      .catch(function (e) {
        setResult('result-bubble-sort', errorHtml(e.message || String(e)));
      });
  }
  window.fetchBubbleSort = fetchBubbleSort;

  // ---- 导出 ----
  var exportCurrentBtn = document.getElementById('export-current-btn');
  if (exportCurrentBtn) exportCurrentBtn.addEventListener('click', exportCurrent);

  function exportCurrent() {
    exportByType(currentTab);
  }
  window.exportCurrent = exportCurrent;

  function exportByType(type) {
    var url = API_BASE_URL + '/api/export?type=' + encodeURIComponent(type) + '&format=csv';
    var a = document.createElement('a');
    a.href = url;
    a.download = type + '-result.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  var exportTabBtns = document.querySelectorAll('.btn-export-tab');
  for (var k = 0; k < exportTabBtns.length; k++) {
    (function (btn) {
      btn.addEventListener('click', function () {
        exportByType(btn.getAttribute('data-type'));
      });
    })(exportTabBtns[k]);
  }

  // 默认加载第一个 tab
  fetchHelloWorld();
})();
