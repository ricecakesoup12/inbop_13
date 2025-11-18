/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: '#8BC34A',
        'primary-hover': '#7CB342',
        secondary: '#9E9E9E',
        accent: '#D7CCC8',
        beige: '#F7F5F3',
        'text-main': '#424242',
        'text-sub': '#757575',
        'text-light': '#BDBDBD',
      },
      fontFamily: {
        'gowun': ['Gowun Dodum', 'sans-serif'],
      },
    },
  },
  plugins: [],
}

