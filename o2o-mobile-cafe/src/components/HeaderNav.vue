<template>
    <div>
    <b-navbar toggleable="lg" type="dark" variant="danger">
        <b-navbar-brand href="#" @click="movePage('/')">
            <img src="@/assets/logo.png" width="50" height="50" alt="logo"/>
            SSAFY CAFE
        </b-navbar-brand>

        <b-navbar-toggle target="nav-collapse"></b-navbar-toggle>

        <b-collapse id="nav-collapse" is-nav>

        <!-- Right aligned nav items -->
        <b-navbar-nav class="ml-auto" v-show="!isLoggedin">
            <b-button @click="movePage('/login')" class="my-2 my-sm-0 mx-1" type="submit" variant="light">로그인</b-button>
            <b-button @click="movePage('/register')" class="my-2 my-sm-0 mx-1" type="submit" variant="warning">회원가입</b-button>
        </b-navbar-nav>

        <b-navbar-nav class="ml-auto" v-if="isLoggedin" v-show="isLoggedin">
            <b-button @click="movePage('/user-info')" class="my-2 my-sm-0 mx-1" type="submit" variant="light">{{ loginUser["user"].id }}</b-button>
            <b-button @click="logout()" class="my-2 my-sm-0 mx-1" type="submit" variant="warning">로그아웃</b-button>
        </b-navbar-nav>

        </b-collapse>
    </b-navbar>
    </div>
</template>

<script>
export default {
    name: 'header-nav',
    methods: {
        movePage(url) { // 화면 이동
            this.$router.push(url);
        },
        logout() {
            this.$store.dispatch('logout')
            this.movePage('/')
        }
    },
    computed: {
      loginUser() {
        return this.$store.getters.getLoginUser;
      },
      isLoggedin() {
        return this.$store.getters.getIsLoggedin;
      }
    },
}
// method : 매 호출 시 새롭게 사용
// computed : 처음 호출된 값을 저장해서 사용

// 로그인 상태인지 아닌지에 따라, 표기되는 화면 상태가 다름.
// => 어떤 부분이 다르냐면, 로그인을 하지 않으면 메뉴를 보는 것만 가능함.
</script>
