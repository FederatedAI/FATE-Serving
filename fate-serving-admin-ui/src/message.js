import { Message } from 'element-ui'

const showMessage = Symbol('showMessage')

class GetMessage {
    success(options, single = true) {
        this[showMessage]('success', options, single)
    }
    warning(options, single = true) {
        this[showMessage]('warning', options, single)
    }
    info(options, single = true) {
        this[showMessage]('info', options, single)
    }
    error(options, single = true) {
        this[showMessage]('error', options, single)
    }

    [showMessage](type, options, single) {
        let nowMessage = document.querySelector('.el-message')
        let nowContent = document.querySelectorAll('.el-message__content')
        let nowTextArr = []
        nowContent && nowContent.forEach(item => {
            nowTextArr.push(item.innerHTML)
        })
        let msg = (options && options.typeof === 'string') ? options : options.message
        console.log(nowTextArr, 'nowTextArr')
        if (single) {
            if (nowMessage !== null || !nowTextArr.includes(msg)) {
                Message[type](options)
            }
        } else {
            Message[type](options)
        }
    }
}

const message = new GetMessage()

export default message
