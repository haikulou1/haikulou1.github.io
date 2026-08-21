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
                <th data-sort="employeeId" class="sortable" style="cursor:pointer;">工号</th>
                <th data-sort="name" class="sortable" style="cursor:pointer;">姓名</th>
                <th data-sort="department" class="sortable" style="cursor:pointer;">部门</th>
                <th data-sort="position" class="sortable" style="cursor:pointer;">职位</th>
                <th>手机</th>
                <th>邮箱</th>
                <th data-sort="budget" class="sortable" style="cursor:pointer;">预算(¥)</th>
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