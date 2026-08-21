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