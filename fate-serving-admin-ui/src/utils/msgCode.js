
// 10600000  fail with unprocessable exception 失败，出现无法处理的异常
// 10600001  Internal business exception 内部业务异常
// 10600002  Illegal param 非法参数
// 10600003  Decrypt content fail 解密内容失败
// 10600004  Request wechat service fail 请求微信服务失败
// 10600005  User not existed 用户不存在
// 10600006  Password not match 密码不匹配
// 10600007  User did not set a password 用户未设置密码
// 10600008  Current ip request too frequently 当前ip请求太频繁
// 10600009  Current authentication has expired 当前身份验证已过期
// 10600010  Current authentication has been used 当前身份验证已经认证
// 10600011  Authenticate email too frequently 验证电子邮件太频繁
// 10600012  Authenticate sms code too frequently 验证短信代码太频繁
// 10600013  Nickname is existed 名字已经存在
// 10600014  Tel is registered 电话号码已经存在
// 10600015  WeChat user did not associate tel 微信用户未关联电话
// 10600016  IdCard is not match with face identify result 身份证与人脸识别结果不匹配
// 10600017  Name is not match with face identify result 名称与人脸识别结果不匹配
// 10600018  Not exist this authentication 此认证不存在
// 10600019  Illegal authentication 非法身份验证
// 10600020  User not match as expected 用户与预期不匹配
// 10600021  Current authentication has not been authenticated 当前身份验证尚未进行身份验证
// 10600022  Authentication code too long 验证码太长
// 10600023  IdCard has been authenticated 身份证已经过验证
// 10600024  Current user are not the expect user to authenticate 当前用户不是要进行身份验证的预期用户
// 10600025  Authenticate sms code too frequently 验证短信代码太频繁

const msgCode = [
    { code: '10600005', msg: '该用户未注册过FDN账号！' },
    { code: '10600006', msg: '账号或密码错误！' },
    { code: '10600007', msg: '未设置密码' },
    { code: '10600009', msg: '当前身份验证已过期' },
    { code: '10600010', msg: '当前身份验证已认证' },
    { code: '10600013', msg: '昵称已经存在' },
    { code: '10600014', msg: '号码已经被注册过' },
    { code: '10600004', msg: '请求微信服务失败,请重试！' }
]
export default msgCode
