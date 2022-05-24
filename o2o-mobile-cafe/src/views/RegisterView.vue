<template>
  <div class="register">
    <h2>회원 가입</h2>

    <!-- <b-form @submit="onSubmit" @reset="onReset"> -->
    <b-form @submit="onSubmit" @reset="onReset">
      <b-form-group id="input-group-id" label="아이디: " label-for="input-id">
        <b-form-input
          id="input-id"
          v-model="form.id"
          type="id"
          placeholder="아이디를 입력해주세요."
          required
        ></b-form-input>
        <!-- <b-form-invalid-feedback :state="!emptyid" v-if="emptyid">
          내용이 비어 있습니다.
        </b-form-invalid-feedback> -->
        <b-form-invalid-feedback :state="validation">
          중복되는 아이디입니다.
        </b-form-invalid-feedback>
        <b-form-valid-feedback :state="validation" v-if="!emptyid">
          사용가능한 아이디입니다.
        </b-form-valid-feedback>
      </b-form-group>

      <b-form-group id="input-group-name" label="이름:" label-for="input-name">
        <b-form-input
          id="input-name"
          v-model="form.name"
          placeholder="이름을 입력해주세요."
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

      <b-button class="px-3 m-1 my-5" type="submit" variant="dark">가입</b-button>
      <b-button class="px-3 m-1 my-5" type="reset" variant="outline-dark">취소</b-button>
    </b-form>
  </div>
</template>

<script>
  import http from '@/util/http-common';

  export default {
    data() {
      return {
        form: {
          id: '',
          name: '',
          pass: ''
        },
        invalid: false
      }
    },
    methods: {
      onSubmit(event) {        
        event.preventDefault()
        if (this.invalid) {
          alert("아이디가 올바르지 않습니다!")
        }
        else {
          http
          .post('/user', this.form)
          .then((response) => {
            console.log("setData : " + response.data);          
            alert("가입 성공했습니다!")
          })
          .catch((error) => {
            console.log(error)
            alert("네트워크 오류!")
          })

          this.$router.push('/');
        }
      },
      onReset(event) {
        event.preventDefault()
        this.form.id = ''
        this.form.name = ''
        this.form.pass = ''
        
        this.$router.push('/');
      },
      isUsed() {
        http
        .get('/user/isUsed', {params: {id: this.form.id}})
        .then(response => {
          this.invalid = response.data; // true or false
          // console.log("Status Code : " + response.status);
          // console.log("Data : " + response.data);
        })
        .catch((error) => {
          console.log(error);
        })
      }
    },
    computed: {
      validation() {
        if(!this.isUsed()) {
          this.isUsed();
        }
        return !this.invalid;
      },
      emptyid() {
        return this.form.id == ''
      }
    }
  }

</script>

<style>
.register {
  padding: 24px;
}

</style>
