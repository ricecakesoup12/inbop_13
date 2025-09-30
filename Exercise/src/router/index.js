import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import GuardianDashboard from '../views/GuardianDashboard.vue'
import UserDashboard from '../views/UserDashboard.vue'
import ExerciseStart from '../views/ExerciseStart.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/guardian',
    name: 'GuardianDashboard',
    component: GuardianDashboard
  },
  {
    path: '/user',
    name: 'UserDashboard',
    component: UserDashboard
  },
  {
    path: '/exercise',
    name: 'ExerciseStart',
    component: ExerciseStart
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router


