package com.damian.viewer

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubViewerTest {

    companion object {
        private lateinit var wireMockServer: WireMockServer

        @JvmStatic
        @BeforeAll
        fun setUpAll() {
            wireMockServer = WireMockServer(0)
            wireMockServer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("gitHubApi.url") { "http://localhost:${wireMockServer.port()}" }
        }

        @JvmStatic
        @AfterAll
        fun tearDownAll() {
            wireMockServer.stop()
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    private val apiData = GitHubApiTestData(ObjectMapper())

    @AfterEach
    fun tearDown() {
        wireMockServer.resetAll()
    }

    @Test
    fun shouldFetchUserRepositoriesAndFilterOutForks() {
        // given
        val username = GitHubApiTestData.USERNAME
        val repoNames = apiData.fetchRepoNames()

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(apiData.createRepositoriesUrl(username)))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(apiData.readUserReposResponse())))
        repoNames.forEach { repoName ->
            wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(apiData.createBranchesUrl(username, repoName)))
                .willReturn(WireMock.aResponse()
                    .withFixedDelay(1000)
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(apiData.readUserBranchesResponse())))
        }

        // when
        val start = System.currentTimeMillis()
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/api/github/repositories/{username}", username))
            .andReturn()
            .response

        // then
        Assertions.assertEquals(HttpStatus.OK.value(), response.status)
        val time = System.currentTimeMillis() - start
        Assertions.assertTrue(time in 2000..3000, "Expected time between 2000-3000ms but was ${time}ms")
        wireMockServer.verify(repoNames.size - 1, WireMock.getRequestedFor(
            WireMock.urlMatching(".*/branches")
        ))

        val repositories = mapper.readValue(response.contentAsString, object : TypeReference<List<RepositoryResponse>>() {})
        Assertions.assertEquals(repoNames.size-1, repositories.size, "shouldn't contain any fork repository")
        repositories.forEach { repository ->
            val branches = repository.branches
            Assertions.assertFalse(branches.isEmpty(), "branches shouldn't be empty")
        }
    }

    @Test
    fun shouldThrowWhenUserNotFound() {
        // given
        val username = "InvalidUser"
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(apiData.createRepositoriesUrl(username)))
            .willReturn(WireMock.aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(apiData.readUserNotFoundResponse())))

        // when
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/api/github/repositories/{username}", username))
            .andReturn()
            .response

        // then
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.status)
        val errorResponse = mapper.readValue(response.contentAsString, GlobalExceptionHandler.ErrorResponse::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.status)
        Assertions.assertNotNull(errorResponse.message)
        Assertions.assertTrue(errorResponse.message!!.contains(username))
    }
}
