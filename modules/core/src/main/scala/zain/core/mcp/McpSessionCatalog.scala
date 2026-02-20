package zain.core.mcp

final class McpSessionCatalog:
  private var entries: Map[McpSessionId, McpSessionCatalog.McpSessionEntry] = Map.empty

  def register(
      sessionId: McpSessionId,
      provider: McpProvider,
      config: McpConnectionConfig
  ): McpSessionCatalog.McpSessionEntry =
    val entry = McpSessionCatalog.McpSessionEntry(
      sessionId = sessionId,
      provider = provider,
      connectionConfig = config,
      state = McpSessionState.Opened
    )
    this.synchronized:
      entries = entries.updated(sessionId, entry)
    entry

  def resolve(sessionId: McpSessionId): Option[McpSessionCatalog.McpSessionEntry] =
    this.synchronized:
      entries.get(sessionId)

  def resolveOpened(sessionId: McpSessionId): Either[McpError, McpSessionCatalog.McpSessionEntry] =
    this.synchronized:
      entries.get(sessionId) match
        case Some(entry) if entry.state == McpSessionState.Opened => Right(entry)
        case Some(_)                                               => Left(McpError.SessionClosed)
        case None                                                  => Left(McpError.SessionNotFound)

  def markClosed(sessionId: McpSessionId): Either[McpError, Unit] =
    this.synchronized:
      entries.get(sessionId) match
        case None => Left(McpError.SessionNotFound)
        case Some(entry) if entry.state == McpSessionState.Closed =>
          Left(McpError.SessionClosed)
        case Some(entry) =>
          entries = entries.updated(sessionId, entry.copy(state = McpSessionState.Closed))
          Right(())

object McpSessionCatalog:
  final case class McpSessionEntry(
      sessionId: McpSessionId,
      provider: McpProvider,
      connectionConfig: McpConnectionConfig,
      state: McpSessionState
  )
