<template>
  <div class="homepage">
    <div class="homepage-m">
         <el-row v-loading="loading"  >
      <section>
        <figure class="market-data-cen" style="margin-top: 0px;">
            <div >
            <div class="card-add cardhover" @click="toRegister">
                <el-card class="card" shadow="never">
                    <div class="card-bor"></div>
                    <div class="card-top"><i class="el-icon-plus"></i> </div>
                </el-card>
                </div>
            </div>
        </figure>
        <figure v-for="(item,index) in cardData" :key="index" class="market-data-cen">
          <div class="cardhover">
                <el-card class="card" shadow="never" >
                <div class="card-bor"></div>
                <div class="card-top" :class="{'allDel': item.isAllDel}" >
                    <div :class="{'delcolor': item.isAllDel}">
                       <div class="card-top-t" >
                          <div>
                            <div class="card-top-tit carddel card-top-l" >Federated Organization</div>
                            <div class="card-top-con card-top-omit" style="width:160px">{{ item.federatedOrganization }}</div>
                          </div>
                          <div>
                            <div class="card-top-tit carddel card-top-r" >Size</div>
                            <div class="card-top-con">{{ item.size }}</div>
                          </div>
                       </div>
                       <div class="card-top-b">
                          <div class="card-top-it">
                            <div class="card-top-tit carddel card-top-l">Institution</div>
                            <div class="card-top-con card-top-it">{{ item.institutions }}</div>
                          </div>
                          <div>
                            <div class="card-top-tit carddel card-top-l">Creation Time</div>
                            <div class="card-top-con">{{item.createTime | dateFormat }}</div>
                          </div>
                       </div>
                    </div>
                </div>
                <ul>
                    <li class="card-li"  v-for="(siteitem,index) in item.siteList" :key="index" :class="{'delcolor':siteitem.status && siteitem.status.code == 3}" @click="toSietInfo(item.federatedId, siteitem.partyId)">
                        <div class="card-li-main">
                        <div class="card-top-t">
                            <div>
                                <div class="card-top-tit card-top-l card-top-color">Site Name</div>
                                <div class="card-top-con card-top-ma card-top-omit">{{ siteitem.siteName }}</div>
                            </div>
                            <div>
                                <div class="card-top-tit card-top-color" >status</div>
                                <div class="card-top-con" style="margin-top: 15px;color:#217AD9 !important">{{ siteitem.status.desc }}</div>
                            </div>
                        </div>
                        <div class="card-top-b">
                            <div>
                                <div class="card-top-tit card-top-l card-top-color">Role</div>
                                <div class="card-top-con card-top-ma">{{ siteitem.role.desc }}</div>
                            </div>
                            <div style="margin-left: 10px; width:60px">
                                <div class="card-top-tit card-top-l card-top-color" >Party ID</div>
                                <div class="card-top-con card-top-ma">{{ siteitem.partyId }}</div>
                            </div>
                            <div style="margin-left: 10px;">
                                <div class="card-top-tit card-top-l card-top-color">Activation Time</div>
                                <div class="card-top-con card-top-ma">{{ siteitem.acativationTime | dateFormat }}</div>
                            </div>
                        </div>
                        </div>
                        <div class="bor"></div>
                    </li>
                </ul>
            </el-card>
          </div>
      </figure>
      </section>
    </el-row>
    </div>
  </div>
</template>
<script>
import { siteList } from '@/api/welcomepage'
import moment from 'moment'
export default {
    name: 'Homepage',

    components: {
    },
    filters: {
        dateFormat(vaule) {
            return moment(vaule).format('YYYY-MM-DD HH:mm:ss')
        }
    },
    data() {
        return {
            isAllDel: true,
            index: 0,
            loading: false,
            cardData: []
        }
    },
    computed: {
    },
    watch: {

    },
    created() {

    },
    mounted() {
        // this.getSiteList()
    },
    methods: {
        getSiteList() {
            siteList().then(res => {
                this.cardData = res.data
                this.cardData.forEach(item => {
                    item.isAllDel = true
                    item.siteList.forEach(siteitem => {
                        // siteitem.status.code = 3
                        if (siteitem.status.code !== 3) {
                            item.isAllDel = false
                        }
                    })
                })
            })
        },
        toRegister() {
            this.$router.push({
                name: 'register',
                path: '/home/register'
                // query: {}

            })
        },
        toSietInfo(federatedId, partyId) {
            this.$router.push({
                name: 'siteinfo',
                path: '/siteinfo/index',
                query: { federatedId: federatedId, partyId: partyId }
            })
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" >
  .homepage{
    min-height:calc(100vh - 65px);
    background-color:#F5F8FA;
    opacity:1;
    margin-left: -65px;
    // overflow: auto;
    position: relative;
    .homepage-m {
        width: 100%;
         position: absolute;
        overflow-y: auto;
    background-color:#F5F8FA;

    }
    .el-row {

        padding: 35px calc(8% - 8px) 20px 8%;
    }
    .market-data-cen {
        margin-left: 20px;
        width: 95%;
        height: 100%;
        position: relative;
        .cardhover {
            // width: calc(100% + 56px);
            // margin: 0 12px;
            transition: all 0.3s;

        }
        .cardhover:hover{
            box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
        }
         .delcolor {
                .card-top-con,.carddel {
                    color: #848C99 !important;
                }
            }
        .allDel {
            background: #f1f2f6 !important;
        }
    }
    .el-card {
      border-radius: 0px !important;
    //   background: linear-gradient(-45deg, transparent 25px, #fff 0);
    //   background-repeat: no-repeat;
      border: none;
      .el-card__body {
          padding: 0;
      }

      .el-card__body:hover {
          box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
      }
    }

      .card-add {
         .card-top {
             background: #E6EBF0 !important;
             position: relative;
            //  background: linear-gradient(-45deg, transparent 25px, #e8e9f2 0) !important;
            //  background-repeat: no-repeat;
             i{
                 position: absolute;
                 left: 50%;
                 top: 50%;
                margin-left: -35px;
                margin-top: -35px;
                 font-size: 70px;
                 color: #4AA2FF;
             }
         }
     }
    .card{
      position: relative;
      min-height: 180px;
      box-sizing: border-box;
      cursor: pointer;
      .card-bor {
          top: 0;
          height: 6px;
          background: #2772DB;
      }
      .card-top {
          height: 180px;
          background: #edf6ff;
          padding: 24px;
          box-sizing: border-box;
      }
       .card-top-tit {
        font-size:14px;
        font-family:Product Sans;
        font-weight:400;
        line-height:17px;
        color:rgba(0,90,186,1);
        opacity:1;
       }
        .card-top-t,.card-top-b {
            display: flex;
            justify-content : space-between;
        }
        .card-top-b {
            margin-top: 25px;
        }
        .card-top-r {
            float: right;
        }
        .card-top-l {
            float: left;
        }
        .card-top-omit {
            max-width: 140px;
            white-space: nowrap; //文本强制不换行；
            text-overflow:ellipsis; //文本溢出显示省略号；
            overflow:hidden; //溢出的部分隐藏；
        }
        .card-top-it {
             width: 120px;
            white-space: nowrap; //文本强制不换行；
            text-overflow:ellipsis; //文本溢出显示省略号；
            overflow:hidden; //溢出的部分隐藏；
        }
        .card-top-con {
            margin-top: 30px;
        font-size:18px;
        font-family:Product Sans;
        font-weight:bold;
        line-height:20px;
        color:rgba(0,90,186,1);
        opacity:1;
        }
        .card-top-color {
            color: #B8BFCC;
        }
          .card-top-ma {
            color: #4E5766;
        }
      ul {
          width: 100%;
          margin: 0;
          padding: 0;
      }
      li {
          list-style-type:none;
          padding: 24px 24px 0 24px;
          box-sizing: border-box
      }
      .card-li {
            width: 100%;
            min-height: 170px;
            box-sizing: border-box;
      }
      .card-li-main {
        padding-bottom: 27px;
        border-bottom: 2px solid rgba(230,235,240,1);
      }
      li:last-child .card-li-main {
          border-bottom: none;
      }
      li:last-child {
          min-height: 175px;
      }
    }
    section{
        min-height: 500px;
        width: 1670px;
        padding-left: 40px;
        // margin:0 auto;
        column-count: 4;
        column-gap:0;
        overflow: auto;
        }
        figure{
            break-inside: avoid;
            }
            .tablelist-box {
                overflow-y: auto;
            }
  }
</style>
