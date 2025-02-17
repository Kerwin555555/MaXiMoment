package com.moment.app.network

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.moment.app.datamodel.BackendException
import com.moment.app.utils.ProgressIndicatorFragment
import com.moment.app.utils.toast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import kotlin.coroutines.cancellation.CancellationException

@Keep
data class MomentNetError(val code: Int, val message: String?, val throwable: Throwable)
class UserCancelException: CancellationException()
inline fun CoroutineScope.startCoroutine(
    crossinline block: suspend CoroutineScope.() -> Unit,
    crossinline errorAction: (error: MomentNetError) -> Unit,
): Job {
    //map 配置 参数读取从主线程开始
    val coroutineHandler = CoroutineExceptionHandler {_, throwable ->
        launch(Dispatchers.Main) {
            NetErrorHandler.handleNetError(throwable)
            val pair = throwable.format()
            errorAction.invoke(MomentNetError(pair.first, pair.second, throwable))
        }
    }
    return this.launch(coroutineHandler) {
        this.block()
    }
}
inline fun ViewModel.startCoroutine(
    crossinline block: suspend CoroutineScope.() -> Unit,
    crossinline errorAction: (it: MomentNetError) -> Unit,
) : Job{
    val coroutineHandler = CoroutineExceptionHandler {_, throwable ->
        this.viewModelScope.launch(Dispatchers.Main) {
            NetErrorHandler.handleNetError(throwable)
            val pair = throwable.format()
            errorAction.invoke(MomentNetError(pair.first, pair.second, throwable))
        }
    }
    return this.viewModelScope.launch(coroutineHandler) {
        this.block()
    }
}

inline fun AppCompatActivity.startCoroutine(
    crossinline block: suspend CoroutineScope.() -> Unit,
    crossinline errorAction: (it: MomentNetError) -> Unit,
) : Job  {
    val coroutineHandler = CoroutineExceptionHandler {_, throwable ->
        this.lifecycleScope.launch(Dispatchers.Main) {
            NetErrorHandler.handleNetError(throwable)
            val pair = throwable.format()
            errorAction.invoke(MomentNetError(pair.first, pair.second, throwable))
        }
    }
    return this.lifecycleScope.launch(coroutineHandler) {
        this.block()
    }
}

inline fun Fragment.startCoroutine(
    crossinline block: suspend CoroutineScope.() -> Unit,
    crossinline errorAction: (it: MomentNetError) -> Unit,
) : Job {
    val coroutineHandler = CoroutineExceptionHandler {_, throwable ->
        this.viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            NetErrorHandler.handleNetError(throwable)
            val pair = throwable.format()
            errorAction.invoke(MomentNetError(pair.first, pair.second, throwable))
        }
    }
    return this.viewLifecycleOwner.lifecycleScope.launch(coroutineHandler) {
        this.block()
    }
}


fun Throwable.format(): Pair<Int, String?> {
    return when (this) {
        is BackendException -> {
            errorCode to message
        }

        is HttpException -> {
            -1 to "Server error. It will be fixed soon. Please try it later."
        }

        is ConnectException, is IOException -> {
            -1 to "Connect error, please check your network and try later."
        }

        is CancellationException -> {
            -1 to "Cancellation Error Happen, $message"
        }

        else -> {
            -1 to "Connect error, please check your network and try later."
        }
    }
}

fun MomentNetError.toast(withCode: Boolean = false) {
    if (throwable is CancellationException) {
        Log.e("LitNet", "CancellationException ==>", throwable)
        return
    }

    if (withCode) {
        "$code, $message".toast()
    } else {
        message?.toast()
    }
}

sealed class ProgressDialogStatus {
    class ShowProgressDialog(var cancellable: Boolean): ProgressDialogStatus()

    object CancelProgressDialog: ProgressDialogStatus()
}
fun ProgressDialogStatus.refreshProgressDialog(oldProgressDialog: ProgressIndicatorFragment?, context: Context?) : ProgressIndicatorFragment?{
    context?.let {
        when (this) {
            is ProgressDialogStatus.ShowProgressDialog -> {
                oldProgressDialog?.dismissAllowingStateLoss()
                return ProgressIndicatorFragment.show(it).apply {
                    isCancelable = cancellable
                }
            }
            is ProgressDialogStatus.CancelProgressDialog -> {
                oldProgressDialog?.dismissAllowingStateLoss()
                return null
            }
        }
    }
    return null
}

sealed class LoadingStatus<T> {
    class InProgressLoadingStatus<T>(val show: Boolean): LoadingStatus<T>()
    class SuccessLoadingStatus<T>(val result: T): LoadingStatus<T>()
    class FailedLoadingStatus<T>(val error: MomentNetError? = null): LoadingStatus<T>()
    class FailureLoadingStatus<T>(val error: T? = null): LoadingStatus<T>()
}

