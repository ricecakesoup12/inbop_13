import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/pages/Landing.vue'),
  },
  {
    path: '/guardian',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'GuardianHome',
        component: () => import('@/pages/guardian/GuardianHome.vue'),
      },
      {
        path: 'users/:id',
        name: 'UserDetail',
        component: () => import('@/pages/guardian/UserDetail.vue'),
      },
      {
        path: 'users/:id/survey/send',
        name: 'SurveySend',
        component: () => import('@/pages/guardian/SurveySend.vue'),
      },
      {
        path: 'users/:id/survey/result',
        name: 'SurveyResult',
        component: () => import('@/pages/guardian/SurveyResult.vue'),
      },
    ],
  },
  {
    path: '/user/register',
    name: 'UserRegister',
    component: () => import('@/pages/user/UserRegister.vue'),
  },
  {
    path: '/user',
    component: () => import('@/layouts/AppShell.vue'),
    children: [
      {
        path: '',
        name: 'UserDashboard',
        component: () => import('@/pages/user/UserDashboard.vue'),
      },
      {
        path: 'survey/:requestId',
        name: 'UserSurvey',
        component: () => import('@/pages/user/UserSurvey.vue'),
      },
      {
        path: 'survey/result',
        name: 'UserSurveyResult',
        component: () => import('@/pages/user/UserSurveyResult.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router

