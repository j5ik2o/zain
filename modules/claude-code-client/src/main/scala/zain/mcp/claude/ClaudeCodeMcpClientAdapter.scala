package zain.mcp.claude

import java.util.UUID

import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpClient
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpError
import zain.core.mcp.McpProvider
import zain.core.mcp.McpResult
import zain.core.mcp.McpSessionCatalog
import zain.core.mcp.McpSessionId
import zain.core.mcp.McpToolInfo

final class ClaudeCodeMcpClientAdapter(factory: ClaudeCodeTransportFactory) extends McpClient:
  private val sessionCatalog = new McpSessionCatalog
  private var sessionHandles: Map[McpSessionId, ClaudeCodeClientHandle] = Map.empty

  override def openSession(config: McpConnectionConfig): McpResult[McpSessionId] =
    factory.create(config) match
      case Left(error) => McpResult.Failure(error)
      case Right(handle) =>
        val sessionId = McpSessionId(UUID.randomUUID().toString)
        sessionCatalog.register(sessionId, McpProvider.ClaudeCode, config)
        putHandle(sessionId, handle)
        McpResult.Success(sessionId)

  override def listTools(sessionId: McpSessionId): McpResult[Seq[McpToolInfo]] =
    withOpenedSession(sessionId): handle =>
      handle.listTools() match
        case Left(error)  => McpResult.Failure(error)
        case Right(tools) => McpResult.Success(tools)

  override def callTool(sessionId: McpSessionId, request: McpCallToolRequest): McpResult[McpCallToolResult] =
    if request.name.isEmpty then McpResult.Failure(McpError.InvalidRequest)
    else
      withOpenedSession(sessionId): handle =>
        handle.callTool(request) match
          case Left(error)   => McpResult.Failure(error)
          case Right(result) => McpResult.Success(result)

  override def ping(sessionId: McpSessionId): McpResult[Unit] =
    withOpenedSession(sessionId): handle =>
      handle.ping() match
        case Left(error) => McpResult.Failure(error)
        case Right(())   => McpResult.Success(())

  override def closeSession(sessionId: McpSessionId): McpResult[Unit] =
    sessionCatalog.resolveOpened(sessionId) match
      case Left(error) => McpResult.Failure(error)
      case Right(_) =>
        getHandle(sessionId) match
          case None => McpResult.Failure(McpError.SessionNotFound)
          case Some(handle) =>
            handle.close() match
              case Left(error) => McpResult.Failure(error)
              case Right(()) =>
                sessionCatalog.markClosed(sessionId) match
                  case Left(error) => McpResult.Failure(error)
                  case Right(()) =>
                    removeHandle(sessionId)
                    McpResult.Success(())

  private def withOpenedSession[A](sessionId: McpSessionId)(
      f: ClaudeCodeClientHandle => McpResult[A]
  ): McpResult[A] =
    sessionCatalog.resolveOpened(sessionId) match
      case Left(error) => McpResult.Failure(error)
      case Right(_) =>
        getHandle(sessionId) match
          case None         => McpResult.Failure(McpError.SessionNotFound)
          case Some(handle) => f(handle)

  private def putHandle(sessionId: McpSessionId, handle: ClaudeCodeClientHandle): Unit =
    this.synchronized:
      sessionHandles = sessionHandles.updated(sessionId, handle)

  private def getHandle(sessionId: McpSessionId): Option[ClaudeCodeClientHandle] =
    this.synchronized:
      sessionHandles.get(sessionId)

  private def removeHandle(sessionId: McpSessionId): Unit =
    this.synchronized:
      sessionHandles = sessionHandles.removed(sessionId)
