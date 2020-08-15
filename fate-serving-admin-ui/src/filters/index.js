import moment from 'moment'

export const dateform = (value) => {
    return value ? moment(value).format('YYYY-MM-DD HH:mm:ss') : '--'
}
