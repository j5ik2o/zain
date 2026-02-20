# Technology Stack

## Architecture

薄いライブラリアーキテクチャ。現状はMCPクライアント基盤（セッション管理・ツール呼び出し）を提供し、将来的にTAKT概念をScala DSL + ストリームコンビネータへ拡張する。

## Core Technologies

- **Language**: Scala 3.8.x
- **Build Tool**: sbt
- **Runtime**: JVM (Java 17+)
- **MCP Client**: [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) (client SDK)

## Key Libraries

- **MCP Java SDK (Client)**: MCPプロトコルのクライアント実装。Codex MCP / Claude Code MCPサーバへの接続に使用。
- **ScalaTest**: ユニットテスト・契約テスト・E2Eテストで使用。

## 現在の実装範囲

- `core`: MCP連携に必要なドメイン型（`McpClient`, `McpSessionCatalog`, `McpCallToolRequest` など）
- `mcp-sdk-client`: MCP Java SDKを使う単一アダプタ実装（`McpSdkClientAdapter`, `McpSdkTransportFactory`）
- `contract-tests`: `McpClient` 共通契約の検証（Codex / Claude Code のProvider差分を同一契約で検証）
- `e2e-tests`: 実CLI経由の接続検証 + SDK直結検証（`McpSdkDirectE2ESpec`）

## 今後の実装範囲（Planned）

- **Pekko-Stream統合**: ワークフロー合成・実行コンビネータの提供
- **TAKT概念モデル拡張**: Piece / Facet / Movement 等のDSL化

## 使用しないもの（現時点の設計判断）

- **codex-sdk (CLIラッパーSDK)**: CLIラップではなくMCPクライアント接続を採用。
- **claude agent SDK (CLIラッパーSDK)**: 同上。
- **YAMLワークフロー定義**: 現時点では採用しない。将来はScala DSL中心で拡張予定。

## Development Standards

### Type Safety
- 接続設定は `McpConnectionConfig` のADT（`Stdio` / `StreamableHttp` / `Sse`）で表現する。
- ツール引数は `McpCallToolArgument` で表現し、`Any` を使わない。
- 不変性を優先する（case class, 不変コレクション）。ただしセッションライフサイクル管理では同期付き可変Mapを局所的に使用する。

### Code Quality
- 1公開型 = 1ファイル。
- 曖昧なサフィックス（Manager, Util, Service等）を避ける。
- 既存コードのパターンを分析してから新しいコードを書く。

### Testing
- `sbt test` で `core` / `mcp-sdk-client` / `contract-tests` / `e2e-tests` を実行。
- `e2e-tests` は外部CLIがない環境では `assume` でスキップする。

## Development Environment

### Required Tools
- Java 17+
- sbt
- Scala 3.8.x（sbt経由で自動取得）

### Common Commands
```bash
sbt compile
sbt test
sbt "project core" compile
sbt "project mcpSdkClient" compile
sbt "project contractTests" test
sbt "project e2eTests" test
```

## Key Technical Decisions

- Providerごとに実装モジュールを分けず、`McpProvider` と `McpSdkClientAdapter` の組み合わせでCodex / Claude Codeを切り替える。
- 接続方式は `McpConnectionConfig`（`Stdio` / `StreamableHttp` / `Sse`）ADTで表現し、呼び出し側の分岐を局所化する。
- MCP SDKのレスポンス揺らぎを吸収するため、`McpSdkTransportFactory` でlenientなJSONマッパーを構築する。
- セッションの生存状態は `McpSessionCatalog` で管理し、`SessionNotFound` / `SessionClosed` 契約を明確化する。
- セッション管理の実装は局所的な同期付き可変状態（`McpSessionCatalog`, `McpSdkClientAdapter`）を許容し、公開APIは `McpResult` と不変値で安定化する。

## Steering Metadata

- updated_at: 2026-02-20T17:57:41Z
- sync_reason: `codex-client` / `claude-code-client` 前提の記述を `mcp-sdk-client` 集約実装へ同期
