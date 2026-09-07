<template>
  <div class="blank-layout">
    <router-view />
  </div>
</template>

<style scoped>
.blank-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 100vh;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background: #060b12;
}
</style>

<style>
.blank-layout > .equip-screen-root {
  flex: 1;
  min-height: 0;
  min-width: 0;
}
</style>
