/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.farsitel.bazaar.lintrules.rules

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

/**
 * Detector spotting non-final classes that extends BaseFragment and overriding plugins function.
 */
class PluginsOverrideInNonFinalClassDetector : Detector(), UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UClass::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                val evaluator = context.evaluator
                if (node.isFinal) {
                    return
                }

                buildSet {
                    InheritanceUtil.getSuperClasses(node, this, false)
                }.find {
                    it.name == "BaseFragment" ||
                    it.name == "BaseActivity"
                } ?: return

                node.findMethodsByName("plugins", false)
                    .find {
                        // check signature
                        evaluator.isOverride(it, includeInterfaces = false)
                    }?.apply {
                        context.report(
                            ISSUE,
                            node,
                            context.getLocation(this),
                            """Non-final fragment/activity should not override `plugins` function. `override` in child classes instead."""
                        )
                    }
            }
        }
    }

    companion object {
        /**
         * Issue describing the problem and pointing to the detector
         * implementation.
         */
        @JvmField
        val ISSUE: Issue = Issue.create(
            // ID: used in @SuppressLint warnings etc
            id = "PluginsInNonFinalClass",
            // Title -- shown in the IDE's preference dialog, as category headers in the
            // Analysis results window, etc
            briefDescription = "Plugins override In non-final class",
            // Full explanation of the issue; you can use some markdown markup such as
            // `monospace`, *italic*, and **bold**.
            explanation = """
                    This rules checks plugins function override in non-final classes.
                    """, // no need to .trimIndent(), lint does that automatically
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                PluginsOverrideInNonFinalClassDetector::class.java,
                Scope.JAVA_FILE_SCOPE // means Java/Kotlin
            )
        )
    }
}
