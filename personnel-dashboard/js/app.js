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