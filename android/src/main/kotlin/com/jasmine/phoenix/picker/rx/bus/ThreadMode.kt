package com.jasmine.phoenix.picker.rx.bus

enum class ThreadMode {

    /**
     * current thread
     */
    CURRENT_THREAD,

    /**
     * android main thread
     */
    MAIN,

    /**
     * new thread
     */
    NEW_THREAD
}
