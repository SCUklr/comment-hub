import React from 'react';

export type ToastType = 'success' | 'error' | 'info';

let pushFn: ((msg: string, type?: ToastType) => void) | null = null;

export function registerToast(fn: (msg: string, type?: ToastType) => void) {
  pushFn = fn;
}

export function toast(msg: string, type: ToastType = 'success') {
  pushFn?.(msg, type);
}
