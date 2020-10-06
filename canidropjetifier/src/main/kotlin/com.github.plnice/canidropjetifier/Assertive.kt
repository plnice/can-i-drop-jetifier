package com.github.plnice.canidropjetifier

import org.gradle.api.GradleException


interface IAssertive {
    /**
     * Take note of the bad dependencies.
     * @param dependencies a list of bad deps (can be empty)
     */
    fun note(dependencies: List<BlamedDependency>)

    /**
     * Assert that the dependencies are OK to continue the build.
     *
     * Concrete implementation shall inspect the list of dependencies collected through
     * [IAssertive.note] and make the decision. In case of NO-GO it throws [GradleException].
     *
     * @throws GradleException - in case the deps evaluated NO-GO
     */
    fun assert()
}

class IgnoreAll : IAssertive {
    override fun note(dependencies: List<BlamedDependency>) { /* Do nothing */ }
    override fun assert() { /* Do nothing */ }
}

class StrictAssert : IAssertive {
    private val blamed = ArrayList<BlamedDependency>()

    override fun note(dependencies: List<BlamedDependency>) {
        blamed.addAll(dependencies)
    }

    override fun assert() {
        if (blamed.isNotEmpty()) {
            throw GradleException("STOPPING the build: found dependencies on pre-AndroidX artifacts, Jetifier can NOT be dropped.\n" +
                    "Scroll up the build log to see the blamed dependencies pretty-printed.\n" +
                    "You can set `verbose = true` in this plugin's configuration to see more details.\n" +
                    "Blamed dependencies:\n$blamed")
        }
    }
}
