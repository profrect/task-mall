class StorageUtil {
  static setItem(key: string, value: unknown): void {
    try { localStorage.setItem(key, JSON.stringify(value)); } catch (e) { console.error(e); }
  }
  static getItem<T = unknown>(key: string): T | null {
    try { const item = localStorage.getItem(key); return item ? JSON.parse(item) : null; } catch (e) { console.error(e); return null; }
  }
  static removeItem(key: string): void {
    try { localStorage.removeItem(key); } catch (e) { console.error(e); }
  }
  static clear(): void {
    try { localStorage.clear(); } catch (e) { console.error(e); }
  }
}
export default StorageUtil;
