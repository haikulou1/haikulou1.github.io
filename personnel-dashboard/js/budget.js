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