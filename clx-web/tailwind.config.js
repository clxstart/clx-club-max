/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Acid Graphics 配色
        acid: {
          green: '#39ff14',
          yellow: '#ffff00',
          purple: '#a020f0',
          pink: '#ff006e',
          cyan: '#00ffff',
          dark: '#0a0a0a',
        },
      },
      fontFamily: {
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
  // Ant Design 兼容
  corePlugins: {
    preflight: false,
  },
}