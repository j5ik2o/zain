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

### MCP SDK Client Adapter
**Location**: `/modules/mcp-sdk-client/`  
**Purpose**: MCP Java SDKベースの単一アダプタを提供する。`McpProvider` でCodex / Claude Codeを切り替え、ツール呼び出し・セッション管理を共通化する。

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

- **Modules**: `kebab-case`（例: `mcp-sdk-client`, `contract-tests`, `e2e-tests`）
- **Packages**: `zain.core.mcp`, `zain.mcp.sdk`, `zain.mcp.contract`, `zain.e2e`
- **Types**: Scala標準の `PascalCase`
- **Functions/Methods**: Scala標準の `camelCase`
- **Files**: 1公開型 = 1ファイル。ファイル名は型名を反映

## Module Dependencies

### Current
```
modules/mcp-sdk-client
  └── modules/core

modules/contract-tests
  ├── modules/core
  └── modules/mcp-sdk-client

modules/e2e-tests
  ├── modules/core
  └── modules/mcp-sdk-client
```

### Planned
```
modules/pekko-stream (planned)
  ├── modules/core
  └── modules/mcp-sdk-client
```

## Code Organization Principles

- ドメインモデル（core）は外部ライブラリに依存しない。
- SDKアダプタモジュール（`mcp-sdk-client`）はMCP Java SDKに依存し、Provider差分を `McpProvider` で吸収する。
- 契約テストは実装固有ではなく `McpClient` 契約を検証し、Codex / Claude Code双方で同一契約を再利用する。
- E2Eテストは実サーバ接続を確認する（CLIが存在しない環境では `assume` でスキップ）。加えてSDK直結テストで初期化/ツール列挙の互換性を確認する。
- Pekko-Streamモジュールは今後追加予定（現時点では未実装）。

## Steering Metadata

- updated_at: 2026-02-20T17:57:41Z
- sync_reason: モジュール統合（`mcp-sdk-client`）と依存方向の実態に合わせて構造情報を更新
