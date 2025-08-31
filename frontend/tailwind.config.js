/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'primary-blue': '#007bff',
        'secondary-blue': '#4c95eb',
        'primary-green': '#28a745',
        'primary-green-hover': '#218838',
        'soft-gray': '#f8f9fa',
        'dark-blue-600': '#2563eb',
        'dark-gray-600': '#4b5563',
      },
      boxShadow: {
        'custom-light': '0 4px 6px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0, 0, 0, 0.08)',
        'custom-medium': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
        'neumorphic': '6px 6px 12px #cbced1, -6px -6px 12px #ffffff',
        'neumorphic-inner': 'inset 6px 6px 12px #cbced1, inset -6px -6px 12px #ffffff',
      },
    },
  },
  plugins: [],
}