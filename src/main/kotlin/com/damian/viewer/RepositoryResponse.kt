package com.damian.viewer

data class RepositoryResponse(
    val repositoryName: String,
    val ownerLogin: String,
    val branches: List<GitHubBranch>
)
