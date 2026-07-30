'use strict';

// 后端 API 基础地址
var BASE_URL = 'http://127.0.0.1:8080';

(function () {
  // tab 名映射（展示名 → 接口名）
  var TAB_NAMES = {
    helloworld: 'helloworld',
    hash: 'hash',
    bubble: 'bubble'
  };

  var currentTab = 'helloworld';

  // DOM 引用
  var tabs = Array.prototype.slice.call(document.querySelectorAll('.demo-tab'));
  var panels = Array.prototype.slice.call(document.querySelectorAll('.demo-panel'));
  var exportBtn = document.getElementById('exportBtn');

  var helloworldResult = document.getElementById('helloworldResult');
  var hashInput = document.getElementById('hashInput');
  var hashExecBtn = document.getElementById('hashExecBtn');
  var hashResult = document.getElementById('hashResult');
  var bubbleInput = document.getElementById('bubbleInput');
  var bubbleExecBtn = document.getElementById('bubbleExecBtn');
  var bubbleResult = document.getElementById('bubbleResult');

  // ===== 工具函数 =====

  function escapeHtml(str) {
    if (str === null || str === undefined) {
      return '';
    }
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function formatTimestamp(ts) {
    var n = Number(ts);
    if (!isFinite(n)) {
      return String(ts);
    }
    var d = new Date(n);
    if (isNaN(d.getTime())) {
      return String(ts);
    }
    var pad = function (v) {
      return v < 10 ? '0' + v : '' + v;
    };
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
      ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
  }

  function fieldHtml(label, valueHtml, valueClass) {
    var cls = valueClass ? ' class="' + valueClass + '"' : '';
    return '<p class="demo-field">' +
      '<span class="demo-field-label">' + escapeHtml(label) + '</span>' +
      '<span class="demo-field-value"' + cls + '>' + valueHtml + '</span>' +
      '</p>';
  }

  function showError(container, message) {
    container.innerHTML = '<p class="demo-error">' + escapeHtml(message) + '</p>';
  }

  function showLoading(container, text) {
    container.innerHTML = '<p class="demo-loading">' + escapeHtml(text || '加载中…') + '</p>';
  }

  // ===== Tab 切换 =====

  function switchTab(tabName) {
    if (!TAB_NAMES[tabName]) {
      return;
    }
    currentTab = tabName;

    tabs.forEach(function (tab) {
      if (tab.dataset.tab === tabName) {
        tab.classList.add('active');
      } else {
        tab.classList.remove('active');
      }
    });

    panels.forEach(function (panel) {
      if (panel.id === 'panel-' + tabName) {
        panel.classList.add('active');
      } else {
        panel.classList.remove('active');
      }
    });

    // 进入 HelloWorld tab 自动调用
    if (tabName === 'helloworld') {
      loadHelloWorld();
    }
  }

  // ===== 接口1 HelloWorld =====

  function loadHelloWorld() {
    showLoading(helloworldResult, '加载中…');
    fetch(BASE_URL + '/api/helloworld')
      .then(function (res) {
        if (!res.ok) {
          return res.json().then(function (body) {
            throw new Error(body && body.error ? body.error : 'HTTP ' + res.status);
          }, function () {
            throw new Error('HTTP ' + res.status);
          });
        }
        return res.json();
      })
      .then(function (data) {
        var html = fieldHtml('结果', escapeHtml(data.result)) +
          fieldHtml('时间戳', '<span class="demo-timestamp">' + escapeHtml(formatTimestamp(data.timestamp)) + '</span>');
        helloworldResult.innerHTML = html;
        helloworldResult.classList.remove('demo-result-empty');
      })
      .catch(function (err) {
        showError(helloworldResult, '请求失败：' + (err && err.message ? err.message : err));
      });
  }

  // ===== 接口2 哈希算法 =====

  function execHash() {
    var input = hashInput.value;
    if (!input) {
      showError(hashResult, 'input is required');
      return;
    }
    showLoading(hashResult, '执行中…');
    var url = BASE_URL + '/api/hash?input=' + encodeURIComponent(input);
    fetch(url)
      .then(function (res) {
        if (!res.ok) {
          return res.json().then(function (body) {
            throw new Error(body && body.error ? body.error : 'HTTP ' + res.status);
          }, function () {
            throw new Error('HTTP ' + res.status);
          });
        }
        return res.json();
      })
      .then(function (data) {
        var html = fieldHtml('输入', escapeHtml(data.input)) +
          fieldHtml('算法', escapeHtml(data.algorithm)) +
          fieldHtml('哈希值', escapeHtml(data.hash), 'demo-hash-value') +
          fieldHtml('长度', escapeHtml(String(data.length)));
        hashResult.innerHTML = html;
        hashResult.classList.remove('demo-result-empty');
      })
      .catch(function (err) {
        showError(hashResult, '请求失败：' + (err && err.message ? err.message : err));
      });
  }

  // ===== 接口3 冒泡排序 =====

  function execBubble() {
    var data = bubbleInput.value;
    if (!data) {
      showError(bubbleResult, 'data format invalid');
      return;
    }
    showLoading(bubbleResult, '执行中…');
    var url = BASE_URL + '/api/bubble?data=' + encodeURIComponent(data);
    fetch(url)
      .then(function (res) {
        if (!res.ok) {
          return res.json().then(function (body) {
            throw new Error(body && body.error ? body.error : 'HTTP ' + res.status);
          }, function () {
            throw new Error('HTTP ' + res.status);
          });
        }
        return res.json();
      })
      .then(function (data) {
        var html = fieldHtml('原数组', escapeHtml(JSON.stringify(data.input))) +
          fieldHtml('排序后', escapeHtml(JSON.stringify(data.sorted))) +
          fieldHtml('交换次数', escapeHtml(String(data.steps))) +
          fieldHtml('排序方向', escapeHtml(data.asc ? '升序 (asc)' : '降序 (desc)'));
        bubbleResult.innerHTML = html;
        bubbleResult.classList.remove('demo-result-empty');
      })
      .catch(function (err) {
        showError(bubbleResult, '请求失败：' + (err && err.message ? err.message : err));
      });
  }

  // ===== 接口4 导出 =====

  function exportCurrent() {
    var tab = TAB_NAMES[currentTab];
    if (!tab) {
      return;
    }
    var url = BASE_URL + '/api/export?tab=' + encodeURIComponent(tab);
    // 使用隐藏 <a download> 触发浏览器下载 CSV
    var a = document.createElement('a');
    a.href = url;
    a.download = '';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  // ===== 事件绑定 =====

  function bindEvents() {
    tabs.forEach(function (tab) {
      tab.addEventListener('click', function () {
        switchTab(tab.dataset.tab);
      });
    });

    hashExecBtn.addEventListener('click', execHash);
    bubbleExecBtn.addEventListener('click', execBubble);
    exportBtn.addEventListener('click', exportCurrent);

    // 回车触发执行
    hashInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.keyCode === 13) {
        execHash();
      }
    });
    bubbleInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.keyCode === 13) {
        execBubble();
      }
    });
  }

  // ===== 初始化 =====

  function init() {
    bindEvents();
    switchTab('helloworld');
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
