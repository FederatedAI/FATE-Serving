<template>
    <ul>
        <li
            v-if="ArrProxy[0] && ArrProxy[0].children.length"
            class="proxy"
            :class="selected === 1 ? 'active' : ''"
            @click="tabNav(1)"
        >serving-proxy</li>
        <li v-else class="proxy disabled">serving-proxy</li>
        <li class="admin">admin</li>
        <li
            v-if="ArrServing[0] && ArrServing[0].children.length"
            class="serving"
            :class="selected === 2 ? 'active' : ''"
            @click="tabNav(2)"
        >serving-server</li>
        <li v-else class="serving disabled">serving-server</li>
        <li class="caret-l caret">
            <p></p>
            <i class="el-icon-caret-bottom"></i>
        </li>
        <li class="caret-r caret">
            <p></p>
            <i class="el-icon-caret-bottom"></i>
        </li>
        <li class="caret-c caret">
            <i class="el-icon-caret-left" />
            <p></p>
            <i class="el-icon-caret-right" />
        </li>
    </ul>
</template>

<script>
export default {
    name: 'overview',
    props: {
        selected: {
            type: Number,
            default() {
                return 1
            }
        },
        ArrProxy: {
            type: Array,
            default() {
                return []
            }
        },
        ArrServing: {
            type: Array,
            default() {
                return []
            }
        }
    },
    methods: {
        tabNav(index) {
            this.$emit('tabNav', index)
        }
    }
}
</script>

<style>
</style>
