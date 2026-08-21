// 批量导入模块
const ImportModule = {
  _importedData: [],

  render(container) {
    container.innerHTML = `
      <div class="page-header">
        <h1>📥 批量导入</h1>
        <p>通过 CSV 文件批量导入员工信息</p>
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
    result.data.forEach((emp) => {
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