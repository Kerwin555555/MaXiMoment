package com.moment.app.datamodel

import com.moment.app.utils.BaseBean

class Result<T>  {
    var isOk: Boolean = false
    var result: Int = 0 //0 成功
    var msg: String? = null
    var message: String? = null
    var data: T? = null

    override fun toString(): String {
        return "Result{" +
                "success=" + isOk +
                ", result=" + result +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}'
    }
}