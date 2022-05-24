import Vue from 'vue'
import Vuex from 'vuex'
import http from '@/util/http-common'
import createPersistedState from 'vuex-persistedstate'

Vue.use(Vuex)

export default new Vuex.Store({
  state: { // data 저장소
    products: [],

    loginUser: [],
    isLoggedin: false,
    orderByUser: [],

    // 회원 등급 관리
    userGrade: '', // 현재 등급
    reqStampMax: 0, // 현재 등급에서 승급에 필요한 최대 스탬프 수
    reqStamps: 0 // 승급에 필요한 스탬프 수
  },
  getters: { // state 기반 계산(~= computed) = state를 직접 접근하는 코드
    getProducts(state) {
      return state.products
    },
    getLoginUser(state) {
      return state.loginUser
    },
    getIsLoggedin(state) {
      return state.isLoggedin
    },
    getOrderByUser(state) {
      return state.orderByUser
    },
    getUserGrade(state) {
      return state.userGrade
    },
    getReqStampMax(state) {
      return state.reqStampMax
    },
    getReqStamps(state) {
      return state.reqStamps
    }
  },
  mutations: { // state의 값을 변경하는 유일한 방법(동기 methods)
    SET_PRODUCTS(state, products) {
      state.products = products
    },
    LOGIN_USER(state, loginUser) {
      state.loginUser = loginUser
      state.isLoggedin = true
      //console.log('Login User : ' + JSON.stringify(state.loginUser))
    },
    LOGOUT_USER(state) {
      state.loginUser = []
      state.isLoggedin = false
      state.orderByUser = []
    },
    SET_USER_GRADE(state, stamps) {
      const G1_NAME = '씨앗'; const G1_STAMP = 10; const G1_MAX = G1_STAMP * 5;
      const G2_NAME = '꽃'; const G2_STAMP = 15; const G2_MAX = G1_MAX + G2_STAMP * 5;
      const G3_NAME = '열매'; const G3_STAMP = 20; const G3_MAX = G2_MAX + G3_STAMP * 5;
      const G4_NAME = '커피콩'; const G4_STAMP = 25; const G4_MAX = G3_MAX + G4_STAMP * 5;
      const G5_NAME = '나무';

      // stamps = 360 (등급 동작 확인용)

      if (stamps < G1_MAX) {
        state.userGrade = G1_NAME + (parseInt(stamps/10) + 1) + '단계'
        state.reqStampMax = G1_STAMP
        state.reqStamps = G1_STAMP - stamps%G1_STAMP
      }
      else if (stamps < G2_MAX) {
        state.userGrade = G2_NAME + (parseInt((stamps - G1_MAX)/G2_STAMP) + 1) + '단계'
        state.reqStampMax = G2_STAMP
        state.reqStamps = G2_STAMP - (stamps - G1_MAX)%G2_STAMP
      }
      else if (stamps < G3_MAX) {
        state.userGrade = G3_NAME + (parseInt((stamps - G2_MAX)/G3_STAMP) + 1) + '단계'
        state.reqStampMax = G3_STAMP
        state.reqStamps = G3_STAMP - (stamps - G2_MAX)%G3_STAMP
      }
      else if (stamps < G4_MAX) {
        state.userGrade = G4_NAME + (parseInt((stamps - G3_MAX)/G4_STAMP) + 1) + '단계'
        state.reqStampMax = G4_STAMP
        state.reqStamps = G4_STAMP - (stamps - G3_MAX)%G4_STAMP
      }
      else {
        state.userGrade = G5_NAME + '단계!!'
        state.reqStamps = -1 // 더 이상 올라갈 단계가 없음!
      }
    },
    SET_ORDER_BY_USER(state, orders) {
      state.orderByUser = orders
      //console.log("Response.data : " + JSON.stringify(state.orderByUser))
    }
  },
  actions: { // 변이에 대한 커밋 처리 (비동기 methodss)
    selectProducts({ commit }) {
      http.get('/product')
      .then((response) => {
        commit('SET_PRODUCTS', response.data)
      })
      .catch((error) => {
        console.log(error)
      })
    },
    login({ commit, dispatch }, form) {
      var user = form.data
      // 1. 현재 입력한 id로 사용자 정보를 찾고,
      http.post('/user/info', null, {params: {id: user.id}}) // form.id X
        .then((response) => {
          // console.log("로그인으로 얻은 데이터 " + JSON.stringify(response.data))
          if (Object.keys(response.data).length === 0) {
            alert('없는 아이디입니다!')
          }
          else {
            // 2. 존재한다면, pass가 일치하는지 확인하자.
            if (response.data["user"].pass === user.pass) {              
              alert('로그인 성공!!!')
              // console.log(response.data)
              commit('LOGIN_USER', response.data)

              // stamps 정보로 stamp 등급 계산하기
              commit('SET_USER_GRADE', response.data["user"].stamps)

              // {id}에 해당하는 사용자의 최근 1개월간 주문 내역 반환.
              dispatch('selectOrderByUser', response.data["user"].id)

              // 성공적 로그인 이후, loginId라는 쿠키 받기
              dispatch('setCookie', response.data)
            }
            else {
              alert('실패....')
            }
          }
        })
        .catch((error) => {
          console.log(error)
        })      
    },
    logout({ commit }) {
      commit('LOGOUT_USER')
    },
    selectUserInfo({ commit, state }, userid) {
      if (!state.isLoggedin) {
        alert('로그인 상태가 아님!!')
      }
      else {
        http.post('/user/info', null, {params: {id: userid}})
        .then(response => {
          commit('LOGIN_USER', response.data)

          // stamps 정보로 stamp 등급 계산하기
          commit('SET_USER_GRADE', response.data["user"].stamps)
        })
        .catch(error => {
          console.log(error)
        })        
      }
    },
    selectOrderByUser({ commit }, id) {
      http.get('/order/byUser', {params: {id: id}})
      .then((response) => {
        commit('SET_ORDER_BY_USER', response.data)
      })
      .catch((error) => {
        console.log(error)
      })
    },
    setCookie(_, data) {
      // 쿠키 설정해 로그인 상태 유지
      http.post('/user/login', data)
      .then(response => { // response == cookie
        if (response.status == 200) { // 성공!
          // 쿠키에 토큰 설정
          console.log("Cookie Response : " + JSON.stringify(response))
          // window.$cookies.set("accessToken", response)
          // http.defaults.headers.common["x-access-token"] = response
          // window.$cookies.set("id", state.loginUser['user'].id)
          window.$cookies.set("loginuser", response)
        }
      })
      .catch(error => {
        console.log(error)
      })
    }
  },
  modules: {
  },
  plugins: [
    createPersistedState(
    // {
    //   storage: window.sessionStorage,
    //   reducer: state => ({
    //     loginUser: [],
    //     isLoggedin: false
    //   })
    // }
    )
  ]
})
