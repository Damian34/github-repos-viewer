package com.damian.viewer

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.deser.std.StdDeserializer

class GitHubBranchDeserializer: StdDeserializer<GitHubBranch>(GitHubBranch::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GitHubBranch {
        val node: JsonNode = p.readValueAsTree()
        val name = node.get("name").stringValue()
        val sha = node.get("commit")?.get("sha")?.stringValue() ?: ""
        return GitHubBranch(name, lastCommitSha = sha)
    }
}