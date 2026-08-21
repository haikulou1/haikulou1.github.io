// 白名单管理模块
const WhitelistModule = {
  _currentPage: 1,
  _pageSize: 15,
  _searchKeyword: '',

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