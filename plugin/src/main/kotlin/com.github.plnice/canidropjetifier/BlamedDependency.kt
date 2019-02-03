package com.github.plnice.canidropjetifier

sealed class BlamedDependency {
    data class FirstLevelDependency(val name: String) : BlamedDependency()
    data class ChildDependency(val parents: List<String>, val name: String) : BlamedDependency()
}
