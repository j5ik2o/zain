# Project Overview
- Project: zain
- Purpose: ScalaベースのMCPクライアント群（Codex/Claude Code）と共通MCPドメインを提供するマルチモジュール構成。
- Main modules: `modules/core`, `modules/codex-client`, `modules/claude-code-client`, `modules/contract-tests`, `modules/e2e-tests`.
- Build: sbt multi-project (`build.sbt`).
- Language/runtime: Scala 3.8.1, JVM.
- Main dependency: MCP Java SDK (`io.modelcontextprotocol.sdk`).
