<template>
  <div class="product-list">
    <div class="mb-4" v-if="!isLoggedin">
      <b-alert show variant="warning" align="center">상품을 주문하려면 로그인을 해야 합니다.</b-alert>
    </div>

    <b-container fluid="xl">
    <b-row>
      <b-col>
        <b-row>
            <!-- <b-link :to="/product-detail/ + item.id"> -->
            <b-col cols="3" v-for="(item, idx) in products" :key="idx" class="col-md-3 p-0">
                <b-card img-top align="center" class="mb-4 p-0">
                    <b-card-img :src="require('@/assets/menu/' + item.img)"
                      width="100%" height="300px" class="mb-3 p-0 btn"
                      @click="movePage('/product-detail/' + item.id)"/>
                    <h5 small>{{ item.name }}</h5>
                    <b-card-text>{{ item.price }}원</b-card-text>

                    <div v-if="isLoggedin">
                    <b-button variant="dark" small @click="plusCount(idx)">+</b-button>
                    <span class="mx-2">{{ counts[idx] }}</span>
                    <b-button variant="dark" small @click="minusCount(idx)">-</b-button>    
                    </div>
                </b-card>            
            </b-col>
        </b-row>
      </b-col>
      <b-col class="col-2" v-if="isLoggedin">
        <b-card header="주문서" class="text-center mb-3">
        <b-row v-for="(item, idx) in counts" :key="idx" >  
          <b-col class="p-0" v-if="item > 0">
            <b-card class="p-0">
              <!-- 여유가 될 때 삭제하는 버튼을 만들어 보자 -->
              <b-avatar size="2em" variant="info" :src="require('@/assets/menu/' + products[idx].img)" class="mr-1"/>
              <b-card-text class="mt-2">{{ products[idx].name }}</b-card-text>
              <b-badge variant="dark" pill class="m-0">{{ item }}</b-badge>
            </b-card>
          </b-col>
        </b-row>

          <b-button class="mt-3" block @click="order" v-if="totalCount > 0">주문</b-button>
   
        </b-card>
      </b-col>      
    </b-row>
    </b-container>
  </div>
</template>

<script>
import http from '@/util/http-common';

export default {
  data() {
    return {
      products: [],
      // TODO : 이제 숫자 달리 보이게 하고 저장하는 배열을 만들자
      totalCount: 0,
      counts: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }
  },
  methods: {
    movePage(url) {
      this.$router.push(url);
    },
    plusCount(idx) {
      let value = this.counts[idx]
      this.$set(this.counts, idx,  + value + 1)
      this.totalCount++
      // vue는 인덱스로 배열에 있는 항목을 직접 설정하는 경우, 변경 사항 감지X - 그래서 위와같이 씀
    },
    minusCount(idx) {
      let value = this.counts[idx]
      if (value > 0) {
        this.totalCount--
        this.$set(this.counts, idx,  + value - 1)
      }
    },
    order() {
      // 1. order detail 설정
      let detailList = []
      let msg = ''
      for (let i = 0; i < this.counts.length; i++) {
        if (this.counts[i] > 0) {
          detailList.push(
          {
            "orderId": 0,
            "productId": this.products[i].id,
            "quantity": this.counts[i]
          })
          msg += (this.products[i].name + " " + this.counts[i] + "잔\n")
        }
      }
      msg += "\n위 주문 내용으로 구매하시겠습니까?"

      // let today = new Date()
      // let time = today.getFullYear() + '-' + (today.getMonth() + 1) + '-' + today.getDate() + ' '
      // time += (today.getHours() + ':' + today.getMinutes() + ':' + today.getSeconds())

      // 2. 주문 내용 확인
      var order = {
        "completed": 'N',
        "details": detailList,
        "orderTable": 'web',
        "userId": this.loginUser['user'].id
      }

      if (confirm(msg) == true) {
        // post('/order')로 order 객체 추가한다. (& body order)
        http.post('/order', order)
        .then(response => {
            console.log("주문서 번호 : " + response.data)
            if (response.data) {
                alert("주문이 성공적으로 완료되었습니다!")
                // counts 초기화
                for (let i = 0; i < detailList.length; i++) {
                  this.$set(this.counts, 10 - detailList[i].productId, 0)
                }
                this.totalCount = 0
                // 새로고침
                this.$router.go()
            }
        })
        .catch(error => {
            console.log(error)
        })        
      }
    }
  },
  created() { // 페이지가 생성되면 전체 상품 정보를 가져온다.
    http.get('/product')
    .then((response) => {
      this.products = response.data
      console.log(this.products)
    })
    .catch((error) => {
      console.log(error)
    })
    // store에서 비동기 작업을 거치고 products 정보를 받으니
    // 작업 속도가 느려서 여기에서 비동기 작업을 수행 

    this.$store.dispatch('selectProducts')
  },
  computed: {
    loginUser() {
      return this.$store.getters.getLoginUser
    },
    isLoggedin() {
      return this.$store.getters.getIsLoggedin
    }
  }
}
</script>

<style>
.product-list {
  padding: 24px;
}
</style>
