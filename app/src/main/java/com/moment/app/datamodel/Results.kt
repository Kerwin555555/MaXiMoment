package com.moment.app.datamodel

import java.io.IOException

class Results<T>  {
    var code: Int = 0 //0 成功
    var msg: String? = null
    var message: String? = null
    var data: T? = null

    override fun toString(): String {
        return "Result{" +
                "success="  +
                ", result=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}'
    }

    fun success(): Boolean {
        return code == 0
    }
}

class BackendException : IOException {
    var errorCode: Int
        private set

    var response: String? = null
        private set

    constructor(code: Int, msg: String?) : super(msg) {
        this.errorCode = code
    }

    constructor(code: Int, msg: String?, response: String?) : super(msg) {
        this.errorCode = code
        this.response = response
    }
}

