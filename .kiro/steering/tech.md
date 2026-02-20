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
- `codex-client`: Codex MCPアダプタ
- `claude-code-client`: Claude Code MCPアダプタ
- `contract-tests`: `McpClient` 共通契約の検証
- `e2e-tests`: 実CLI経由の接続検証

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
- 不変性を優先する（case class, 不変コレクション）。

### Code Quality
- 1公開型 = 1ファイル。
- 曖昧なサフィックス（Manager, Util, Service等）を避ける。
- 既存コードのパターンを分析してから新しいコードを書く。

### Testing
- `sbt test` で `core` / `codex-client` / `claude-code-client` / `contract-tests` / `e2e-tests` を実行。
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
sbt "project e2eTests" test
```
