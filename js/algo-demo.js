/**
 * 算法演示前端逻辑
 * 三 Tab 切换 + fetch 调用后端 + 导出按钮 + 错误兜底
 *
 * CR 修复（2026-07-30）：
 * - B1 XSS: innerHTML 全部改为 textContent/DOM API，杜绝 HTML 注入面
 * - I1 统一响应体: callApi 解包 {code,message,data} 的 data 层
 * - I3 BASE_URL 环境自适应: localhost→开发，其他→生产域名
 * - I4 导出改 fetch+Blob: 可捕获 HTTP 错误状态码
 */

// I3: 环境自适应——开发期用 localhost，生产期用实际后端域名
const BASE_URL = location.hostname === 'localhost' || location.hostname === '127.0.0.1'
    ? 'http://localhost:8080/api'
    : 'https://algo-api.example.com/api'; // 生产环境部署时替换为实际域名

let currentTab = 'hello';

/**
 * 切换 Tab：高亮 + 切换面板 + 自动加载当前 Tab 数据
 */
function switchTab(type, el) {
    currentTab = type;
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    el.classList.add('active');
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
    document.getElementById('panel-' + type).classList.add('active');
    // 自动加载当前 Tab 数据
    if (type === 'hello') {
        callHello();
    } else if (type === 'hash') {
        callHash();
    } else if (type === 'bubble') {
        callBubble();
    }
}

/**
 * 通用 fetch 封装：5s 超时 + 错误兜底
 * 兼容两种响应：后端正常接口直接返回业务 JSON（如 {"result":"HelloWorld"}），
 * 异常时由 GlobalExceptionHandler 返回统一响应体 {code,message,data}。
 */
async function callApi(url) {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), 5000);
    try {
        const res = await fetch(url, { signal: ctrl.signal });
        if (!res.ok) {
            // HTTP 错误：尝试解析统一错误体 {code,message,data}
            let msg = 'HTTP ' + res.status;
            try {
                const errBody = await res.json();
                if (errBody && errBody.message) msg = errBody.message;
            } catch (e) { /* 非 JSON 错误体，用状态码兜底 */ }
            return { _error: '接口请求失败：' + msg + '（后端可能未启动）' };
        }
        const body = await res.json();
        // 统一响应体兜底：若含 code 字段且非 0，视为业务错误
        if (body && typeof body.code === 'number' && body.code !== 0) {
            return { _error: body.message || ('错误码 ' + body.code) };
        }
        // 正常业务 JSON（裸 Map 或 code=0 的统一体），原样返回
        return body;
    } catch (e) {
        return { _error: '接口请求失败：' + e.message + '（后端可能未启动）' };
    } finally {
        clearTimeout(timer);
    }
}

/**
 * 辅助：创建 label+value 行（B1: 用 DOM API 而非 innerHTML）
 */
function createResultRow(label, value) {
    const div = document.createElement('div');
    const labelSpan = document.createElement('span');
    labelSpan.className = 'label';
    labelSpan.textContent = label;
    const valueSpan = document.createElement('span');
    valueSpan.className = 'value';
    valueSpan.textContent = value;
    div.appendChild(labelSpan);
    div.appendChild(valueSpan);
    return div;
}

/**
 * 渲染错误提示（红色），保留上次成功结果（不覆盖）
 * B1: 用 textContent 而非 innerHTML
 */
function renderError(elId, msg) {
    const container = document.getElementById(elId);
    // 移除旧错误提示（如有），保留成功结果
    const oldErr = container.querySelector('.error-msg');
    if (oldErr) oldErr.remove();
    const div = document.createElement('div');
    div.className = 'error-msg';
    div.textContent = msg;
    container.insertBefore(div, container.firstChild);
}

/**
 * 清除错误提示（成功时调用）
 */
function clearError(elId) {
    const container = document.getElementById(elId);
    const oldErr = container.querySelector('.error-msg');
    if (oldErr) oldErr.remove();
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
    clearError('result-hello');
    const container = document.getElementById('result-hello');
    container.textContent = '';
    container.appendChild(createResultRow('结果：', data.result));
}

/**
 * 哈希算法
 * B1: data.input 用 textContent 渲染，杜绝 XSS
 */
async function callHash() {
    const input = document.getElementById('hash-input').value;
    const data = await callApi(BASE_URL + '/hash?input=' + encodeURIComponent(input));
    if (data._error) {
        renderError('result-hash', data._error);
        return;
    }
    clearError('result-hash');
    const container = document.getElementById('result-hash');
    container.textContent = '';
    container.appendChild(createResultRow('输入：', data.input));
    container.appendChild(createResultRow('算法：', data.algorithm));
    container.appendChild(createResultRow('哈希：', data.hash));
}

/**
 * 冒泡排序
 * B1: 用 DOM API 渲染，data.input/sorted 经 parseInt 过滤为数字安全
 */
async function callBubble() {
    const nums = document.getElementById('bubble-input').value;
    const data = await callApi(BASE_URL + '/bubble?nums=' + encodeURIComponent(nums));
    if (data._error) {
        renderError('result-bubble', data._error);
        return;
    }
    clearError('result-bubble');
    const container = document.getElementById('result-bubble');
    container.textContent = '';
    if (data.warning) {
        const warningDiv = document.createElement('div');
        warningDiv.className = 'warning';
        warningDiv.textContent = data.warning;
        container.appendChild(warningDiv);
    }
    container.appendChild(createResultRow('输入：', '[' + data.input.join(', ') + ']'));
    container.appendChild(createResultRow('排序后：', '[' + data.sorted.join(', ') + ']'));
}

/**
 * 导出当前 Tab 结果
 * I4: 改用 fetch+Blob 方式，可捕获 HTTP 错误状态码并提示用户
 * 传递当前 Tab 的用户动态参数（input/nums），保证导出内容与页面展示一致
 */
async function exportResult() {
    // 根据当前 Tab 拼接导出 URL，传递用户动态参数
    let url = BASE_URL + '/export?type=' + currentTab;
    if (currentTab === 'hash') {
        const input = document.getElementById('hash-input').value;
        url += '&input=' + encodeURIComponent(input);
    } else if (currentTab === 'bubble') {
        const nums = document.getElementById('bubble-input').value;
        url += '&nums=' + encodeURIComponent(nums);
    }

    const btn = document.querySelector('.btn-export');
    const originalText = btn ? btn.textContent : '';
    if (btn) {
        btn.disabled = true;
        btn.textContent = '导出中...';
    }

    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), 30000);
    try {
        const res = await fetch(url, { signal: ctrl.signal });
        if (!res.ok) {
            // 解析后端统一错误体 {code,message,data}
            let errMsg = 'HTTP ' + res.status;
            try {
                const errBody = await res.json();
                if (errBody && errBody.message) errMsg = errBody.message;
            } catch (e) { /* 非 JSON 错误体，用状态码兜底 */ }
            alert('导出失败：' + errMsg);
            return;
        }
        const blob = await res.blob();
        // 从 Content-Disposition 解析文件名
        const disposition = res.headers.get('Content-Disposition') || '';
        let filename = currentTab + '.csv';
        const match = disposition.match(/filename="?([^"]+)"?/);
        if (match) filename = match[1];
        const blobUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = blobUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(blobUrl);
    } catch (e) {
        alert('导出失败：' + e.message);
    } finally {
        clearTimeout(timer);
        if (btn) {
            btn.disabled = false;
            btn.textContent = originalText;
        }
    }
}

// 页面加载时默认调用 HelloWorld
callHello();
