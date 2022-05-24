import Vue from 'vue'
import VueRouter from 'vue-router'
import ProductListView from '../views/ProductListView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import UserInfoView from '../views/UserInfoView.vue'
import ProductDetailView from '../views/ProductDetailView.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'product-list',
    component: ProductListView  
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },
  {
    path: '/user-info',
    name: 'user-info',
    component: UserInfoView
  },
  {
    path: '/product-detail/:no',
    name: 'product-detail',
    component: ProductDetailView  
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router

/*
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/about',
    name: 'about',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" *//* '../views/AboutView.vue')
  }
*/