package com.damian.viewer

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/github")
class GitHubController(
    private val gitHubService: GitHubService
) {
    @GetMapping("/repositories/{username}")
    fun getRepositories(@PathVariable username: String): List<RepositoryResponse> {
        return gitHubService.getRepositories(username)
    }
}
