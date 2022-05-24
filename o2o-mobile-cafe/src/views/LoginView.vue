<template>
  <div class="login">
    <h2>로그인</h2>

    <b-form @submit="onLogin" @reset="onReset">
      <b-form-group id="input-group-id" label="아이디: " label-for="input-id">
        <b-form-input
          id="input-id"
          v-model="form.id"
          type="id"
          placeholder="아이디를 입력해주세요."
          required
        ></b-form-input>
      </b-form-group>

      <b-form-group id="input-group-pass" label="비밀번호:" label-for="input-pass">
        <b-form-input
          id="input-pass"
          v-model="form.pass"
          placeholder="비밀번호를 입력해주세요."
          required
        ></b-form-input>
      </b-form-group>

      <b-button class="px-3 m-1 my-5" type="submit" variant="dark">로그인</b-button>
      <b-button class="px-4 m-1 my-5" type="reset" variant="outline-dark">리셋</b-button>
    </b-form>
  </div>
</template>

<script>
//import http from '@/util/http-common';

  export default {
    data() {
      return {
        form: {
          id: '',
          pass: ''
        },
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
    methods: {
      onLogin(event) {        
        event.preventDefault()
        this.$store.dispatch('login', {data: this.form})
        this.$router.push('/');
      },
      onReset(event) {
        event.preventDefault()
        this.form.id = ''
        this.form.pass = ''
      },
    }
  }
</script>

<style>
.login {
  padding: 24px;
}
</style>
