# 発見ログ: mcp-client-sdk-modules-codex-client-claude-code-client

## サマリー
- **機能**: `mcp-client-sdk-modules-codex-client-claude-code-client`
- **発見スコープ**: 新規
- **主要な発見**:
  - 現行リポジトリには `modules/` 配下実装が存在せず、新規のマルチモジュール設計が必要。
  - 要件は `modules/codex-client` と `modules/claude-code-client` を要求し、共通契約 `McpClient` の多態利用を前提としている。
  - MCP Java SDK はクライアント側で `STDIO` / `Streamable-HTTP` / `SSE` をサポートし、`initialize` と `closeGracefully` を明示的に扱う前提がある。

## リサーチログ

### 要件と制約の確定
- **コンテキスト**: 設計対象と受け入れ条件の確定。
- **参照元**:
  - `.kiro/specs/mcp-client-sdk-modules-codex-client-claude-code-client/requirements.md`
  - `.takt/runs/20260220-141610-feature-mcp-client-sdk-modules/context/policy/generate-design.1.20260220T141610Z.md`
- **発見**:
  - 共通契約 `McpClient` の必須操作は `openSession` / `execute` / `closeSession`。
  - 戻り値契約は全操作で `McpResult`。
  - エラー契約は少なくとも `ConnectionUnavailable` / `SessionClosed` / `InvalidRequest` / `SessionNotFound` を公開する必要がある。
  - `McpResponse` は成功時に `provider` / `sessionId` / `content` を必須で含む。
- **影響**: 型契約を最初に `core` で固定し、各プロバイダ実装はその契約に従う設計にする。

### 現行コードベースと統合ポイント
- **コンテキスト**: 既存実装の再利用可否と変更起点の特定。
- **参照元**:
  - `build.sbt`
  - `src/main/scala`（空）
  - `src/test/scala`（空）
  - `.mcp.json`
- **発見**:
  - `build.sbt` は単一 `root` プロジェクトのみで、サブモジュール未定義。
  - `src` 配下は空で、同レイヤーの既存実装はない。
  - `.mcp.json` の運用実態は `codex` を `stdio` で起動しており、MCPサーバ接続に transport 指定が必要な前提と整合する。
- **影響**: 設計では sbt マルチモジュール化（core + provider clients）を前提にし、接続設定モデルに transport を明示する。

### ステアリング整合性の確認
- **コンテキスト**: プロジェクト全体方針との整合。
- **参照元**:
  - `.kiro/steering/structure.md`
  - `.kiro/steering/tech.md`
  - `.kiro/steering/product.md`
- **発見**:
  - ステアリングは「薄いライブラリ」「MCP Java SDK利用」「coreとMCP接続モジュールの分離」を要求。
  - モジュール名は steering では `codex-mcp` / `claude-code-mcp` が例示される一方、要件は `codex-client` / `claude-code-client` を要求している。
- **影響**: この feature の受け入れ条件を優先し、ディレクトリ名は要件準拠、パッケージ責務と依存方向は steering 準拠で設計する。

### 類似パターン調査（既存参照コード）
- **コンテキスト**: 新規設計時の構造パターン抽出。
- **参照元**:
  - `references/takt/src/infra/providers/types.ts`
  - `references/takt/src/infra/providers/index.ts`
  - `references/takt/src/infra/providers/codex.ts`
  - `references/takt/src/infra/providers/claude.ts`
- **発見**:
  - 共通契約（Provider interface）と実装（Codex/Claude）の分離が明確。
  - 変換責務（共通オプション -> プロバイダ固有オプション）を Adapter 側で閉じており、上位層にインフラ詳細を漏らしていない。
- **影響**: `McpClient` 契約と provider adapter を分離し、プロバイダ固有の transport/client 生成は adapter 内に局所化する。

### 外部依存調査（MCP Java SDK）
- **コンテキスト**: transport とセッションライフサイクルの妥当性確認。
- **参照元**:
  - https://github.com/modelcontextprotocol/java-sdk
  - https://modelcontextprotocol.io/sdk/java/mcp-client
- **発見**:
  - Java SDK は client side で `STDIO` / `Streamable-HTTP` / `SSE` をサポート。
  - クライアント利用フローとして `initialize` と `closeGracefully` が示されている。
  - 同期 (`McpSyncClient`) / 非同期 (`McpAsyncClient`) の両APIが存在する。
- **影響**: `openSession` は client initialize 完了を成功条件に含め、`closeSession` は graceful close を契約として扱う設計とする。

## 設計判断

### 判断1: 共通契約を `modules/core` に集約する
- **コンテキスト**: 要件1と要件6は多態利用と契約テスト成立を要求。
- **代替案**:
  - 各クライアントモジュールで個別の型を定義する。
  - 共通契約を `core` で一元定義する。
- **選択**: `core` に `McpClient` / `McpResult` / `McpResponse` / `McpError` を集約。
- **根拠**: 契約の単一化により、要件6の契約テストを同一観点で実行できる。
- **トレードオフ**: `core` 変更時に両実装へ影響が及ぶが、契約変更の可視性は高まる。

### 判断2: セッション状態を adapter 境界で明示管理する
- **コンテキスト**: 要件2.4/3.4/5.4はセッション状態起因の失敗分類を要求。
- **代替案**:
  - SDK任せで状態を暗黙管理。
  - `McpSessionCatalog` で `Opened` / `Closed` を明示管理。
- **選択**: `McpSessionCatalog` を導入し、`execute`/`closeSession` 前に状態検証する。
- **根拠**: `SessionClosed` と `SessionNotFound` を再現可能にし、実装間で判定基準を揃えられる。
- **トレードオフ**: セッションメタデータ保持の責務が増える。

### 判断3: 入力境界は parse して契約エラーへ正規化する
- **コンテキスト**: 要件5.1〜5.3で 0文字と1文字を区別する必要がある。
- **代替案**:
  - 各 adapter で都度バリデーション。
  - `McpRequestParser` で一元的に `InvalidRequest` 判定。
- **選択**: `McpRequestParser` を共通化し、0文字のみ失敗とする。
- **根拠**: 2実装で境界挙動を一致させやすく、契約テストで差分が出にくい。
- **トレードオフ**: parser への依存が増えるが、責務が明確になる。

### 判断4: モジュール命名は要件優先、依存方向は steering 優先
- **コンテキスト**: 要件と steering のモジュール名例が不一致。
- **代替案**:
  - steering の例名 (`codex-mcp`) に合わせる。
  - 要件名 (`codex-client`) に合わせる。
- **選択**: 本 feature では要件名を採用。
- **根拠**: 受け入れ条件と成果物パスの整合を優先するため。
- **トレードオフ**: 将来、全体命名統一のリファクタリングが発生する可能性がある。

## リスクと緩和策
| リスク | 緩和策 |
|--------|--------|
| Codex/Claude MCP サーバ実体の差で同一契約を満たせない | 契約テストを共通化し、プロバイダ差分は adapter 内に閉じる |
| transport ごとの差異（stdio/http/sse）で openSession の成功条件がぶれる | `McpConnectionConfig` と transport別 factory を分離し、成功条件を `initialize` 完了に統一 |
| セッション状態不整合で `SessionClosed` / `SessionNotFound` が不安定になる | `McpSessionCatalog` の状態遷移を仕様化し、単体テストで遷移網羅を固定 |
| 要件と steering の命名差分が将来の理解コストになる | `design.md` に命名判断を明記し、次フェーズで統一可否を独立タスク化する |
| 外部依存（MCP Java SDK）更新による API 変更 | SDK バージョンを固定し、adapter境界で依存を隔離して変更波及を最小化する |
