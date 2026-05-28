package com.damian.viewer

import tools.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = GitHubBranchDeserializer::class)
data class GitHubBranch(
    val name: String,
    val lastCommitSha: String
)
