(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-030206ac"],{"4f92":function(e,t,a){"use strict";var n=a("52fe"),s=a.n(n);s.a},"52fe":function(e,t,a){},"77b8":function(e,t,a){"use strict";a.r(t);var n=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{staticClass:"home"},[n("el-header",[n("div",{staticClass:"logo"},[n("span",{staticClass:"logo-t",on:{click:e.toHome}},[e._v("FATE Serving")]),e.token?n("div",[n("el-popover",{attrs:{placement:"top",width:"160"},model:{value:e.visible,callback:function(t){e.visible=t},expression:"visible"}},[n("p",{staticClass:"pname",staticStyle:{"text-align":"center"}},[e._v(e._s(e.username))]),n("div",{staticStyle:{"text-align":"center",margin:"0"}},[n("el-button",{staticClass:"exitbut",attrs:{type:"primary",size:"mini"},on:{click:e.exit}},[e._v("Exit "),n("img",{staticClass:"exits",attrs:{src:a("e58a"),alt:"",srcset:""}})])],1),n("span",{staticClass:"name",attrs:{slot:"reference"},slot:"reference"},[e._v(e._s(e.username)+" "),n("i",{staticClass:"el-icon-caret-bottom"})])])],1):e._e()])]),n("el-main",[e.showNav?n("navbar"):e._e(),n("router-view")],1)],1)},s=[],i=a("cebc"),c=(a("7f7f"),function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticClass:"navbar"},[a("ul",[a("li",{class:0===e.selected?"active":"",on:{click:function(t){return e.tabNav(0)}}},[e._v("Cluster")]),a("li",{class:1===e.selected?"active":"",on:{click:function(t){return e.tabNav(1)}}},[e._v("Service")])])])}),o=[],r={name:"navbar",components:{},data:function(){return{selected:0}},watch:{"$route.name":function(e){"service"===e?this.selected=1:"cluster"===e&&(this.selected=0)}},computed:{},created:function(){"service"===this.$route.name?this.selected=1:"cluster"===this.$route.name&&(this.selected=0)},methods:{tabNav:function(e){var t;this.selected=+e,t=1===+e?"/home/service":"/home/cluster",this.$router.push({path:t})}}},l=r,u=(a("d44e"),a("2877")),h=Object(u["a"])(l,c,o,!1,null,null,null),m=h.exports,v=a("2f62"),f={name:"home",components:{navbar:m},data:function(){return{showNav:!1,username:localStorage.name,visible:!1}},watch:{$route:{handler:function(e){"login"===e.name?this.showNav=!1:(this.showNav=!0,this.username=localStorage.name)},immediate:!0}},computed:Object(i["a"])({},Object(v["mapState"])(["name"]),Object(v["mapGetters"])(["token"])),methods:{toHome:function(){this.$router.push({name:"cluster",path:"/home/cluster",query:{}})},exit:function(){var e=this;this.$store.dispatch("Logout").then(function(t){e.$router.push({path:"/home/login"}),e.visible=!1}).catch()}}},p=f,A=(a("4f92"),Object(u["a"])(p,n,s,!1,null,null,null));t["default"]=A.exports},"7f7f":function(e,t,a){var n=a("86cc").f,s=Function.prototype,i=/^\s*function ([^ (]*)/,c="name";c in s||a("9e1e")&&n(s,c,{configurable:!0,get:function(){try{return(""+this).match(i)[1]}catch(e){return""}}})},"870f":function(e,t,a){},d44e:function(e,t,a){"use strict";var n=a("870f"),s=a.n(n);s.a},e58a:function(e,t){e.exports="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABHNCSVQICAgIfAhkiAAAAI1JREFUSEvdlVEOwCAIQ9eb6s28aRc+NOrihEUWo59KeLSg4nJecM5/HQggSattAIZOPA5+A7xVlRXmYj4p2AJQ94pkAhD6/g17oFHQAWQ4BBLr/dUAyd1A1ADjdBWIFyACSCJHDZhdvkphSe4BaJKvBoRsy7Ip2vsmz5ra2LDVa2qpXBN74JepkW2JuQGV/HYZruBfXQAAAABJRU5ErkJggg=="}}]);