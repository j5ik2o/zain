package zain.core.mcp

import org.scalatest.funsuite.AnyFunSuite

final class McpSessionCatalogSpec extends AnyFunSuite:
  private val sampleConfig = McpConnectionConfig.StreamableHttp("http://localhost:3000")

  test("should resolve opened session entry when session is registered"):
    val catalog = new McpSessionCatalog
    val sessionId = McpSessionId("session-1")
    val registered = catalog.register(sessionId, McpProvider.Codex, sampleConfig)

    val actual = catalog.resolveOpened(sessionId)

    assert(actual == Right(registered))

  test("should return SessionNotFound when session is not registered"):
    val catalog = new McpSessionCatalog

    val actual = catalog.resolveOpened(McpSessionId("missing-session"))

    assert(actual == Left(McpError.SessionNotFound))

  test("should return SessionClosed when resolving opened state after close"):
    val catalog = new McpSessionCatalog
    val sessionId = McpSessionId("session-closed")
    catalog.register(sessionId, McpProvider.ClaudeCode, sampleConfig)
    val closeResult = catalog.markClosed(sessionId)

    val actual = catalog.resolveOpened(sessionId)

    assert(closeResult == Right(()))
    assert(actual == Left(McpError.SessionClosed))

  test("should return SessionClosed when closing already closed session"):
    val catalog = new McpSessionCatalog
    val sessionId = McpSessionId("session-already-closed")
    catalog.register(sessionId, McpProvider.Codex, sampleConfig)
    catalog.markClosed(sessionId)

    val actual = catalog.markClosed(sessionId)

    assert(actual == Left(McpError.SessionClosed))

  test("should return SessionNotFound when closing unknown session"):
    val catalog = new McpSessionCatalog

    val actual = catalog.markClosed(McpSessionId("unknown-session"))

    assert(actual == Left(McpError.SessionNotFound))
