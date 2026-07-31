/**
 * 算法演示前端逻辑
 * 调用后端 algo-demo 服务的四个接口，原生 fetch，无框架依赖。
 */

// 后端地址常量（开发期 localhost:8080，部署时改为实际后端域名）
const API_BASE = 'http://localhost:8080';

(function () {
  'use strict';

  function ready(fn) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', fn);
    } else {
      fn();
    }
  }

  // ---- 通用渲染工具（用 DOM 操作防 XSS，不拼 innerHTML） ----

  function clearResult(el) {
    while (el.firstChild) el.removeChild(el.firstChild);
  }

  function setError(el, message) {
    clearResult(el);
    var err = document.createElement('span');
    err.className = 'err';
    err.textContent = '错误：' + message;
    el.appendChild(err);
  }

  function setLoading(el, text) {
    clearResult(el);
    var s = document.createElement('span');
    s.className = 'muted';
    s.textContent = text || '请求中…';
    el.appendChild(s);
  }

  function addField(parent, label, value, valueClass) {
    var wrap = document.createElement('div');
    wrap.className = 'field';
    var lbl = document.createElement('span');
    lbl.className = 'label';
    lbl.textContent = label + '：';
    var val = document.createElement('span');
    val.className = 'val mono' + (valueClass ? ' ' + valueClass : '');
    val.textContent = String(value);
    wrap.appendChild(lbl);
    wrap.appendChild(val);
    parent.appendChild(wrap);
  }

  // ---- 请求封装 ----

  function fetchJson(url, options) {
    return fetch(url, options).then(function (res) {
      if (!res.ok) {
        var status = res.status;
        return res.text().then(function (body) {
          throw new Error('HTTP ' + status + (body ? (' - ' + body) : ''));
        });
      }
      var ct = res.headers.get('content-type') || '';
      if (ct.indexOf('application/json') === -1) {
        throw new Error('非 JSON 响应：' + ct);
      }
      return res.json();
    });
  }

  // ---- Tab 切换 ----

  function initTabs() {
    var heads = document.querySelectorAll('.tab-head');
    var panels = document.querySelectorAll('.tab-panel');
    heads.forEach(function (head) {
      function activate() {
        heads.forEach(function (h) { h.classList.remove('active'); });
        panels.forEach(function (p) { p.classList.remove('active'); });
        head.classList.add('active');
        var target = head.getAttribute('data-tab');
        var panel = document.getElementById('panel-' + target);
        if (panel) panel.classList.add('active');
      }
      head.addEventListener('click', activate);
      head.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          activate();
        }
      });
    });
  }

  // ---- 接口1 helloworld ----

  function callHello() {
    var el = document.getElementById('result-hello');
    setLoading(el, '请求中…');
    fetchJson(API_BASE + '/api/helloworld', { method: 'GET' })
      .then(function (data) {
        clearResult(el);
        addField(el, 'result', data.result == null ? '' : data.result, 'ok');
        addField(el, 'timestamp', data.timestamp == null ? '' : data.timestamp);
      })
      .catch(function (err) {
        setError(el, err && err.message ? err.message : String(err));
      });
  }

  // ---- 接口2 哈希算法 ----

  function callHash() {
    var el = document.getElementById('result-hash');
    var input = document.getElementById('hash-input').value.trim();
    var algo = document.getElementById('hash-algo').value;

    if (!input) {
      setError(el, '请输入待哈希文本');
      return;
    }

    setLoading(el, '计算中…');
    fetchJson(API_BASE + '/api/hash', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ input: input, algorithm: algo })
    })
      .then(function (data) {
        clearResult(el);
        addField(el, 'input', data.input == null ? '' : data.input);
        addField(el, 'algorithm', data.algorithm == null ? '' : data.algorithm);
        addField(el, 'hash', data.hash == null ? '' : data.hash, 'ok');
        addField(el, 'length', data.length == null ? '' : data.length);
      })
      .catch(function (err) {
        setError(el, err && err.message ? err.message : String(err));
      });
  }

  // ---- 接口3 冒泡排序 ----

  function parseBubbleInput(raw) {
    if (!raw) throw new Error('请输入逗号分隔的整数，如 5,3,8,1,9,2');
    var parts = raw.split(/[,，\s]+/).filter(function (s) { return s.length > 0; });
    if (parts.length === 0) throw new Error('未解析到任何数字');
    var arr = [];
    for (var i = 0; i < parts.length; i++) {
      var n = Number(parts[i]);
      if (!Number.isFinite(n) || !Number.isInteger(n)) {
        throw new Error('存在非整数值：' + parts[i]);
      }
      arr.push(n);
    }
    return arr;
  }

  function callBubble() {
    var el = document.getElementById('result-bubble');
    var raw = document.getElementById('bubble-input').value.trim();
    var inputArr;
    try {
      inputArr = parseBubbleInput(raw);
    } catch (e) {
      setError(el, e.message);
      return;
    }

    setLoading(el, '排序中…');
    fetchJson(API_BASE + '/api/bubble-sort', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ input: inputArr })
    })
      .then(function (data) {
        clearResult(el);
        addField(el, 'input', JSON.stringify(data.input == null ? inputArr : data.input));
        addField(el, 'sorted', JSON.stringify(data.sorted == null ? [] : data.sorted), 'ok');
        addField(el, 'swapCount', data.swapCount == null ? '' : data.swapCount);

        var steps = data.steps;
        if (Array.isArray(steps) && steps.length > 0) {
          var title = document.createElement('div');
          title.className = 'steps-title';
          title.textContent = '排序轨迹（共 ' + steps.length + ' 轮）：';
          el.appendChild(title);

          var ol = document.createElement('ol');
          ol.className = 'steps-list';
          steps.forEach(function (step) {
            var li = document.createElement('li');
            var round = document.createElement('span');
            round.className = 'label';
            round.textContent = '第 ' + (step.round == null ? '?' : step.round) + ' 轮';
            li.appendChild(round);

            var swaps = document.createElement('span');
            swaps.className = 'val';
            swaps.textContent = '  交换 ' + (step.swaps == null ? 0 : step.swaps) + ' 次  →  ';
            li.appendChild(swaps);

            var arr = document.createElement('span');
            arr.className = 'val mono';
            arr.textContent = JSON.stringify(step.array || []);
            li.appendChild(arr);

            ol.appendChild(li);
          });
          el.appendChild(ol);
        }
      })
      .catch(function (err) {
        setError(el, err && err.message ? err.message : String(err));
      });
  }

  // ---- 接口4 导出 ----

  function exportAll() {
    fetch(API_BASE + '/api/export?tab=all', { method: 'GET' })
      .then(function (res) {
        if (!res.ok) {
          return res.text().then(function (body) {
            throw new Error('HTTP ' + res.status + (body ? (' - ' + body) : ''));
          });
        }
        var disposition = res.headers.get('content-disposition') || '';
        var fileName = 'algo-export.txt';
        var m = disposition.match(/filename="?([^"]+)"?/i);
        if (m && m[1]) fileName = decodeURIComponent(m[1]);
        return res.blob().then(function (blob) {
          triggerDownload(blob, fileName);
        });
      })
      .catch(function (err) {
        // 导出失败时，回退到当前可见面板的结果区展示错误
        var activePanel = document.querySelector('.tab-panel.active .result');
        if (activePanel) {
          setError(activePanel, '导出失败：' + (err && err.message ? err.message : String(err)));
        }
      });
  }

  function triggerDownload(blob, fileName) {
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(function () { URL.revokeObjectURL(url); }, 0);
  }

  // ---- 绑定 ----

  ready(function () {
    initTabs();
    var btnHello = document.getElementById('btn-hello');
    var btnHash = document.getElementById('btn-hash');
    var btnBubble = document.getElementById('btn-bubble');
    var btnExport = document.getElementById('btn-export');
    var btnExport2 = document.getElementById('btn-export-2');

    if (btnHello) btnHello.addEventListener('click', callHello);
    if (btnHash) btnHash.addEventListener('click', callHash);
    if (btnBubble) btnBubble.addEventListener('click', callBubble);
    if (btnExport) btnExport.addEventListener('click', exportAll);
    if (btnExport2) btnExport2.addEventListener('click', exportAll);

    // 回车提交
    var hashInput = document.getElementById('hash-input');
    var bubbleInput = document.getElementById('bubble-input');
    if (hashInput) hashInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') callHash();
    });
    if (bubbleInput) bubbleInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') callBubble();
    });
  });
})();
