package com.github.plnice.canidropjetifier

sealed class BlamedDependency {
    data class FirstLevelDependency(val dependency: Dependency) : BlamedDependency()
    data class ChildDependency(val parents: List<Dependency>, val dependency: Dependency) : BlamedDependency()
}

sealed class Dependency(open val name: String) {
    data class Module(override val name: String) : Dependency(name)
    data class External(override val name: String) : Dependency(name)
}
