<template>
  <div class="user-info">
    <div v-if="isLoggedin">
    <b-alert show variant="warning">
        <h3 class="alert-heading">{{ userGrade }} {{ loginUser["user"].name }}({{ loginUser["user"].id }})님, 환영합니다!</h3>
        <hr>
        <div v-if="reqStamps!=-1">
        <b-progress :value="reqStampMax - reqStamps" :max="reqStampMax" 
                    height="2rem" variant="warning" class="my-3"
                    show-value animated>
        </b-progress>
        <h5 small>전체 스탬프 개수 : {{ loginUser["user"].stamps }}개</h5>
        <h5 small>다음 등급으로 가려면? : {{ reqStamps }}개</h5>
        </div>
        <div v-if="reqStamps==-1">
        <h5 small>최고 등급을 달성하셨습니다! 감사합니다!!</h5>
        </div>
    </b-alert>

    <b-card header="최근 한달 간 주문 내역">
        <p class="card-text mt-2">
            주문 정보를 클릭하면 주문 내역을 살펴볼 수 있습니다.
        </p>

        <div class="accordion" role="tablist" 
          v-for="(order, idx) in orderByUser" :key="idx">
          <b-card no-body class="mb-1" v-if="!isSameOrder(idx)">
            <b-card-header header-tag="header" class="p-1" role="tab">
              <b-button block v-b-toggle="'accordion-' + idx" 
                variant="light" class="text-left">
                  {{ order.order_time }}, 주문 방식 : 웹 주문
                </b-button>
            </b-card-header>
            <b-collapse :id="'accordion-' + idx" accordion="my-accordion" role="tabpanel">
              <b-alert show variant="warning" class="m-3">
                <h3 class="alert-heading my-3">주문 상세</h3>
                <!-- 해당 order의 주문 상세 정보를 출력하자. -->
                <b-list-group v-for="(item, index) in orderByUser" :key="index">
                  <b-list-group-item class="d-flex align-items-center"
                    v-if="order.o_id == item.o_id">
                    <b-avatar variant="info" :src="require('@/assets/menu/' + item.img)" class="mr-3"/>
                    제품명 : {{ item.name }}, 금액 : {{ item.price }}원, {{ item.quantity }}잔
                    <b-badge variant="primary" class="ml-3" pill>{{ item.price * item.quantity }}원</b-badge>
                  </b-list-group-item>
                </b-list-group>
                <hr>
                <h5 small>총 금액 : {{ totalPrice(order.o_id) }}원</h5>
                <h5 small>스탬프 적립 : {{ totalStamps(order.o_id) }}개</h5>
              </b-alert>
            </b-collapse>
          </b-card>
        </div>
    </b-card>    
    </div>
    <div v-if="!isLoggedin">
    <h2>로그인 상태여야만 회원 정보를 볼 수 있습니다.</h2>
    </div>
  </div>
</template>

<script>
export default {
    name: 'user-info',
    methods: {
      movePage(url) { // 화면 이동
        this.$router.push(url);
        // 주문 내역의 p_id에 페이지 이동을 넣을지 말지 고민해보자
      },
      totalPrice(o_id) {
        let sum = 0
        for (let i = 0; i < this.orderByUser.length; i++) {
          if (this.orderByUser[i].o_id == o_id) {
            sum += this.orderByUser[i].price * this.orderByUser[i].quantity
          }
        }
        return sum
      },
      totalStamps(o_id) {
        let sum = 0
        for (let i = 0; i < this.orderByUser.length; i++) {
          if (this.orderByUser[i].o_id == o_id) {
            sum += this.orderByUser[i].quantity
          }
        }
        return sum
      },
      isSameOrder(idx) {
        if (idx > 0) {
          if (this.orderByUser[idx-1].o_id == this.orderByUser[idx].o_id) {
            return true
          }
        }
        return false
      }
    },
    created() {
      // 이걸 computed userGrade에 넣으니까 화면 표시 딜레이가 있더라 여기서 작업하자
      this.$store.dispatch('selectUserInfo', this.loginUser['user'].id)
    },
    computed: {
      loginUser() {
        return this.$store.getters.getLoginUser;
      },
      isLoggedin() {
        return this.$store.getters.getIsLoggedin;
      },
      orderByUser() {
        this.$store.dispatch('selectOrderByUser', this.loginUser['user'].id)
        return this.$store.getters.getOrderByUser;
      },
      userGrade() {
        return this.$store.getters.getUserGrade;
      },
      reqStampMax() {
        return this.$store.getters.getReqStampMax;
      },
      reqStamps() {
        return this.$store.getters.getReqStamps;
      },
    },
}

</script>

<style>
.user-info {
  padding: 24px;
}
</style>
