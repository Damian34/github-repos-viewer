package com.damian.viewer

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "gitHubApi", url = "\${gitHubApi.url}")
interface GitHubApiClient {

    @GetMapping("/users/{username}/repos")
    fun getRepositories(@PathVariable username: String): List<GitHubRepository>

    @GetMapping("/repos/{owner}/{repo}/branches")
    fun getBranches(@PathVariable owner: String, @PathVariable repo: String): List<GitHubBranch>
}
