# Product Overview

ZAIN（ZAIN Agent Intention Network）は、[TAKT](https://github.com/nrslib/takt) から影響を受けたエージェントオーケストレーションライブラリです。現状はScala上のMCPクライアント基盤を提供し、将来的にPekko-Stream統合を提供する方針です。

## Core Concept

- **薄いライブラリ**: フレームワークではなくライブラリ。現状はMCPクライアントとして既存アプリケーションへ組み込み可能で、将来的にPekko-Streamコンビネータを提供する。
- **TAKTドメインモデルの踏襲**: Piece（ピース）、Facet（ファセット: Persona / Policy / Instruction / Knowledge / Output Contract）といったTAKTの概念モデルをScalaの型として表現する。
- **YAMLインターフェースなし**: TAKTがYAMLで宣言的にワークフローを定義するのに対し、ZAINはScala DSLベースでプログラマティックにワークフローを構築する方針を取る。
- **MCPクライアント経由のLLM接続**: Codex MCP / Claude Code MCPサーバにMCPクライアントで接続し、CLI起動コストを回避する。

## Target Use Cases

- MCPサーバ（Codex / Claude Code）を型安全に扱いたい場合。
- 将来的にPekko-Streamベースのアプリケーションへエージェントオーケストレーションを組み込みたい場合。
- Codex MCP / Claude Code MCPサーバをプログラムから直接操作したい場合。

## AI Agent Skills による開発支援

将来的にPekko-Streamコンビネータを直接扱う際、YAMLベースのTAKTより難易度が高くなる。これを補うため、ZAIN用エージェントスキルを提供する。開発者は自然言語で要件を伝えると、AIエージェントがZAINを使ったワークフローコードを生成する。

## Value Proposition

現在はTAKTの概念をMCPクライアント基盤として段階的に取り込み、将来的にScalaの型システムとPekko-Streamのリアクティブストリーム処理へ融合させる。YAMLの手軽さの代わりに、コンパイル時型検査・ストリーム合成・バックプレッシャー制御といったプログラマティックな利点を得る。
