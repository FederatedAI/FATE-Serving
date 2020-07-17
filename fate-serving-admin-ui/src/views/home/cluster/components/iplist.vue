<template>
    <div>
        <div
            class="ip-list-li"
            v-for="(item,index) in ipData.children"
            :key="index"
        >
            <span style="cursor: pointer;" @click="selectIP(item,index)" :class="activeip === index ? 'activeip' : ''">{{ item && item.name }}</span>
            <span
                class="iptime"
                :class="activeip === index ? 'activetime' : ''"
            >{{ item.timestamp | dateform}}</span>
        </div>
    </div>
</template>

<script>
export default {
    name: 'iplist',
    props: {
        activeip: {
            type: Number,
            default() {
                return 0
            }
        },
        ipData: {
            type: Object,
            default() {
                return {}
            }
        }
    },
    methods: {
        selectIP(item, index) {
            this.$emit('selectIP', item, index)
        }
    }
}
</script>

<style>
</style>
