/* 萌宠社区风 (Pet Adopt Community) - Tailwind Preset */
/* Generated for CLX Project */

/** @type {import('tailwindcss').Config} */
module.exports = {
  theme: {
    extend: {
      colors: {
        brand: {
          primary: '#F8A323',
          orange: '#F58418',
          pink: '#FF7E93',
        },
        tag: {
          blue: '#368BE3',
          green: '#70AF48',
          coral: '#F08080',
          yellow: '#fca400',
        },
        'pet-text': {
          primary: '#333333',
          secondary: '#778288',
          muted: '#999999',
        },
        'pet-bg': {
          page: '#F8F8F8',
          card: '#FFFFFF',
        },
      },
      borderRadius: {
        'pet-sm': '10rpx',
        'pet-md': '20rpx',
        'pet-lg': '30rpx',
        'pet-full': '40rpx',
      },
      boxShadow: {
        'pet-card': '0 2rpx 12rpx rgba(206, 225, 235, 0.5)',
        'pet-card-hover': '0 4rpx 20rpx rgba(206, 225, 235, 0.8)',
      },
      fontFamily: {
        pet: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'sans-serif'],
      },
    },
  },
};
