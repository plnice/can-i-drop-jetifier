package com.github.plnice.canidropjetifier

import org.gradle.api.Project

import com.github.plnice.canidropjetifier.BlamedDependency.ChildDependency
import com.github.plnice.canidropjetifier.BlamedDependency.FirstLevelDependency
import org.gradle.internal.logging.text.StreamingStyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutput
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import kotlin.math.max

interface CanIDropJetifierReporter {
    val verbose: Boolean
    fun report(project: Project, blamedDependencies: List<BlamedDependency>)
}

class TextCanIDropJetifierReporter(override val verbose: Boolean) : CanIDropJetifierReporter {

    private val textOutput = StreamingStyledTextOutput(BufferedWriter(OutputStreamWriter(System.out)))

    override fun report(project: Project, blamedDependencies: List<BlamedDependency>) {
        with(textOutput) {
            style(StyledTextOutput.Style.FailureHeader)

            println("=".repeat(max(40, 8 + project.name.length)))
            println("Project ${project.name}")
            println("=".repeat(max(40, 8 + project.name.length)))
            println("")

            when (blamedDependencies.size) {
                0 -> {
                    println("No dependencies on old artifacts! Safe to drop Jetifier.")
                    println("")
                }
                else -> {
                    println("Cannot drop Jetifier due to following dependencies:")
                    println("")

                    blamedDependencies
                        .filterIsInstance<FirstLevelDependency>()
                        .forEach { it.print() }

                    blamedDependencies
                        .filterIsInstance<ChildDependency>()
                        .groupBy { it.parents.first() }
                        .forEach { it.print() }
                }
            }
        }
    }

    private fun FirstLevelDependency.print() {
        println("* $name")
        println("")
    }

    private fun Map.Entry<String, List<ChildDependency>>.print() {
        val (parent, dependencies) = this
        println("* $parent")
        if (verbose) {
            dependencies.forEach {
                val parentsWithoutFirst = it.parents.subList(1, it.parents.size)
                parentsWithoutFirst.forEachIndexed { index: Int, parent: String ->
                    println(" ".repeat(index + 2) + "\\-- $parent")
                }
                println(" ".repeat(parentsWithoutFirst.size + 2) + "\\-- ${it.name}")
            }
        }
        println("")
    }
}
