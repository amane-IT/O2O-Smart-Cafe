<template>
  <div class="product-detail">
    <b-container fluid="xl">
    <h2 class="mb-4">{{ products[10 - no].name }} 상세 정보</h2>
    <!-- <h2>{{ this.$route.params.no }}번 상품 상세 페이지입니다!</h2> -->
    <b-alert show variant="warning">
        <h3 class="alert-heading mb-4 text-center">상품 평점</h3>
        <b-row>
        <b-img :src="require('@/assets/menu/' + products[10 - no].img)"
            thumbnail rounded class="col-2"
            ></b-img>
        <b-list-group class="col-10 text-center align-self-center">
            <b-list-group-item>상품명 : {{ products[10 - no].name }}</b-list-group-item>
            <b-list-group-item>가격 : {{ products[10 - no].price }}원</b-list-group-item>
            <b-list-group-item>총 주문 수량 : {{ product[0].sells }}개</b-list-group-item>
            <b-list-group-item>평가 수 : {{ product[0].commentCnt }}개</b-list-group-item>
            <b-list-group-item v-if="product[0].commentCnt > 0">평균 평점 : {{ product[0].avg }}점</b-list-group-item>
            <b-list-group-item v-if="product[0].commentCnt == 0">평점 없음</b-list-group-item>
        </b-list-group>
        </b-row>
        <b-button v-show="isLoggedin" class="my-4 p-3" block 
            variant="warning" v-b-modal.modal-add-comment>
            한줄평 남기기
        </b-button>
        <hr>
        <p>자신이 남긴 평가만 수정, 삭제할 수 있습니다.</p>
        
        <table class="table table-bordered table-active">
        <thead class="text-center">
            <tr class="table-light">
                <th scope="col" class="col-2">사용자</th>
                <th scope="col" class="col-1">평점</th>
                <th scope="col" class="col-7">한줄평</th>
                <th scope="col" class="col-2"></th>
            </tr>
        </thead>
        <tbody>
            <tr class="table-light text-center" v-for="(item, idx) in product" :key="idx">
                <th class="col-2">{{ item.user_id }}</th>
                <td class="col-1">{{ item.rating }}</td>
                <td class="col-7">{{ item.comment }}</td>
                <!-- 아래 내용이 표기되지 않는 건 로그아웃 상태를 고려하지 않았기 때문 (isLoggedin을 조건으로 추가해주니 해결)-->
                <!-- 여기서 오류가 나니까 나머지 부분에서도 오류가 나더라-->
                <td class="col-2" v-if="!isLoggedin || item.user_id != loginUser['user'].id"></td>
                <td class="col-2" v-if="isLoggedin && item.user_id == loginUser['user'].id">
                    <b-button @click="updateSubmittedComment(idx)" variant="success" size="sm" v-b-modal.modal-update-comment>수정</b-button>
                    <b-button @click="deleteComment(idx)" class="ml-1" variant="danger" size="sm">삭제</b-button>
                </td>
            </tr>
        </tbody>
        </table>
    </b-alert>

    <b-modal id="modal-add-comment" ref="modal"
        title="한줄평 남기기"
        @show="resetModal" 
        @hidden="resetModal"
        @ok="handleOk">
        <form ref="formRating">
            <b-form-group :label="'평점: ' + rating + '점'" 
                label-for="rating-input">
                <b-form-input id="rating-input"
                    type="range" min="0" max="10" 
                    v-model="rating"></b-form-input>    
            </b-form-group>
        </form>        
        <form ref="form" @submit.stop.prevent="handleSubmit">
            <b-form-group label="한줄평: "
                label-for="comment-input"
                invalid-feedback="내용을 입력해주세요!"
                :state="commentState">
                <b-form-input id="comment-input" 
                    v-model="comment" 
                    :state="commentState" 
                    required></b-form-input>
            </b-form-group>
        </form>
    </b-modal>

    <b-modal id="modal-update-comment" ref="modal"
        title="한줄평 수정하기"
        @show="resetModal" 
        @hidden="resetModal"
        @ok="handleUpdateOk">
        <form ref="formRating">
            <b-form-group :label="'평점: ' + submittedUpdateComment['rating'] + '점'" 
                label-for="rating-input">
                <b-form-input id="rating-input"
                    type="range" min="0" max="10" 
                    v-model="submittedUpdateComment['rating']"></b-form-input>    
            </b-form-group>
        </form>        
        <form ref="form" @submit.stop.prevent="handleUpdateSubmit">
            <b-form-group label="한줄평: "
                label-for="comment-input"
                invalid-feedback="내용을 입력해주세요!"
                :state="commentState">
                <b-form-input id="comment-input" 
                    v-model="submittedUpdateComment['comment']" 
                    :state="commentState" 
                    required></b-form-input>
            </b-form-group>
        </form>
    </b-modal>    
    </b-container>
  </div>
</template>

<script>
import http from '@/util/http-common'

export default {
  name: 'product-detail',
  data() {
    return {
      product: [],
      no: 0,

      comment: '',
      commentState: null,
      submittedComment: {          
        "comment": '',
        "productId": 0,
        "rating": 0,
        "userId": ''
      }, // DB에 올라갈 정보
      submittedUpdateComment: {
        "comment": '',
        "id": 0,
        "productId": 0,
        "rating": 0,
        "userId": ''
      }, // DB에 올라갈 정보

      rating: 10,
    }
  },
  computed: {
    products() {
        return this.$store.getters.getProducts
    },
    loginUser() {
        return this.$store.getters.getLoginUser
    },
    isLoggedin() {
        return this.$store.getters.getIsLoggedin
    }
  },
  created() {
    this.no = this.$route.params.no
    http.get(`/product/${this.$route.params.no}`)
    .then((response) => {
        this.product = response.data
    })
    .catch((error) => {
        console.log(error)
    })
  },
  methods: {
    movePage(url) {
      this.$router.push(url);
    },      
    checkValid() {
        const valid = this.$refs.form.checkValidity()
        this.commentState = valid
        return valid
    },
    resetModal() {
        this.comment = ''
        this.commentState = null
        this.rating = 10
        this.ratingState = null
    },    
    handleOk(bvModalEvent) {
        // prevent modal from closing
        bvModalEvent.preventDefault()
        // Trigger submit handler
        this.handleSubmit()        
    },
    handleSubmit() {
        if (!this.checkValid()) {
            return
        }

        if (!this.isLoggedin) return

        // set the submitted comment
        this.submittedComment['comment'] = this.comment
        this.submittedComment['productId'] = parseInt(this.$route.params.no)
        this.submittedComment['rating'] = parseInt(this.rating)
        this.submittedComment['userId'] = this.loginUser['user'].id

        //alert(JSON.stringify(this.submittedComment))

        // post('/comment')로 comment 객체 추가한다. (& body comment)
        http.post('/comment', this.submittedComment)
        .then(response => {
            console.log("POST COMMENT STATUS : " + response.data)
            if (response.data) {
                alert("상품평이 추가되었습니다!")
            }
        })
        .catch(error => {
            console.log(error)
        })

        // hide the modal manually
        this.$nextTick(() => {
            this.$bvModal.hide('modal-add-comment')
        })

        // 새로고침
        this.$router.go()
    },
    handleUpdateOk(bvModalEvent) {
        // prevent modal from closing
        bvModalEvent.preventDefault()
        // Trigger submit handler
        this.handleUpdateSubmit()
    },
    handleUpdateSubmit() {
        if (!this.checkValid()) {
            return
        }

        if (!this.isLoggedin) return

        this.submittedUpdateComment['rating'] = parseInt(this.submittedUpdateComment['rating'])
        //alert(JSON.stringify(this.submittedUpdateComment))

        // put.('/rest/comment') 로 comment 객체 수정하기 + body
        http.put('/comment', this.submittedUpdateComment)
        .then(response => {
            console.log("PUT COMMENT STATUS : " + response.data)
            if (response.data) {
                alert("평가가 수정되었습니다!")
            }
        })
        .catch(error => {
            console.log(error)
        })
        //this.movePage('/product-detail/' + this.$route.params.no)

        // hide the modal manually
        this.$nextTick(() => {
            this.$bvModal.hide('modal-update-comment')
        })

        // 새로고침
        this.$router.go()
    },
    updateSubmittedComment(index){ // submittedComment 정보를 업데이트 시킴.
        this.submittedUpdateComment['id'] = parseInt(this.product[index].commentId)
        this.submittedUpdateComment['comment'] = this.product[index].comment
        this.submittedUpdateComment['productId'] = parseInt(this.$route.params.no)
        this.submittedUpdateComment['rating'] = parseInt(this.product[index].rating)
        this.submittedUpdateComment['userId'] = this.product[index].user_id
    },
    deleteComment(index) { // 동작은 잘 되는데 화면이 동기 처리가 안 된다.
        if (confirm("'" + this.product[index].comment + "' 라는 상품평을 삭제하시겠습니까?") == true) {
            let commentId = this.product[index].commentId

            http.delete(`/comment/${commentId}`)
            .then(response => {
                console.log("DELETE Status Code : " + response.status)
                if (response.data) {
                    alert("삭제 완료되었습니다!")
                    // 새로고침
                    this.$router.go()
                }
            })
            .catch(error => {
                console.log(error)
            })
        }
        else {
            console.log("취소되었습니다.")
        }
    },
    getTotalSells: function() {
        console.log("현재 Product 상태 : " + JSON.stringify(this.product))
        let sum = 0
        if (this.product.length > 0) {
            for (let i = 0; i < this.product.length; i++) {
                sum += this.product[i].sells // sells 저장되는 방식이 좀 다른 것 같음
            }
        }
        console.log("Sum : " + sum)
        return sum
    },    
  }
}

</script>

<style>
.product-detail {
  padding: 24px;
}
</style>