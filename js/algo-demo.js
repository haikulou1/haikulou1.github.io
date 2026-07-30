/**
 * 算法演示前端逻辑
 * 三 Tab 切换 + fetch 调用后端 + 导出按钮 + 错误兜底
 */
const BASE_URL = 'http://localhost:8080/api';
let currentTab = 'hello';

/**
 * 切换 Tab
 */
function switchTab(type, el) {
    currentTab = type;
    // 更新 Tab 高亮
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    el.classList.add('active');
    // 切换面板
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
    document.getElementById('panel-' + type).classList.add('active');
}

/**
 * 通用 fetch 封装：5s 超时 + 错误兜底
 */
async function callApi(url) {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), 5000);
    try {
        const res = await fetch(url, { signal: ctrl.signal });
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return await res.json();
    } catch (e) {
        return { _error: '接口请求失败：' + e.message + '（后端可能未启动）' };
    } finally {
        clearTimeout(timer);
    }
}

/**
 * 渲染错误提示（红色），不覆盖上次成功结果
 */
function renderError(elId, msg) {
    document.getElementById(elId).innerHTML =
        '<div class="error-msg">' + msg + '</div>';
}

/**
 * HelloWorld
 */
async function callHello() {
    const data = await callApi(BASE_URL + '/hello');
    if (data._error) {
        renderError('result-hello', data._error);
        return;
    }
    document.getElementById('result-hello').innerHTML =
        '<div><span class="label">结果：</span><span class="value">' + data.result + '</span></div>';
}

/**
 * 哈希算法
 */
async function callHash() {
    const input = document.getElementById('hash-input').value;
    const data = await callApi(BASE_URL + '/hash?input=' + encodeURIComponent(input));
    if (data._error) {
        renderError('result-hash', data._error);
        return;
    }
    document.getElementById('result-hash').innerHTML =
        '<div><span class="label">输入：</span><span class="value">' + data.input + '</span></div>' +
        '<div><span class="label">算法：</span><span class="value">' + data.algorithm + '</span></div>' +
        '<div><span class="label">哈希：</span><span class="value">' + data.hash + '</span></div>';
}

/**
 * 冒泡排序
 */
async function callBubble() {
    const nums = document.getElementById('bubble-input').value;
    const data = await callApi(BASE_URL + '/bubble?nums=' + encodeURIComponent(nums));
    if (data._error) {
        renderError('result-bubble', data._error);
        return;
    }
    let html = '';
    if (data.warning) {
        html += '<div class="warning">' + data.warning + '</div>';
    }
    html += '<div><span class="label">输入：</span><span class="value">[' + data.input.join(', ') + ']</span></div>';
    html += '<div><span class="label">排序后：</span><span class="value">[' + data.sorted.join(', ') + ']</span></div>';
    document.getElementById('result-bubble').innerHTML = html;
}

/**
 * 导出当前 Tab 结果
 */
function exportResult() {
    const url = BASE_URL + '/export?type=' + currentTab;
    // 用隐藏 iframe 触发下载，避免页面跳转
    const iframe = document.createElement('iframe');
    iframe.style.display = 'none';
    iframe.src = url;
    document.body.appendChild(iframe);
    setTimeout(() => {
        document.body.removeChild(iframe);
    }, 10000);
    // 兜底：iframe 加载失败提示
    iframe.onerror = function () {
        alert('导出失败，请检查后端服务');
    };
}

// 页面加载时默认调用 HelloWorld
callHello();
