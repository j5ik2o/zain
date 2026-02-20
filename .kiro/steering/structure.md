# Project Structure

## Organization Philosophy

マルチモジュール構成。コアのドメインモデル、MCP接続モジュール、契約テスト/実サーバE2Eを分離し、依存方向を明確にする。

## Directory Patterns

### Root
**Location**: `/`  
**Purpose**: sbtマルチプロジェクトのルート。`build.sbt` でサブモジュールを集約する。

### Core Domain Model
**Location**: `/modules/core/`  
**Purpose**: MCP連携の中核型（セッション、リクエスト/レスポンス、エラー）をScala型として定義する。外部依存を最小限にする。

### Codex MCP Client
**Location**: `/modules/codex-client/`  
**Purpose**: Codex MCPサーバへのMCPクライアント接続を提供する。MCP Java SDK のクライアントライブラリを使用し、ツール呼び出し・セッション管理をラップする。

### Claude Code MCP Client
**Location**: `/modules/claude-code-client/`  
**Purpose**: Claude Code MCPサーバへのMCPクライアント接続を提供する。MCP Java SDK のクライアントライブラリを使用し、ツール呼び出し・セッション管理をラップする。

### Contract Tests
**Location**: `/modules/contract-tests/`  
**Purpose**: `McpClient` の共通契約（open/list/call/ping/closeの振る舞い）をプロバイダ実装横断で検証する。

### E2E Tests
**Location**: `/modules/e2e-tests/`  
**Purpose**: 実際の `codex` / `claude` CLI 経由でMCPサーバ接続を検証する統合テストを提供する。

### References
**Location**: `/references/takt/`  
**Purpose**: TAKTのソースコード・ドキュメント・ビルトイン定義を参照資料として保持する（git submodule）。

## Naming Conventions

- **Modules**: `kebab-case`（例: `codex-client`, `claude-code-client`, `contract-tests`）
- **Packages**: `zain.core`, `zain.mcp.codex`, `zain.mcp.claude`
- **Types**: Scala標準の `PascalCase`
- **Functions/Methods**: Scala標準の `camelCase`
- **Files**: 1公開型 = 1ファイル。ファイル名は型名を反映

## Module Dependencies

### Current
```
modules/codex-client
  └── modules/core

modules/claude-code-client
  └── modules/core

modules/contract-tests
  ├── modules/core
  ├── modules/codex-client
  └── modules/claude-code-client

modules/e2e-tests
  ├── modules/core
  ├── modules/codex-client
  └── modules/claude-code-client
```

### Planned
```
modules/pekko-stream (planned)
  ├── modules/core
  ├── modules/codex-client
  └── modules/claude-code-client
```

## Code Organization Principles

- ドメインモデル（core）は外部ライブラリに依存しない。
- MCPクライアントモジュールはMCP Java SDKに依存する。
- 契約テストは実装固有ではなく `McpClient` 契約を検証する。
- E2Eテストは実サーバ接続を確認する（CLIが存在しない環境では `assume` でスキップ）。
- Pekko-Streamモジュールは今後追加予定（現時点では未実装）。
