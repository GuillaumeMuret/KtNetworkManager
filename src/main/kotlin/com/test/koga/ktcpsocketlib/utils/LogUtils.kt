package com.test.koga.ktcpsocketlib.utils

object LogUtils {
    const val DEBUG_TAG = "TCP_LIB"

    /**
     * Process called to display a debug message on the logcat
     * @param message : the message
     */
    fun d(aClass: Class<*>?, message: String?) {
        if (aClass != null) {
            Log.d(DEBUG_TAG + "-" + aClass.simpleName, message)
        } else {
            Log.d(DEBUG_TAG, message)
        }
    }

    /**
     * Process called to display an error message on the logcat
     * @param tag : the tag of the message
     * @param message : the message
     */
    fun e(tag: String?, message: String?) {
        Log.e(tag, message)
    }

    /**
     * Process called to display an error message on the logcat with throwable exception
     * @param tag : the tag of the message
     * @param message : the message
     * @param throwable : the exception to display
     */
    fun e(tag: String?, message: String?, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }

    object Log {
        fun e(tag: String?, message: String?, throwable: Throwable?) {
            println("E $tag -> $message -> $throwable")
        }
        fun e(tag: String?, message: String?) {
            println("E $tag -> $message")
        }
        fun d(tag: String?, message: String?) {
            println("D $tag -> $message")
        }
    }
}