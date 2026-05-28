package com.damian.viewer

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ArrayNode
import java.io.IOException
import java.nio.charset.StandardCharsets

class GitHubApiTestData(val mapper: ObjectMapper) {

    companion object {
        const val USERNAME: String = "Damian34"
    }

    fun createRepositoriesUrl(username: String): String = "/users/$username/repos"

    fun createBranchesUrl(owner: String, repo: String): String = "/repos/$owner/$repo/branches"

    fun readUserReposResponse(): String = readResourceFile("github_api/github-user-repos-response.json")

    fun readUserBranchesResponse(): String = readResourceFile("github_api/github-user-branches-response.json")

    fun readUserNotFoundResponse(): String = readResourceFile("github_api/github-user-not-found-response.json")

    fun fetchRepoNames(): List<String> {
        val reposArray = mapper.readTree(readUserReposResponse()) as ArrayNode
        return reposArray.toList().map { it.get("name").stringValue() }
    }

    private fun readResourceFile(resourcePath: String): String {
        try {
            javaClass.classLoader.getResourceAsStream(resourcePath).use { inputStream ->
                requireNotNull(inputStream) { "Resource not found: $resourcePath" }
                return String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to read resource file: $resourcePath", e)
        }
    }
}
