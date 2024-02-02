package com.farsitel.bazaar.testapp

open class TestClass : BaseFragment() {
    fun plugins(sL :Int): Array<Int> {
        return emptyArray()
    }

    override fun plugins(): Array<Any> {
        return super.plugins()
    }
}

abstract class BaseFragment {
    open fun plugins(): Array<Any> {
        return emptyArray()
    }
}