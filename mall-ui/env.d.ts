/// <reference types="vite/client" />

// 自定义环境变量类型补充：API 基地址（开发期留空走 vite 代理，生产经网关/同源）。
interface ImportMetaEnv {
  readonly VITE_API_BASE?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}