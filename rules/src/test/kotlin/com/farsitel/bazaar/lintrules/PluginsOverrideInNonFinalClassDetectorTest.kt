package com.farsitel.bazaar.lintrules

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintResult
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import com.farsitel.bazaar.lintrules.rules.PluginsOverrideInNonFinalClassDetector
import org.intellij.lang.annotations.Language
import org.junit.Test

class PluginsOverrideInNonFinalClassDetectorTest {

    @Test
    fun testFragmentError() {
        baseTest(
            """
                abstract class MiddleClass: BaseFragment()

                abstract class TestClass : MiddleClass {

                    override fun plugins(): Array<Any> {
                        return emptyArray()
                    }
                }
            """
        )
            .expect(EXPECTED_ERROR_TEXT)
    }

    @Test
    fun testActivityError() {
        baseTest(
            """
                abstract class BaseActivity {
                    open fun plugins(): Array<Any> = emptyArray()
                }

                abstract class TestClass : BaseActivity {

                    override fun plugins(): Array<Any> {
                        return emptyArray()
                    }
                }
            """
        )
            .expectErrorCount(1)
    }

    @Test
    fun testNoInheritance() {
        baseTest(
            """
                abstract class TestClass {
                
                    override fun plugins(): Array<Any> {
                        return emptyArray()
                    }
                }
            """
        )
            .expectClean()
    }

    @Test
    fun testNoOverride() {
        baseTest(
            """
                abstract class TestClass : BaseFragment
            """
        )
            .expectClean()
    }

    @Test
    fun testFinalClass() {
        baseTest(
            """
                class TestClass : BaseFragment {
                
                    override fun plugins(): Array<Any> {
                        return emptyArray()
                    }
                }
            """
        )
            .expectClean()
    }

    @Test
    fun testNotOverride() {
        baseTest(
            """
                abstract class TestClass : BaseFragment {

                    fun plugins(input: String): Array<Int> {
                        return emptyArray()
                    }
                }
            """
        )
            .expectClean()
    }

    private fun baseTest(@Language("kotlin") inheritedClass: String): TestLintResult {
        return lint()
            .files(kotlin(BASE_FRAGMENT + inheritedClass).indented())
            .issues(PluginsOverrideInNonFinalClassDetector.ISSUE)
            .skipTestModes(TestMode.SUPPRESSIBLE)
            .run()
    }

    companion object {
        @Language("kotlin")
        const val BASE_FRAGMENT = """
                package test.pkg

                abstract class BaseFragment {
                    open fun plugins(): Array<Any> = emptyArray()
                }
        """

        const val EXPECTED_ERROR_TEXT = """
src/test/pkg/BaseFragment.kt:11: Error: non-final fragment/activity should not override plugins function. override in child classes instead. [PluginsInNonFinalClass]
    override fun plugins(): Array<Any> {
    ^
1 errors, 0 warnings"""
    }
}