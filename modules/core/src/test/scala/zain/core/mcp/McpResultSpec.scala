package zain.core.mcp

import org.scalatest.funsuite.AnyFunSuite

final class McpResultSpec extends AnyFunSuite:
  test("should hold value in success result"):
    val result: McpResult[String] = McpResult.Success("done")
    assert(result == McpResult.Success("done"))

  test("should hold error in failure result"):
    val result: McpResult[String] = McpResult.Failure(McpError.ConnectionUnavailable)
    assert(result == McpResult.Failure(McpError.ConnectionUnavailable))

  test("should expose standardized error categories"):
    val actual = McpError.values.toSet
    val expected = Set(
      McpError.ConnectionUnavailable,
      McpError.SessionClosed,
      McpError.SessionNotFound,
      McpError.InvalidRequest
    )
    assert(actual == expected)
