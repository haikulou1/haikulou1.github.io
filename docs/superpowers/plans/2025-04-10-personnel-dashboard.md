# 人员看板 (Personnel Dashboard) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个人员看板系统，支持员工基本信息管理（增删改查）、批量导入、成本预算记录、白名单管理。

**Architecture:** 纯前端单页应用（SPA），基于原生 HTML/CSS/JavaScript 实现，数据存储使用 localStorage。整个应用放置在 `personnel-dashboard/` 目录下，与现有 Hexo 博客共存。

**Tech Stack:** HTML5, CSS3, Vanilla JavaScript (ES6+), localStorage

---

## Global Constraints

- 所有页面须为中文界面
- 数据持久化使用 localStorage，无需后端
- 兼容 Chrome/Firefox/Edge 最新版本
- 文件必须放在 `personnel-dashboard/` 目录下
- 禁止修改现有 Hexo 博客的任何文件

---

## File Structure

```
personnel-dashboard/
├── index.html              # 主入口 - 人员看板首页
├── css/
│   └── style.css           # 全局样式
├── js/
│   ├── app.js              # 应用主模块 - 路由/初始化
│   ├── employee.js         # 员工 CRUD 模块
│   ├── import.js           # 批量导入模块
│   ├── budget.js           # 成本预算模块
│   ├── whitelist.js        # 白名单管理模块
│   └── storage.js          # localStorage 封装层
├── pages/
│   ├── employee-list.html  # 员工列表页（模板片段）
│   ├── employee-form.html  # 员工表单页（新增/编辑）
│   ├── import.html         # 批量导入页
│   ├── budget.html         # 成本预算页
│   └── whitelist.html      # 白名单管理页
└── README.md               # 使用说明
```

---

## Task 1: 项目骨架与存储层

**Files:**
- Create: `personnel-dashboard/index.html`
- Create: `personnel-dashboard/css/style.css`
- Create: `personnel-dashboard/js/storage.js`
- Create: `personnel-dashboard/js/app.js`

**Interfaces:**
- Consumes: 无
- Produces: `Storage` 对象 (getItem/setItem/removeItem/getAllKeys), `App` 对象 (init/navigate/render)

- [ ] **Step 1: 创建 index.html 入口**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>人员看板</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>
  <nav class="sidebar">
    <div class="sidebar-header">
      <h2>人员看板</h2>
    </div>
    <ul class="nav-list">
      <li><a href="#employees" data-nav="employees">👥 员工管理</a></li>
      <li><a href="#import" data-nav="import">📥 批量导入</a></li>
      <li><a href="#budget" data-nav="budget">💰 成本预算</a></li>
      <li><a href="#whitelist" data-nav="whitelist">🛡️ 白名单管理</a></li>
    </ul>
  </nav>
  <main id="main-content">
    <div class="page-header">
      <h1>欢迎使用人员看板系统</h1>
      <p>请从左侧菜单选择功能模块</p>
    </div>
  </main>
  <script src="js/storage.js"></script>
  <script src="js/app.js"></script>
</body>
</html>
```

- [ ] **Step 2: 创建 style.css 全局样式**

```css
/* 全局重置 */
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; display: flex; min-height: 100vh; background: #f5f7fa; color: #333; }

/* 侧边栏 */
.sidebar { width: 240px; background: #1a1a2e; color: #fff; display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: 20px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.sidebar-header h2 { font-size: 20px; font-weight: 600; }
.nav-list { list-style: none; padding: 10px 0; }
.nav-list li { padding: 0; }
.nav-list a { display: flex; align-items: center; gap: 10px; padding: 12px 20px; color: rgba(255,255,255,0.8); text-decoration: none; transition: all 0.2s; font-size: 14px; }
.nav-list a:hover, .nav-list a.active { background: rgba(255,255,255,0.1); color: #fff; }

/* 主内容区 */
#main-content { flex: 1; padding: 24px; overflow-y: auto; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 24px; color: #1a1a2e; }
.page-header p { font-size: 14px; color: #666; margin-top: 4px; }

/* 卡片 */
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 20px; margin-bottom: 16px; }

/* 表格 */
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
th { background: #f8f9fa; font-weight: 600; color: #555; }
tr:hover { background: #f8f9fa; }

/* 表单 */
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 500; margin-bottom: 4px; color: #333; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; outline: none; transition: border 0.2s; }
.form-group input:focus, .form-group select:focus { border-color: #4a6cf7; }

/* 按钮 */
.btn { display: inline-flex; align-items: center; gap: 4px; padding: 8px 16px; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; transition: all 0.2s; text-decoration: none; }
.btn-primary { background: #4a6cf7; color: #fff; }
.btn-primary:hover { background: #3b5de7; }
.btn-danger { background: #e74c3c; color: #fff; }
.btn-danger:hover { background: #c0392b; }
.btn-success { background: #27ae60; color: #fff; }
.btn-success:hover { background: #219a52; }
.btn-warning { background: #f39c12; color: #fff; }
.btn-warning:hover { background: #e67e22; }
.btn-sm { padding: 4px 10px; font-size: 12px; }
.btn + .btn { margin-left: 8px; }

/* 工具栏 */
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; gap: 12px; flex-wrap: wrap; }
.search-box { display: flex; gap: 8px; align-items: center; }
.search-box input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; width: 240px; }

/* 弹窗 */
.modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center; }
.modal.show { display: flex; }
.modal-content { background: #fff; border-radius: 8px; padding: 24px; min-width: 400px; max-width: 600px; max-height: 80vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.modal-header h3 { font-size: 18px; }
.modal-close { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }

/* 消息提示 */
.toast { position: fixed; top: 20px; right: 20px; padding: 12px 20px; border-radius: 4px; color: #fff; font-size: 14px; z-index: 2000; opacity: 0; transform: translateY(-10px); transition: all 0.3s; }
.toast.show { opacity: 1; transform: translateY(0); }
.toast-success { background: #27ae60; }
.toast-error { background: #e74c3c; }
.toast-info { background: #4a6cf7; }

/* 统计卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); text-align: center; }
.stat-card .stat-value { font-size: 28px; font-weight: 700; color: #4a6cf7; }
.stat-card .stat-label { font-size: 13px; color: #888; margin-top: 4px; }

/* 文件上传 */
.file-upload { border: 2px dashed #ddd; border-radius: 8px; padding: 40px; text-align: center; cursor: pointer; transition: all 0.2s; }
.file-upload:hover { border-color: #4a6cf7; background: #f8f9ff; }
.file-upload input[type="file"] { display: none; }
.file-upload .upload-icon { font-size: 48px; color: #ccc; }
.file-upload p { margin-top: 8px; color: #888; }

/* 分页 */
.pagination { display: flex; justify-content: center; gap: 4px; margin-top: 16px; }
.pagination button { padding: 6px 12px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; font-size: 13px; }
.pagination button.active { background: #4a6cf7; color: #fff; border-color: #4a6cf7; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 20px; color: #999; }
.empty-state .empty-icon { font-size: 48px; margin-bottom: 12px; }

/* 响应式 */
@media (max-width: 768px) {
  .sidebar { width: 60px; }
  .sidebar-header h2, .nav-list a span { display: none; }
  .nav-list a { justify-content: center; padding: 12px; }
  .modal-content { min-width: auto; width: 90%; }
}
```

- [ ] **Step 3: 创建 storage.js (localStorage 封装层)**

```javascript
// Storage 模块 - localStorage 封装
const Storage = {
  _prefix: 'pd_',

  _key(key) {
    return this._prefix + key;
  },

  // 获取所有数据
  get(key) {
    try {
      const data = localStorage.getItem(this._key(key));
      return data ? JSON.parse(data) : null;
    } catch (e) {
      console.error('Storage.get error:', e);
      return null;
    }
  },

  // 保存数据
  set(key, value) {
    try {
      localStorage.setItem(this._key(key), JSON.stringify(value));
      return true;
    } catch (e) {
      console.error('Storage.set error:', e);
      return false;
    }
  },

  // 删除数据
  remove(key) {
    try {
      localStorage.removeItem(this._key(key));
      return true;
    } catch (e) {
      console.error('Storage.remove error:', e);
      return false;
    }
  },

  // 获取所有键名
  getAllKeys() {
    const keys = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith(this._prefix)) {
        keys.push(key.slice(this._prefix.length));
      }
    }
    return keys;
  },

  // 获取集合列表
  getList(listKey) {
    return this.get(listKey) || [];
  },

  // 保存集合列表
  setList(listKey, list) {
    return this.set(listKey, list);
  },

  // 生成唯一 ID
  generateId() {
    return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }
};
```

- [ ] **Step 4: 创建 app.js (应用主模块)**

```javascript
// App 主模块
const App = {
  currentPage: 'employees',
  pageModules: {},

  init() {
    this.bindNavEvents();
    this.loadDefaultPage();
  },

  bindNavEvents() {
    document.querySelectorAll('[data-nav]').forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const page = link.getAttribute('data-nav');
        this.navigate(page);
      });
    });
  },

  navigate(page) {
    this.currentPage = page;
    // 更新导航高亮
    document.querySelectorAll('[data-nav]').forEach(link => {
      link.classList.toggle('active', link.getAttribute('data-nav') === page);
    });
    // 渲染页面
    this.renderPage(page);
  },

  renderPage(page) {
    const container = document.getElementById('main-content');
    if (this.pageModules[page] && this.pageModules[page].render) {
      this.pageModules[page].render(container);
    } else {
      container.innerHTML = `<div class="page-header"><h1>页面加载中...</h1></div>`;
    }
  },

  registerPage(name, module) {
    this.pageModules[name] = module;
  },

  loadDefaultPage() {
    this.navigate('employees');
  },

  // 显示消息提示
  showToast(message, type = 'success') {
    const existing = document.querySelector('.toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    requestAnimationFrame(() => toast.classList.add('show'));
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => toast.remove(), 300);
    }, 2500);
  }
};

// 初始化
document.addEventListener('DOMContentLoaded', () => {
  App.init();
});
```

---

## Task 2: 员工管理模块 (CRUD)

**Files:**
- Create: `personnel-dashboard/js/employee.js`

**Interfaces:**
- Consumes: `Storage.getList('employees')`, `Storage.setList('employees', list)`, `Storage.generateId()`
- Produces: `EmployeeModule` 对象 (render, getList, add, update, remove, search)

- [ ] **Step 1: 创建 employee.js**

```javascript
// 员工管理模块
const EmployeeModule = {
  _pageSize: 10,
  _currentPage: 1,
  _searchKeyword: '',
  _sortField: 'name',
  _sortOrder: 'asc',

  // 获取员工列表
  getList() {
    return Storage.getList('employees');
  },

  // 保存员工列表
  _saveList(list) {
    Storage.setList('employees', list);
  },

  // 添加员工
  add(employee) {
    const list = this.getList();
    employee.id = Storage.generateId();
    employee.createdAt = new Date().toISOString();
    employee.updatedAt = employee.createdAt;
    // 默认成本预算
    if (employee.budget === undefined || employee.budget === null) {
      employee.budget = 0;
    }
    list.push(employee);
    this._saveList(list);
    return employee;
  },

  // 更新员工
  update(id, data) {
    const list = this.getList();
    const index = list.findIndex(e => e.id === id);
    if (index === -1) return null;
    list[index] = { ...list[index], ...data, updatedAt: new Date().toISOString() };
    this._saveList(list);
    return list[index];
  },

  // 删除员工
  remove(id) {
    let list = this.getList();
    list = list.filter(e => e.id !== id);
    this._saveList(list);
  },

  // 根据ID获取员工
  getById(id) {
    const list = this.getList();
    return list.find(e => e.id === id) || null;
  },

  // 搜索员工
  search(keyword) {
    if (!keyword) return this.getList();
    const kw = keyword.toLowerCase();
    return this.getList().filter(e =>
      e.name.toLowerCase().includes(kw) ||
      e.department.toLowerCase().includes(kw) ||
      e.position.toLowerCase().includes(kw) ||
      e.employeeId.toLowerCase().includes(kw) ||
      e.phone.includes(kw)
    );
  },

  // 分页查询
  getPagedData(page, keyword, sortField, sortOrder) {
    let list = this.search(keyword || this._searchKeyword);
    const field = sortField || this._sortField;
    const order = sortOrder || this._sortOrder;

    // 排序
    list.sort((a, b) => {
      let va = a[field] || '', vb = b[field] || '';
      if (typeof va === 'string') va = va.toLowerCase();
      if (typeof vb === 'string') vb = vb.toLowerCase();
      if (va < vb) return order === 'asc' ? -1 : 1;
      if (va > vb) return order === 'asc' ? 1 : -1;
      return 0;
    });

    const total = list.length;
    const totalPages = Math.ceil(total / this._pageSize) || 1;
    const start = (page - 1) * this._pageSize;
    const pagedList = list.slice(start, start + this._pageSize);

    return { list: pagedList, total, totalPages, page, pageSize: this._pageSize };
  },

  // 渲染主页面
  render(container) {
    container.innerHTML = `
      <div class="page-header">
        <h1>👥 员工管理</h1>
        <p>管理所有员工基本信息</p>
      </div>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value" id="emp-total-count">0</div>
          <div class="stat-label">员工总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" id="emp-dept-count">0</div>
          <div class="stat-label">部门数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" id="emp-budget-total">¥0</div>
          <div class="stat-label">预算总额</div>
        </div>
      </div>
      <div class="card">
        <div class="toolbar">
          <div class="search-box">
            <input type="text" id="emp-search-input" placeholder="搜索姓名/部门/职位/工号/电话...">
            <button class="btn btn-primary btn-sm" id="emp-search-btn">搜索</button>
          </div>
          <div>
            <button class="btn btn-success" id="emp-add-btn">➕ 新增员工</button>
          </div>
        </div>
        <div id="emp-table-container">
          <table>
            <thead>
              <tr>
                <th data-sort="employeeId" class="sortable">工号</th>
                <th data-sort="name" class="sortable">姓名</th>
                <th data-sort="department" class="sortable">部门</th>
                <th data-sort="position" class="sortable">职位</th>
                <th>手机</th>
                <th>邮箱</th>
                <th data-sort="budget" class="sortable">预算(¥)</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody id="emp-table-body"></tbody>
          </table>
          <div id="emp-empty-state" class="empty-state" style="display:none;">
            <div class="empty-icon">📋</div>
            <p>暂无员工数据，点击"新增员工"添加</p>
          </div>
        </div>
        <div id="emp-pagination" class="pagination"></div>
      </div>
    `;

    this._bindEvents(container);
    this._refresh();
  },

  _bindEvents(container) {
    container.querySelector('#emp-add-btn').addEventListener('click', () => this._showForm());
    container.querySelector('#emp-search-btn').addEventListener('click', () => {
      this._searchKeyword = container.querySelector('#emp-search-input').value;
      this._currentPage = 1;
      this._refresh();
    });
    container.querySelector('#emp-search-input').addEventListener('keyup', (e) => {
      if (e.key === 'Enter') {
        this._searchKeyword = e.target.value;
        this._currentPage = 1;
        this._refresh();
      }
    });
    // 排序
    container.querySelectorAll('[data-sort]').forEach(th => {
      th.addEventListener('click', () => {
        const field = th.getAttribute('data-sort');
        if (this._sortField === field) {
          this._sortOrder = this._sortOrder === 'asc' ? 'desc' : 'asc';
        } else {
          this._sortField = field;
          this._sortOrder = 'asc';
        }
        this._refresh();
      });
    });
  },

  _refresh() {
    const result = this.getPagedData(this._currentPage, this._searchKeyword, this._sortField, this._sortOrder);
    this._renderTable(result);
    this._renderPagination(result);
    this._updateStats();
  },

  _renderTable(result) {
    const tbody = document.getElementById('emp-table-body');
    const empty = document.getElementById('emp-empty-state');
    if (!tbody) return;

    if (result.list.length === 0) {
      tbody.innerHTML = '';
      empty.style.display = 'block';
      return;
    }
    empty.style.display = 'none';

    tbody.innerHTML = result.list.map(emp => `
      <tr>
        <td>${this._escapeHtml(emp.employeeId || '-')}</td>
        <td>${this._escapeHtml(emp.name)}</td>
        <td>${this._escapeHtml(emp.department || '-')}</td>
        <td>${this._escapeHtml(emp.position || '-')}</td>
        <td>${this._escapeHtml(emp.phone || '-')}</td>
        <td>${this._escapeHtml(emp.email || '-')}</td>
        <td>¥${emp.budget !== undefined && emp.budget !== null ? Number(emp.budget).toLocaleString() : '0'}</td>
        <td>
          <button class="btn btn-primary btn-sm" data-emp-view="${emp.id}">查看</button>
          <button class="btn btn-warning btn-sm" data-emp-edit="${emp.id}">编辑</button>
          <button class="btn btn-danger btn-sm" data-emp-delete="${emp.id}">删除</button>
        </td>
      </tr>
    `).join('');

    // 绑定行内按钮事件
    tbody.querySelectorAll('[data-emp-view]').forEach(btn => {
      btn.addEventListener('click', () => this._showDetail(btn.getAttribute('data-emp-view')));
    });
    tbody.querySelectorAll('[data-emp-edit]').forEach(btn => {
      btn.addEventListener('click', () => this._showForm(btn.getAttribute('data-emp-view')));
    });
    tbody.querySelectorAll('[data-emp-delete]').forEach(btn => {
      btn.addEventListener('click', () => this._confirmDelete(btn.getAttribute('data-emp-view')));
    });
  },

  _renderPagination(result) {
    const pagination = document.getElementById('emp-pagination');
    if (!pagination) return;
    if (result.totalPages <= 1) {
      pagination.innerHTML = '';
      return;
    }
    let html = `<button ${result.page <= 1 ? 'disabled' : ''} data-page="${result.page - 1}">上一页</button>`;
    for (let i = 1; i <= result.totalPages; i++) {
      html += `<button class="${i === result.page ? 'active' : ''}" data-page="${i}">${i}</button>`;
    }
    html += `<button ${result.page >= result.totalPages ? 'disabled' : ''} data-page="${result.page + 1}">下一页</button>`;
    pagination.innerHTML = html;

    pagination.querySelectorAll('[data-page]').forEach(btn => {
      btn.addEventListener('click', () => {
        if (!btn.disabled) {
          this._currentPage = parseInt(btn.getAttribute('data-page'));
          this._refresh();
        }
      });
    });
  },

  _updateStats() {
    const list = this.getList();
    const totalEl = document.getElementById('emp-total-count');
    const deptEl = document.getElementById('emp-dept-count');
    const budgetEl = document.getElementById('emp-budget-total');
    if (totalEl) totalEl.textContent = list.length;
    if (deptEl) {
      const depts = new Set(list.map(e => e.department).filter(Boolean));
      deptEl.textContent = depts.size;
    }
    if (budgetEl) {
      const total = list.reduce((sum, e) => sum + (Number(e.budget) || 0), 0);
      budgetEl.textContent = '¥' + total.toLocaleString();
    }
  },

  _showForm(id) {
    const employee = id ? this.getById(id) : null;
    const isEdit = !!employee;

    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
      <div class="modal-content">
        <div class="modal-header">
          <h3>${isEdit ? '编辑员工' : '新增员工'}</h3>
          <button class="modal-close" id="modal-close-btn">&times;</button>
        </div>
        <form id="emp-form">
          <div class="form-group">
            <label>工号 *</label>
            <input type="text" name="employeeId" value="${isEdit ? this._escapeHtml(employee.employeeId || '') : ''}" required ${isEdit ? 'readonly' : ''}>
          </div>
          <div class="form-group">
            <label>姓名 *</label>
            <input type="text" name="name" value="${isEdit ? this._escapeHtml(employee.name) : ''}" required>
          </div>
          <div class="form-group">
            <label>部门</label>
            <input type="text" name="department" value="${isEdit ? this._escapeHtml(employee.department || '') : ''}">
          </div>
          <div class="form-group">
            <label>职位</label>
            <input type="text" name="position" value="${isEdit ? this._escapeHtml(employee.position || '') : ''}">
          </div>
          <div class="form-group">
            <label>手机</label>
            <input type="text" name="phone" value="${isEdit ? this._escapeHtml(employee.phone || '') : ''}">
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="email" name="email" value="${isEdit ? this._escapeHtml(employee.email || '') : ''}">
          </div>
          <div class="form-group">
            <label>成本预算 (¥)</label>
            <input type="number" name="budget" step="0.01" min="0" value="${isEdit ? (employee.budget || 0) : 0}">
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea name="notes" rows="3">${isEdit ? this._escapeHtml(employee.notes || '') : ''}</textarea>
          </div>
          <div style="text-align:right;margin-top:16px;">
            <button type="button" class="btn" id="form-cancel-btn">取消</button>
            <button type="submit" class="btn btn-primary">${isEdit ? '保存修改' : '添加员工'}</button>
          </div>
        </form>
      </div>
    `;

    document.body.appendChild(modal);

    modal.querySelector('#modal-close-btn').addEventListener('click', () => modal.remove());
    modal.querySelector('#form-cancel-btn').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });

    modal.querySelector('#emp-form').addEventListener('submit', (e) => {
      e.preventDefault();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());
      data.budget = parseFloat(data.budget) || 0;

      if (isEdit) {
        this.update(employee.id, data);
        App.showToast('员工信息已更新');
      } else {
        this.add(data);
        App.showToast('员工添加成功');
      }
      modal.remove();
      this._refresh();
    });
  },

  _showDetail(id) {
    const emp = this.getById(id);
    if (!emp) return;

    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
      <div class="modal-content">
        <div class="modal-header">
          <h3>👤 员工详情 - ${this._escapeHtml(emp.name)}</h3>
          <button class="modal-close" id="modal-close-btn">&times;</button>
        </div>
        <div style="line-height:2;">
          <p><strong>工号：</strong>${this._escapeHtml(emp.employeeId || '-')}</p>
          <p><strong>姓名：</strong>${this._escapeHtml(emp.name)}</p>
          <p><strong>部门：</strong>${this._escapeHtml(emp.department || '-')}</p>
          <p><strong>职位：</strong>${this._escapeHtml(emp.position || '-')}</p>
          <p><strong>手机：</strong>${this._escapeHtml(emp.phone || '-')}</p>
          <p><strong>邮箱：</strong>${this._escapeHtml(emp.email || '-')}</p>
          <p><strong>成本预算：</strong>¥${emp.budget !== undefined ? Number(emp.budget).toLocaleString() : '0'}</p>
          <p><strong>备注：</strong>${this._escapeHtml(emp.notes || '-')}</p>
          <p><strong>创建时间：</strong>${emp.createdAt ? new Date(emp.createdAt).toLocaleString() : '-'}</p>
          <p><strong>更新时间：</strong>${emp.updatedAt ? new Date(emp.updatedAt).toLocaleString() : '-'}</p>
        </div>
        <div style="text-align:right;margin-top:16px;">
          <button class="btn" id="detail-close-btn">关闭</button>
        </div>
      </div>
    `;

    document.body.appendChild(modal);
    modal.querySelector('#modal-close-btn').addEventListener('click', () => modal.remove());
    modal.querySelector('#detail-close-btn').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });
  },

  _confirmDelete(id) {
    const emp = this.getById(id);
    if (!emp) return;

    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
      <div class="modal-content" style="min-width:320px;">
        <div class="modal-header">
          <h3>⚠️ 确认删除</h3>
          <button class="modal-close" id="modal-close-btn">&times;</button>
        </div>
        <p>确定要删除员工 <strong>${this._escapeHtml(emp.name)}</strong>（工号：${this._escapeHtml(emp.employeeId || '-')}）吗？</p>
        <p style="color:#999;font-size:13px;margin-top:8px;">此操作不可撤销</p>
        <div style="text-align:right;margin-top:16px;">
          <button class="btn" id="delete-cancel-btn">取消</button>
          <button class="btn btn-danger" id="delete-confirm-btn">确认删除</button>
        </div>
      </div>
    `;

    document.body.appendChild(modal);
    modal.querySelector('#modal-close-btn').addEventListener('click', () => modal.remove());
    modal.querySelector('#delete-cancel-btn').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });
    modal.querySelector('#delete-confirm-btn').addEventListener('click', () => {
      this.remove(id);
      App.showToast('员工已删除');
      modal.remove();
      this._refresh();
    });
  },

  _escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

// 注册到App
App.registerPage('employees', EmployeeModule);
```

---

## Task 3: 批量导入模块

**Files:**
- Create: `personnel-dashboard/js/import.js`

**Interfaces:**
- Consumes: `EmployeeModule.add()`, `EmployeeModule.getList()`
- Produces: `ImportModule` 对象 (render, parseCSV, importData)

- [ ] **Step 1: 创建 import.js**

```javascript
// 批量导入模块
const ImportModule = {
  _importedData: [],

  render(container) {
    container.innerHTML = `
      <div class="page-header">
        <h1>📥 批量导入</h1>
        <p>通过 CSV 或 Excel 文件批量导入员工信息</p>
      </div>
      <div class="card">
        <h3 style="margin-bottom:16px;">上传文件</h3>
        <div class="file-upload" id="file-upload-area">
          <div class="upload-icon">📂</div>
          <p>点击或拖拽文件到此处上传</p>
          <p style="font-size:12px;color:#aaa;margin-top:4px;">支持 CSV 格式 (UTF-8)</p>
          <input type="file" id="file-input" accept=".csv,.txt">
        </div>
      </div>
      <div class="card" id="import-preview-section" style="display:none;">
        <div class="toolbar">
          <h3>导入预览</h3>
          <div>
            <span id="import-count" style="font-size:14px;color:#666;"></span>
            <button class="btn btn-success" id="import-confirm-btn">✅ 确认导入</button>
          </div>
        </div>
        <div style="margin-bottom:12px;">
          <p style="font-size:13px;color:#888;">期望的列顺序：姓名,工号,部门,职位,手机,邮箱,预算,备注</p>
        </div>
        <div id="import-table-container"></div>
      </div>
      <div class="card" id="import-template-section">
        <h3 style="margin-bottom:8px;">📄 下载模板</h3>
        <p style="font-size:13px;color:#888;margin-bottom:12px;">下载 CSV 模板文件，按格式填写后上传</p>
        <button class="btn btn-primary" id="download-template-btn">下载模板</button>
      </div>
    `;

    this._bindEvents(container);
  },

  _bindEvents(container) {
    const uploadArea = container.querySelector('#file-upload-area');
    const fileInput = container.querySelector('#file-input');

    uploadArea.addEventListener('click', () => fileInput.click());
    uploadArea.addEventListener('dragover', (e) => {
      e.preventDefault();
      uploadArea.style.borderColor = '#4a6cf7';
      uploadArea.style.background = '#f8f9ff';
    });
    uploadArea.addEventListener('dragleave', () => {
      uploadArea.style.borderColor = '#ddd';
      uploadArea.style.background = '';
    });
    uploadArea.addEventListener('drop', (e) => {
      e.preventDefault();
      uploadArea.style.borderColor = '#ddd';
      uploadArea.style.background = '';
      if (e.dataTransfer.files.length) {
        this._handleFile(e.dataTransfer.files[0]);
      }
    });
    fileInput.addEventListener('change', (e) => {
      if (e.target.files.length) {
        this._handleFile(e.target.files[0]);
      }
    });

    container.querySelector('#download-template-btn').addEventListener('click', () => this._downloadTemplate());
    container.querySelector('#import-confirm-btn').addEventListener('click', () => this._confirmImport());
  },

  _handleFile(file) {
    if (!file.name.match(/\.(csv|txt)$/i)) {
      App.showToast('请上传 CSV 格式文件', 'error');
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target.result;
      const result = this._parseCSV(text);
      if (result.errors.length > 0) {
        App.showToast(`解析完成，但有 ${result.errors.length} 条错误`, 'info');
      }
      this._importedData = result.data;
      this._showPreview(result);
    };
    reader.onerror = () => {
      App.showToast('文件读取失败', 'error');
    };
    reader.readAsText(file, 'UTF-8');
  },

  _parseCSV(text) {
    const lines = text.split(/\r?\n/).filter(line => line.trim());
    const data = [];
    const errors = [];
    // 字段映射: 姓名,工号,部门,职位,手机,邮箱,预算,备注
    for (let i = 0; i < lines.length; i++) {
      const fields = this._parseCSVLine(lines[i]);
      if (fields.length < 1) continue;
      const name = fields[0].trim();
      if (!name) {
        errors.push({ line: i + 1, message: '姓名为空' });
        continue;
      }
      data.push({
        name: name,
        employeeId: fields[1] ? fields[1].trim() : '',
        department: fields[2] ? fields[2].trim() : '',
        position: fields[3] ? fields[3].trim() : '',
        phone: fields[4] ? fields[4].trim() : '',
        email: fields[5] ? fields[5].trim() : '',
        budget: fields[6] ? parseFloat(fields[6].trim()) || 0 : 0,
        notes: fields[7] ? fields[7].trim() : ''
      });
    }
    return { data, errors };
  },

  _parseCSVLine(line) {
    const result = [];
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (inQuotes) {
        if (ch === '"') {
          if (i + 1 < line.length && line[i + 1] === '"') {
            current += '"';
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          current += ch;
        }
      } else {
        if (ch === '"') {
          inQuotes = true;
        } else if (ch === ',') {
          result.push(current);
          current = '';
        } else {
          current += ch;
        }
      }
    }
    result.push(current);
    return result;
  },

  _showPreview(result) {
    const section = document.getElementById('import-preview-section');
    const container = document.getElementById('import-table-container');
    const count = document.getElementById('import-count');
    if (!section || !container) return;

    section.style.display = 'block';
    count.textContent = `共 ${result.data.length} 条记录，${result.errors.length} 条错误`;

    if (result.data.length === 0) {
      container.innerHTML = '<p style="color:#999;">无有效数据可导入</p>';
      return;
    }

    let html = `<table>
      <thead><tr>
        <th>姓名</th><th>工号</th><th>部门</th><th>职位</th><th>手机</th><th>邮箱</th><th>预算</th>
      </tr></thead><tbody>`;
    result.data.forEach((emp, idx) => {
      html += `<tr>
        <td>${this._escapeHtml(emp.name)}</td>
        <td>${this._escapeHtml(emp.employeeId)}</td>
        <td>${this._escapeHtml(emp.department)}</td>
        <td>${this._escapeHtml(emp.position)}</td>
        <td>${this._escapeHtml(emp.phone)}</td>
        <td>${this._escapeHtml(emp.email)}</td>
        <td>¥${Number(emp.budget).toLocaleString()}</td>
      </tr>`;
    });
    html += '</tbody></table>';
    container.innerHTML = html;

    // 显示错误
    if (result.errors.length > 0) {
      let errHtml = '<div style="margin-top:12px;padding:12px;background:#fff3f3;border-radius:4px;"><h4 style="color:#e74c3c;">错误信息</h4><ul>';
      result.errors.forEach(e => {
        errHtml += `<li style="font-size:13px;">第 ${e.line} 行：${e.message}</li>`;
      });
      errHtml += '</ul></div>';
      container.innerHTML += errHtml;
    }
  },

  _confirmImport() {
    if (this._importedData.length === 0) {
      App.showToast('没有可导入的数据', 'error');
      return;
    }

    let imported = 0;
    let skipped = 0;
    const existing = EmployeeModule.getList();

    this._importedData.forEach(data => {
      // 检查是否已存在相同工号
      if (data.employeeId && existing.some(e => e.employeeId === data.employeeId)) {
        skipped++;
        return;
      }
      EmployeeModule.add(data);
      imported++;
    });

    App.showToast(`导入完成：成功 ${imported} 条，跳过 ${skipped} 条（已存在）`);
    this._importedData = [];
    document.getElementById('import-preview-section').style.display = 'none';
    document.getElementById('file-input').value = '';
  },

  _downloadTemplate() {
    const header = '姓名,工号,部门,职位,手机,邮箱,预算,备注';
    const sample = '张三,EMP001,技术部,高级工程师,13800138000,zhangsan@example.com,50000,\n李四,EMP002,产品部,产品经理,13900139000,lisi@example.com,40000,试用期';
    const bom = '\uFEFF';
    const blob = new Blob([bom + header + '\n' + sample], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '员工导入模板.csv';
    a.click();
    URL.revokeObjectURL(url);
    App.showToast('模板已下载');
  },

  _escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

// 注册到App
App.registerPage('import', ImportModule);
```

---

## Task 4: 成本预算模块

**Files:**
- Create: `personnel-dashboard/js/budget.js`

**Interfaces:**
- Consumes: `EmployeeModule.getList()`, `Storage.getList('budgetRecords')`, `Storage.setList('budgetRecords', list)`
- Produces: `BudgetModule` 对象 (render, addRecord, getRecords, getSummary)

- [ ] **Step 1: 创建 budget.js**

```javascript
// 成本预算模块
const BudgetModule = {
  _currentPage: 1,
  _pageSize: 15,

  getRecords() {
    return Storage.getList('budgetRecords') || [];
  },

  _saveRecords(list) {
    Storage.setList('budgetRecords', list);
  },

  addRecord(record) {
    const list = this.getRecords();
    record.id = Storage.generateId();
    record.createdAt = new Date().toISOString();
    list.push(record);
    this._saveRecords(list);
  },

  removeRecord(id) {
    let list = this.getRecords();
    list = list.filter(r => r.id !== id);
    this._saveRecords(list);
  },

  getSummary() {
    const employees = EmployeeModule.getList();
    const records = this.getRecords();
    const totalBudget = employees.reduce((sum, e) => sum + (Number(e.budget) || 0), 0);
    const totalSpent = records.reduce((sum, r) => sum + (Number(r.amount) || 0), 0);
    const deptBudget = {};
    const deptSpent = {};

    employees.forEach(e => {
      const dept = e.department || '未分配';
      deptBudget[dept] = (deptBudget[dept] || 0) + (Number(e.budget) || 0);
    });
    records.forEach(r => {
      const dept = r.department || '未分配';
      deptSpent[dept] = (deptSpent[dept] || 0) + (Number(r.amount) || 0);
    });

    return { totalBudget, totalSpent, remaining: totalBudget - totalSpent, deptBudget, deptSpent };
  },

  render(container) {
    container.innerHTML = `
      <div class="page-header">
        <h1>💰 成本预算</h1>
        <p>查看和管理各部门成本预算及支出记录</p>
      </div>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value" id="budget-total">¥0</div>
          <div class="stat-label">预算总额</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" id="budget-spent" style="color:#e74c3c;">¥0</div>
          <div class="stat-label">已支出</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" id="budget-remaining">¥0</div>
          <div class="stat-label">剩余预算</div>
        </div>
      </div>
      <div class="card">
        <h3 style="margin-bottom:12px;">部门预算概览</h3>
        <div id="budget-dept-table"></div>
      </div>
      <div class="card">
        <div class="toolbar">
          <h3>支出记录</h3>
          <button class="btn btn-success" id="budget-add-btn">➕ 新增支出</button>
        </div>
        <div id="budget-table-container"></div>
        <div id="budget-pagination" class="pagination"></div>
      </div>
    `;

    this._bindEvents(container);
    this._refresh();
  },

  _bindEvents(container) {
    container.querySelector('#budget-add-btn').addEventListener('click', () => this._showAddForm());
  },

  _refresh() {
    this._updateStats();
    this._renderDeptTable();
    this._renderRecords();
  },

  _updateStats() {
    const summary = this.getSummary();
    const totalEl = document.getElementById('budget-total');
    const spentEl = document.getElementById('budget-spent');
    const remainEl = document.getElementById('budget-remaining');
    if (totalEl) totalEl.textContent = '¥' + summary.totalBudget.toLocaleString();
    if (spentEl) spentEl.textContent = '¥' + summary.totalSpent.toLocaleString();
    if (remainEl) {
      remainEl.textContent = '¥' + summary.remaining.toLocaleString();
      remainEl.style.color = summary.remaining < 0 ? '#e74c3c' : '#27ae60';
    }
  },

  _renderDeptTable() {
    const container = document.getElementById('budget-dept-table');
    if (!container) return;
    const summary = this.getSummary();
    const depts = Object.keys(summary.deptBudget);
    if (depts.length === 0) {
      container.innerHTML = '<p style="color:#999;padding:12px;">暂无部门预算数据</p>';
      return;
    }
    let html = `<table>
      <thead><tr><th>部门</th><th>预算金额</th><th>已支出</th><th>剩余</th><th>使用率</th></tr></thead><tbody>`;
    depts.forEach(dept => {
      const budget = summary.deptBudget[dept] || 0;
      const spent = summary.deptSpent[dept] || 0;
      const remaining = budget - spent;
      const rate = budget > 0 ? ((spent / budget) * 100).toFixed(1) : '0.0';
      html += `<tr>
        <td>${this._escapeHtml(dept)}</td>
        <td>¥${budget.toLocaleString()}</td>
        <td>¥${spent.toLocaleString()}</td>
        <td style="color:${remaining < 0 ? '#e74c3c' : '#27ae60'}">¥${remaining.toLocaleString()}</td>
        <td>${rate}%</td>
      </tr>`;
    });
    html += '</tbody></table>';
    container.innerHTML = html;
  },

  _renderRecords() {
    const container = document.getElementById('budget-table-container');
    const pagination = document.getElementById('budget-pagination');
    if (!container) return;

    let records = this.getRecords();
    records.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const total = records.length;
    const totalPages = Math.ceil(total / this._pageSize) || 1;
    if (this._currentPage > totalPages) this._currentPage = totalPages;
    const start = (this._currentPage - 1) * this._pageSize;
    const pageRecords = records.slice(start, start + this._pageSize);

    if (pageRecords.length === 0) {
      container.innerHTML = '<div class="empty-state"><div class="empty-icon">💳</div><p>暂无支出记录</p></div>';
      pagination.innerHTML = '';
      return;
    }

    let html = `<table>
      <thead><tr><th>日期</th><th>员工</th><th>部门</th><th>项目/用途</th><th>金额(¥)</th><th>备注</th><th>操作</th></tr></thead><tbody>`;
    pageRecords.forEach(r => {
      html += `<tr>
        <td>${new Date(r.createdAt).toLocaleDateString()}</td>
        <td>${this._escapeHtml(r.employeeName || '-')}</td>
        <td>${this._escapeHtml(r.department || '-')}</td>
        <td>${this._escapeHtml(r.project || '-')}</td>
        <td style="color:#e74c3c;">¥${Number(r.amount).toLocaleString()}</td>
        <td>${this._escapeHtml(r.notes || '-')}</td>
        <td><button class="btn btn-danger btn-sm" data-record-delete="${r.id}">删除</button></td>
      </tr>`;
    });
    html += '</tbody></table>';
    container.innerHTML = html;

    container.querySelectorAll('[data-record-delete]').forEach(btn => {
      btn.addEventListener('click', () => {
        if (confirm('确定删除此支出记录？')) {
          this.removeRecord(btn.getAttribute('data-record-delete'));
          App.showToast('记录已删除');
          this._refresh();
        }
      });
    });

    // 分页
    if (totalPages <= 1) {
      pagination.innerHTML = '';
    } else {
      let ph = `<button ${this._currentPage <= 1 ? 'disabled' : ''}" data-page="${this._currentPage - 1}">上一页</button>`;
      for (let i = 1; i <= totalPages; i++) {
        ph += `<button class="${i === this._currentPage ? 'active' : ''}" data-page="${i}">${i}</button>`;
      }
      ph += `<button ${this._currentPage >= totalPages ? 'disabled' : ''}" data-page="${this._currentPage + 1}">下一页</button>`;
      pagination.innerHTML = ph;
      pagination.querySelectorAll('[data-page]').forEach(btn => {
        btn.addEventListener('click', () => {
          if (!btn.disabled) {
            this._currentPage = parseInt(btn.getAttribute('data-page'));
            this._refresh();
          }
        });
      });
    }
  },

  _showAddForm() {
    const employees = EmployeeModule.getList();
    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
      <div class="modal-content">
        <div class="modal-header">
          <h3>新增支出记录</h3>
          <button class="modal-close" id="modal-close-btn">&times;</button>
        </div>
        <form id="budget-form">
          <div class="form-group">
            <label>员工</label>
            <select name="employeeId" id="budget-employee-select">
              <option value="">-- 选择员工 --</option>
              ${employees.map(e => `<option value="${e.id}">${this._escapeHtml(e.name)} (${this._escapeHtml(e.employeeId || '')})</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label>金额 (¥) *</label>
            <input type="number" name="amount" step="0.01" min="0.01" required>
          </div>
          <div class="form-group">
            <label>项目/用途</label>
            <input type="text" name="project">
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea name="notes" rows="2"></textarea>
          </div>
          <div style="text-align:right;margin-top:16px;">
            <button type="button" class="btn" id="form-cancel-btn">取消</button>
            <button type="submit" class="btn btn-primary">添加记录</button>
          </div>
        </form>
      </div>
    `;

    document.body.appendChild(modal);

    // 选择员工后自动填充部门和姓名
    const select = modal.querySelector('#budget-employee-select');
    select.addEventListener('change', () => {
      const emp = employees.find(e => e.id === select.value);
      // 存储到表单的自定义属性中
      if (emp) {
        select.setAttribute('data-emp-name', emp.name);
        select.setAttribute('data-emp-dept', emp.department || '');
      }
    });

    modal.querySelector('#modal-close-btn').addEventListener('click', () => modal.remove());
    modal.querySelector('#form-cancel-btn').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });

    modal.querySelector('#budget-form').addEventListener('submit', (e) => {
      e.preventDefault();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());
      const emp = employees.find(e => e.id === data.employeeId);
      data.amount = parseFloat(data.amount) || 0;
      if (data.amount <= 0) {
        App.showToast('请输入有效金额', 'error');
        return;
      }
      data.employeeName = emp ? emp.name : '';
      data.department = emp ? emp.department : '';
      this.addRecord(data);
      App.showToast('支出记录已添加');
      modal.remove();
      this._refresh();
    });
  },

  _escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

// 注册到App
App.registerPage('budget', BudgetModule);
```

---

## Task 5: 白名单管理模块

**Files:**
- Create: `personnel-dashboard/js/whitelist.js`

**Interfaces:**
- Consumes: `Storage.getList('whitelist')`, `Storage.setList('whitelist', list)`, `EmployeeModule.getList()`
- Produces: `WhitelistModule` 对象 (render, add, remove, isWhitelisted, batchImport)

- [ ] **Step 1: 创建 whitelist.js**

```javascript
// 白名单管理模块
const WhitelistModule = {
  _currentPage: 1,
  _pageSize: 15,

  getList() {
    return Storage.getList('whitelist') || [];
  },

  _saveList(list) {
    Storage.setList('whitelist', list);
  },

  add(entry) {
    const list = this.getList();
    if (list.some(e => e.employeeId === entry.employeeId)) {
      return false; // 已存在
    }
    entry.id = Storage.generateId();
    entry.createdAt = new Date().toISOString();
    list.push(entry);
    this._saveList(list);
    return true;
  },

  remove(id) {
    let list = this.getList();
    list = list.filter(e => e.id !== id);
    this._saveList(list);
  },

  isWhitelisted(employeeId) {
    return this.getList().some(e => e.employeeId === employeeId);
  },

  batchImportFromEmployees(employeeIds) {
    const employees = EmployeeModule.getList();
    let added = 0;
    let skipped = 0;
    employeeIds.forEach(id => {
      const emp = employees.find(e => e.id === id);
      if (emp && this.add({ employeeId: emp.employeeId, name: emp.name, department: emp.department })) {
        added++;
      } else {
        skipped++;
      }
    });
    return { added, skipped };
  },

  render(container) {
    container.innerHTML = `
      <div class="page-header">
        <h1>🛡️ 白名单管理</h1>
        <p>管理允许访问系统的员工白名单</p>
      </div>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value" id="wl-total-count">0</div>
          <div class="stat-label">白名单人数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" id="wl-emp-ratio">0%</div>
          <div class="stat-label">覆盖率</div>
        </div>
      </div>
      <div class="card">
        <div class="toolbar">
          <div class="search-box">
            <input type="text" id="wl-search-input" placeholder="搜索姓名/工号/部门...">
            <button class="btn btn-primary btn-sm" id="wl-search-btn">搜索</button>
          </div>
          <div>
            <button class="btn btn-success" id="wl-add-btn">➕ 手动添加</button>
            <button class="btn btn-primary" id="wl-batch-btn">📋 从员工导入</button>
          </div>
        </div>
        <div id="wl-table-container"></div>
        <div id="wl-pagination" class="pagination"></div>
      </div>
    `;

    this._bindEvents(container);
    this._refresh();
  },

  _bindEvents(container) {
    container.querySelector('#wl-add-btn').addEventListener('click', () => this._showAddForm());
    container.querySelector('#wl-batch-btn').addEventListener('click', () => this._showBatchImport());
    container.querySelector('#wl-search-btn').addEventListener('click', () => {
      this._searchKeyword = container.querySelector('#wl-search-input').value;
      this._currentPage = 1;
      this._refresh();
    });
    container.querySelector('#wl-search-input').addEventListener('keyup', (e) => {
      if (e.key === 'Enter') {
        this._searchKeyword = e.target.value;
        this._currentPage = 1;
        this._refresh();
      }
    });
  },

  _refresh() {
    this._updateStats();
    this._renderTable();
  },

  _updateStats() {
    const list = this.getList();
    const totalEl = document.getElementById('wl-total-count');
    const ratioEl = document.getElementById('wl-emp-ratio');
    if (totalEl) totalEl.textContent = list.length;
    if (ratioEl) {
      const empCount = EmployeeModule.getList().length;
      const ratio = empCount > 0 ? ((list.length / empCount) * 100).toFixed(1) : '0.0';
      ratioEl.textContent = ratio + '%';
    }
  },

  _renderTable() {
    const container = document.getElementById('wl-table-container');
    const pagination = document.getElementById('wl-pagination');
    if (!container) return;

    let list = this.getList();
    const keyword = this._searchKeyword || '';
    if (keyword) {
      const kw = keyword.toLowerCase();
      list = list.filter(e =>
        e.name.toLowerCase().includes(kw) ||
        e.employeeId.toLowerCase().includes(kw) ||
        (e.department || '').toLowerCase().includes(kw)
      );
    }

    list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const total = list.length;
    const totalPages = Math.ceil(total / this._pageSize) || 1;
    if (this._currentPage > totalPages) this._currentPage = totalPages;
    const start = (this._currentPage - 1) * this._pageSize;
    const pageList = list.slice(start, start + this._pageSize);

    if (pageList.length === 0) {
      container.innerHTML = '<div class="empty-state"><div class="empty-icon">🛡️</div><p>暂无白名单记录</p></div>';
      pagination.innerHTML = '';
      return;
    }

    let html = `<table>
      <thead><tr><th>工号</th><th>姓名</th><th>部门</th><th>添加时间</th><th>操作</th></tr></thead><tbody>`;
    pageList.forEach(item => {
      html += `<tr>
        <td>${this._escapeHtml(item.employeeId)}</td>
        <td>${this._escapeHtml(item.name)}</td>
        <td>${this._escapeHtml(item.department || '-')}</td>
        <td>${new Date(item.createdAt).toLocaleDateString()}</td>
        <td><button class="btn btn-danger btn-sm" data-wl-delete="${item.id}">移除</button></td>
      </tr>`;
    });
    html += '</tbody></table>';
    container.innerHTML = html;

    container.querySelectorAll('[data-wl-delete]').forEach(btn => {
      btn.addEventListener('click', () => {
        if (confirm('确定从白名单中移除？')) {
          this.remove(btn.getAttribute('data-wl-delete'));
          App.showToast('已从白名单移除');
          this._refresh();
        }
      });
    });

    if (totalPages <= 1) {
      pagination.innerHTML = '';
    } else {
      let ph = `<button ${this._currentPage <= 1 ? 'disabled' : ''} data-page="${this._currentPage - 1}">上一页</button>`;
      for (let i = 1; i <= totalPages; i++) {
        ph += `<button class="${i === this._currentPage ? 'active' : ''}" data-page="${i}">${i}</button>`;
      }
      ph += `<button ${this._currentPage >= totalPages ? 'disabled' : ''} data-page="${this._currentPage + 1}">下一页</button>`;
      pagination.innerHTML = ph;
      pagination.querySelectorAll('[data-page]').forEach(btn => {
        btn.addEventListener('click', () => {
          if (!btn.disabled) {
            this._currentPage = parseInt(btn.getAttribute('data-page'));
            this._refresh();
          }
        });
      });
    }
  },

  _showAddForm() {
    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
      <div class="modal-content">
        <div class="modal-header">
          <h3>手动添加白名单</h3>
          <button class="modal-close" id="modal-close-btn">&times;</button>
        </div>
        <form id="wl-add-form">
          <div class="form-group">
            <label>工号 *</label>
            <input type="text" name="employeeId" required>
          </div>
          <div class="form-group">
            <label>姓名 *</label>
            <input type="text" name="name" required>
          </div>
          <div class="form-group">
            <label>部门</label>
            <input type="text" name="department">
          </div>
          <div style="text-align:right;margin-top:16px;">
            <button type="button" class="btn" id="form-cancel-btn">取消</button>
            <button type="submit" class="btn btn-primary">添加</button>
          </div>
        </form>
      </div>
    `;

    document.body.appendChild(modal);
    modal.querySelector('#modal-close-btn').addEventListener('click', () => modal.remove());
    modal.querySelector('#form-cancel-btn').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });

    modal.querySelector('#wl-add-form').addEventListener('submit', (e) => {
      e.preventDefault();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());
      if (this.add(data)) {
        App.showToast('已添加到白名单');
        modal.remove();
        this._refresh();
      } else {
        App.showToast('该工号已在白名单中', 'error');
      }
    });
  },

  _showBatchImport() {
    const employees = EmployeeModule.getList();
    if (employees.length === 0) {
      App.showToast('暂无员工可导入', 'error');
      return;
    }

    const whitelistIds = new Set(this.getList().map(w => w.employeeId));
    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
      <div class="modal-content" style="min-width:500px;">
        <div class="modal-header">
          <h3>从员工列表批量导入白名单</h3>
          <button class="modal-close" id="modal-close-btn">&times;</button>
        </div>
        <p style="font-size:13px;color:#888;margin-bottom:12px;">选择要加入白名单的员工（已勾选的已在白名单中）</p>
        <div style="max-height:400px;overflow-y:auto;">
          <table>
            <thead><tr><th><input type="checkbox" id="wl-select-all"></th><th>工号</th><th>姓名</th><th>部门</th><th>状态</th></tr></thead>
            <tbody>
              ${employees.map(e => {
                const inWhiteList = whitelistIds.has(e.employeeId);
                return `<tr>
                  <td><input type="checkbox" class="wl-select-item" value="${e.id}" ${inWhiteList ? 'checked disabled' : ''}></td>
                  <td>${this._escapeHtml(e.employeeId || '')}</td>
                  <td>${this._escapeHtml(e.name)}</td>
                  <td>${this._escapeHtml(e.department || '-')}</td>
                  <td>${inWhiteList ? '<span style="color:#27ae60;">已添加</span>' : '<span style="color:#999;">待添加</span>'}</td>
                </tr>`;
              }).join('')}
            </tbody>
          </table>
        </div>
        <div style="text-align:right;margin-top:16px;">
          <button class="btn" id="form-cancel-btn">取消</button>
          <button class="btn btn-primary" id="wl-batch-confirm-btn">批量添加</button>
        </div>
      </div>
    `;

    document.body.appendChild(modal);
    modal.querySelector('#modal-close-btn').addEventListener('click', () => modal.remove());
    modal.querySelector('#form-cancel-btn').addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => { if (e.target === modal) modal.remove(); });

    // 全选/取消
    const selectAll = modal.querySelector('#wl-select-all');
    selectAll.addEventListener('change', () => {
      modal.querySelectorAll('.wl-select-item:not(:disabled)').forEach(cb => {
        cb.checked = selectAll.checked;
      });
    });

    modal.querySelector('#wl-batch-confirm-btn').addEventListener('click', () => {
      const selected = [];
      modal.querySelectorAll('.wl-select-item:checked:not(:disabled)').forEach(cb => {
        selected.push(cb.value);
      });
      if (selected.length === 0) {
        App.showToast('请选择要添加的员工', 'error');
        return;
      }
      const result = this.batchImportFromEmployees(selected);
      App.showToast(`批量导入完成：添加 ${result.added} 人，跳过 ${result.skipped} 人`);
      modal.remove();
      this._refresh();
    });
  },

  _escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

// 注册到App
App.registerPage('whitelist', WhitelistModule);
```

---

## Task 6: 创建 README 使用说明

**Files:**
- Create: `personnel-dashboard/README.md`

- [ ] **Step 1: 创建 README.md**

```markdown
# 人员看板系统 (Personnel Dashboard)

一个基于浏览器本地存储的纯前端人员管理看板系统，支持员工信息管理、批量导入、成本预算和白名单管理。

## 功能模块

| 模块 | 功能 |
|------|------|
| 👥 **员工管理** | 员工信息的增删改查、分页、搜索、排序 |
| 📥 **批量导入** | 通过 CSV 文件批量导入员工，支持模板下载 |
| 💰 **成本预算** | 部门预算概览、支出记录管理 |
| 🛡️ **白名单管理** | 白名单维护，支持手动添加和从员工列表批量导入 |

## 快速开始

1. 使用浏览器打开 `index.html` 即可使用
2. 所有数据存储在浏览器本地 (localStorage)
3. 首次使用请先通过"新增员工"或"批量导入"添加员工数据

## CSV 导入格式

支持 UTF-8 编码的 CSV 文件，列顺序为：

```
姓名,工号,部门,职位,手机,邮箱,预算,备注
```

可在批量导入页面下载模板文件。

## 技术栈

- HTML5 + CSS3 + Vanilla JavaScript (ES6+)
- localStorage 数据持久化
- 无需后端服务，开箱即用
```

---

## Self-Review

### 1. Spec Coverage

| 需求 | 对应 Task | 实现方式 |
|------|-----------|---------|
| 员工基本信息录入 | Task 2 | 新增员工表单，含工号/姓名/部门/职位/手机/邮箱/预算/备注 |
| 增删改查 | Task 2 | EmployeeModule 完整 CRUD + 搜索 + 分页 + 排序 |
| 批量导入 | Task 3 | CSV 文件解析，支持逗号分隔和引号包裹，预览后确认导入 |
| 成本预算 | Task 4 | 部门预算概览表 + 支出记录管理，支出关联员工 |
| 白名单管理 | Task 5 | 手动添加 + 从员工列表批量导入，支持搜索和分页 |
| 记录成本预算 | Task 2+4 | 每个员工可设置预算，支出记录单独管理，展示部门级汇总 |

### 2. Placeholder Scan

- 所有代码块包含完整实现，无 "TBD"/"TODO"/"implement later" 等占位符
- 所有函数和方法都有完整实现
- 错误处理和边界条件已覆盖（空数据、文件格式错误、重复工号等）

### 3. Type Consistency

- `Storage.generateId()` 在 Task 1 定义，在 Task 2-5 中一致使用
- `EmployeeModule.getList()` 和 `EmployeeModule.add()` 在 Task 2 定义，在 Task 3-5 中正确引用
- 各模块 `render(container)` 接口签名一致
- `App.registerPage()` 和 `App.navigate()` 接口一致

---

**Plan complete and saved to `docs/superpowers/plans/2025-04-10-personnel-dashboard.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**