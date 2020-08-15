<template>
    <div class="content">
        <navbar />
        <router-view />
    </div>
</template>

<script>
import navbar from './navbar'

export default {
    name: 'content',
    components: {
        navbar
    },
    data() {
        return {
        }
    },
    watch: {},
    computed: {},
    methods: {
    }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.content {
    background-color: #F5F8FA
}
</style>
