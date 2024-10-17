package com.moment.app.entities

import com.moment.app.utils.BaseBean

class Result<T> : BaseBean() {
    var isOk: Boolean = false
    var result: Int = 0 //0 成功
    var msg: String? = null
    var message: String? = null
    private var data: T? = null

    fun getData(): T? {
        return data
    }

    fun setData(data: T) {
        this.data = data
    }

    override fun toString(): String {
        return "Result{" +
                "success=" + isOk +
                ", result=" + result +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}'
    }
}