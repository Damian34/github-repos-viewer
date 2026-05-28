package com.damian.viewer

data class GitHubRepository(
    val name: String,
    val fork: Boolean,
    val owner: Owner
) {
    data class Owner(val login: String)
}