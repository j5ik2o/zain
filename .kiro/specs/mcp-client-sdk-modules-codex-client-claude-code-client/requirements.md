## 導入

本機能は、MCP Client SDKを利用した `modules/codex-client` と `modules/claude-code-client` の2つのライブラリを対象に、共通インターフェイス経由で利用できる要件を定義する。目的は、呼び出し側が実装差分を意識せずに同一の契約でMCPサーバ連携を実行できる状態を作ることである。

本要件では、共通インターフェイスの契約、各ライブラリの正常系挙動、異常系挙動、入力境界条件、要件間依存を明示する。これにより、実装後に契約テストと受け入れテストで多態利用の成立を検証可能にする。

## 実装コンテキスト

- 既存実装の有無: なし
- 判定根拠:
  - リポジトリ直下を確認した結果、`modules` ディレクトリが存在しない。
  - `src` 配下にソースファイルが存在しない。
  - `build.sbt` は単一 `root` プロジェクト定義のみで、対象ライブラリ定義が未作成である。

## 要件

### 1. 共通インターフェイス契約

**目的:** ライブラリ利用者として `codex-client` と `claude-code-client` を同じ型契約で扱い、呼び出し側の分岐をなくしたい。

**受け入れ条件:**
- 1.1 共通インターフェイス `McpClient` は常に `openSession` 操作を公開しなければならない。
- 1.2 共通インターフェイス `McpClient` は常に `execute` 操作を公開しなければならない。
- 1.3 共通インターフェイス `McpClient` は常に `closeSession` 操作を公開しなければならない。
- 1.4 共通インターフェイス `McpClient` は常に `openSession` の戻り値型を `McpResult` で表現しなければならない。
- 1.5 共通インターフェイス `McpClient` は常に `execute` の戻り値型を `McpResult` で表現しなければならない。
- 1.6 共通インターフェイス `McpClient` は常に `closeSession` の戻り値型を `McpResult` で表現しなければならない。

**依存関係:** なし

### 2. codex-client ライブラリの振る舞い

**目的:** Codex MCPサーバを利用する利用者として、`modules/codex-client` を共通契約で実行したい。

**受け入れ条件:**
- 2.1 `openSession` が `serverUrl` と `transport` を含む接続設定で呼び出されたとき、`modules/codex-client` はCodex MCPサーバのセッションを開始しなければならない。
- 2.2 `execute` が開始済みセッションで呼び出されたとき、`modules/codex-client` は `McpResult.Success` を返却しなければならない。
- 2.3 Codex MCPサーバへの接続失敗が発生した場合、`modules/codex-client` は `McpResult.Failure(ConnectionUnavailable)` を返却しなければならない。
- 2.4 `execute` が終了済みセッションに対して呼び出された場合、`modules/codex-client` は `McpResult.Failure(SessionClosed)` を返却しなければならない。

**依存関係:** 1

### 3. claude-code-client ライブラリの振る舞い

**目的:** Claude Code MCPサーバを利用する利用者として、`modules/claude-code-client` を共通契約で実行したい。

**受け入れ条件:**
- 3.1 `openSession` が `serverUrl` と `transport` を含む接続設定で呼び出されたとき、`modules/claude-code-client` はClaude Code MCPサーバのセッションを開始しなければならない。
- 3.2 `execute` が開始済みセッションで呼び出されたとき、`modules/claude-code-client` は `McpResult.Success` を返却しなければならない。
- 3.3 Claude Code MCPサーバへの接続失敗が発生した場合、`modules/claude-code-client` は `McpResult.Failure(ConnectionUnavailable)` を返却しなければならない。
- 3.4 `execute` が終了済みセッションに対して呼び出された場合、`modules/claude-code-client` は `McpResult.Failure(SessionClosed)` を返却しなければならない。

**依存関係:** 1

### 4. 共通レスポンス契約

**目的:** 呼び出し側実装者として、どのクライアント実装でも同じレスポンス構造を処理したい。

**受け入れ条件:**
- 4.1 `modules/codex-client` の `execute` が成功したとき、共通インターフェイス `McpClient` は `provider=codex` を含む `McpResponse` を返却しなければならない。
- 4.2 `modules/claude-code-client` の `execute` が成功したとき、共通インターフェイス `McpClient` は `provider=claude-code` を含む `McpResponse` を返却しなければならない。
- 4.3 共通インターフェイス `McpClient` は常に `McpResponse` に `sessionId` を含めなければならない。
- 4.4 共通インターフェイス `McpClient` は常に `McpResponse` に `content` を含めなければならない。

**依存関係:** 1, 2, 3

### 5. 入力境界条件と共通エラー契約

**目的:** 利用者として、境界入力と無効状態で一貫した失敗結果を受け取りたい。

**受け入れ条件:**
- 5.1 `execute` に0文字の入力が渡された場合、共通インターフェイス `McpClient` は `McpResult.Failure(InvalidRequest)` を返却しなければならない。
- 5.2 `execute` に1文字の入力が渡されたとき、`modules/codex-client` は `McpResult.Failure(InvalidRequest)` を返却してはならない。
- 5.3 `execute` に1文字の入力が渡されたとき、`modules/claude-code-client` は `McpResult.Failure(InvalidRequest)` を返却してはならない。
- 5.4 未開始セッションIDで `execute` が呼び出された場合、共通インターフェイス `McpClient` は `McpResult.Failure(SessionNotFound)` を返却しなければならない。

**依存関係:** 1

### 6. 多態性の検証可能性

**目的:** 品質保証担当者として、2つのクライアント実装が同一契約に準拠していることを同じ観点で検証したい。

**受け入れ条件:**
- 6.1 共通インターフェイス `McpClient` 向けの契約テストが `modules/codex-client` に対して実行されたとき、`modules/codex-client` は全テストケースを成功させなければならない。
- 6.2 共通インターフェイス `McpClient` 向けの契約テストが `modules/claude-code-client` に対して実行されたとき、`modules/claude-code-client` は全テストケースを成功させなければならない。

**依存関係:** 1, 2, 3, 4, 5
