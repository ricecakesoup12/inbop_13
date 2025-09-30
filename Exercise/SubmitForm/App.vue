<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// Router view will handle component rendering
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Gowun Dodum', sans-serif;
  background-color: #f5f5f5;
  padding: 20px;
}

#app {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
}
</style>
