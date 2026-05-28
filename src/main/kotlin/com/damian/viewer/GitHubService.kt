package com.damian.viewer

import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GitHubService(
    private val gitHubApiClient: GitHubApiClient
) {
    private val log = LoggerFactory.getLogger(GitHubService::class.java)

    fun getRepositories(username: String): List<RepositoryResponse> {
        log.info("Starting search repositories for user: {}", username)
        try {
            return gitHubApiClient.getRepositories(username)
                .filter { !it.fork }
                .map { mapRepository(it) }
        } catch (e: FeignException.NotFound) {
            log.error("Not Found $username on GitHub", e)
            throw UserNotFoundException("User $username not found")
        }
    }

    private fun mapRepository(repository: GitHubRepository): RepositoryResponse {
        val name = repository.name
        val owner = repository.owner.login
        val branches = fetchBranches(owner, name)
        return RepositoryResponse(name, owner, branches)
    }

    private fun fetchBranches(owner: String, name: String): List<GitHubBranch> {
        log.info("Fetching branches for repository: {} owned by: {}", name, owner)
        return gitHubApiClient.getBranches(owner, name)
    }
}
