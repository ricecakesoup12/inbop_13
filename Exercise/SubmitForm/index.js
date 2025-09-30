import { createRouter, createWebHistory } from 'vue-router'
import RoleSelect from './exercise/RoleSelect.vue'
import HealthSurvey from './exercise/user/HealthSurvey.vue'
import RoutineResult from './exercise/user/RoutineResult.vue'
import GuardianDashboard from './exercise/guardian/GuardianDashboard.vue'
import GuardianProfile from './exercise/guardian/GuardianProfile.vue'

const routes = [
  { 
    path: '/', 
    redirect: '/role-select' 
  },
  { 
    path: '/role-select', 
    component: RoleSelect,
    name: 'RoleSelect'
  },
  // User routes
  { 
    path: '/user/survey', 
    component: HealthSurvey,
    name: 'UserSurvey'
  },
  { 
    path: '/user/result', 
    component: RoutineResult,
    name: 'UserResult'
  },
  // Guardian routes
  { 
    path: '/guardian/dashboard', 
    component: GuardianDashboard,
    name: 'GuardianDashboard'
  },
  { 
    path: '/guardian/profile/:id', 
    component: GuardianProfile,
    name: 'GuardianProfile'
  },
  // Legacy routes for backward compatibility
  { 
    path: '/survey', 
    redirect: '/user/survey'
  },
  { 
    path: '/result', 
    redirect: '/user/result'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
