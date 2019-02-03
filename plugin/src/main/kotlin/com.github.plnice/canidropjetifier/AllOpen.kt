package com.github.plnice.canidropjetifier

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin

annotation class AllOpen

@AllOpen
abstract class AllOpenTask : DefaultTask()

@AllOpen
interface AllOpenPlugin<T> : Plugin<T>
