// Demo 全局常量与展示映射

export const DEFAULT_OBJECT_ID = 10001;
export const DEFAULT_OBJECT_TYPE = 1;

// 演示用户池：可切换 X-User-Id
export const DEMO_USERS = [
  { id: 10001, name: '作者小张' },
  { id: 10002, name: '用户小李' },
  { id: 10003, name: '用户小王' },
  { id: 10004, name: '运营小美' },
];

export const AUDIT_STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '待审核', color: '#d48806' },
  1: { label: '已通过', color: '#389e0d' },
  2: { label: '已拒绝', color: '#cf1322' },
};

export const NOTIFICATION_TYPE_MAP: Record<number, string> = {
  1: '回复',
  2: '点赞',
};

export function formatTime(iso: string): string {
  if (!iso) return '-';
  return iso.replace('T', ' ').slice(0, 19);
}
