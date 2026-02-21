# 発見ログ: references-takt-always-valid

## サマリー
- **機能**: `references-takt-always-valid`
- **発見スコープ**: 新規
- **主要な発見**: 参照実装の中核制約（`movements>=1`, `max_movements=10`, 実行モード排他、`running/completed/aborted`）は確認できた。移植先 `modules/core` には TAKT ドメイン型が未実装であり、`Part` ドメインと遷移検証コンテキストを設計へ明示追加する必要があった。

## リサーチログ

### 要件・対象範囲の確定
- **コンテキスト**: 何を移植対象に含めるかを固定するため
- **参照元**: `.kiro/specs/references-takt-always-valid/requirements.md`
- **発見**:
  - 要件1.1は Piece/Movement/Rule/Output Contract/Part/実行状態の列挙を必須化している。
  - 要件2〜6は「生成時拒否」「識別可能エラー」「同一無効入力で同一失敗種別」を要求している。
- **影響**: 設計の主軸を `PieceDefinitionFactory` + ドメインプリミティブ + 型付きエラーに置いた。

### 参照モデルの不変条件
- **コンテキスト**: 参照実装の契約を移植先の仕様へ変換するため
- **参照元**:
  - `references/takt/src/core/models/piece-types.ts`
  - `references/takt/src/core/models/schemas.ts`
  - `references/takt/src/infra/config/loaders/pieceParser.ts`
- **発見**:
  - `movements` は最小1件。
  - `initial_movement` 省略時は先頭 movement。
  - `max_movements` 既定値は10、0以下は不許可。
  - `parallel` / `arpeggio` / `team_leader` は相互排他。
- **影響**: `PieceDefinition` / `MovementDefinition` の生成時制約として固定した。

### Part ドメインの抽出
- **コンテキスト**: 前回レビューで欠落指摘された `Part` 系を補完するため
- **参照元**:
  - `references/takt/src/core/models/part.ts`
  - `references/takt/src/core/models/schemas.ts`
  - `references/takt/src/infra/config/loaders/pieceParser.ts`
- **発見**:
  - `PartDefinition`（`id`, `title`, `instruction`, `timeoutMs?`）と `TeamLeaderConfig` が独立したドメイン概念として存在。
  - `max_parts` は `max(3)`、`timeout_ms` は正数。
- **影響**: 設計へ `PartDefinition` / `TeamLeaderConfiguration` を追加し、要件1.1の対象漏れを解消した。

### 実行状態遷移の成立条件
- **コンテキスト**: 要件5.5（未定義Movement遷移拒否）を契約として成立させるため
- **参照元**:
  - `references/takt/src/core/piece/engine/PieceEngine.ts`
  - `references/takt/src/core/piece/engine/state-manager.ts`
- **発見**:
  - `validateConfig` で `initialMovement` と `rule.next` の存在検証を実施。
  - 初期状態は `status=running`, `iteration=0`。
- **影響**: `PieceExecutionState` に `allowedMovements` を保持し、`transitionTo` 単独で未定義遷移を拒否できる設計へ変更した。

### Facet参照の現状と強化点
- **コンテキスト**: 要件4.5/6.x の失敗契約を満たすため
- **参照元**:
  - `references/takt/src/faceted-prompting/resolve.ts`
  - `references/takt/src/infra/config/loaders/resource-resolver.ts`
- **発見**:
  - `resolveRefList` は未解決参照を結果から除外し得る。
- **影響**: 移植側では `FacetCatalog.validateReferences` で未定義参照を明示エラー化する方針にした。

### 既存コードベースとの整合性
- **コンテキスト**: 実装時にプロジェクト規約と乖離しないようにするため
- **参照元**:
  - `.kiro/steering/product.md`
  - `.kiro/steering/structure.md`
  - `.kiro/steering/tech.md`
  - `modules/core/src/main/scala/zain/core/mcp/`
- **発見**:
  - `modules/core` は外部依存を持たないドメイン中心設計。
  - 1公開型=1ファイル、不変値中心、失敗契約は `Either`/結果型が既存パターン。
- **影響**: `zain.core.takt` でも同じ構造・命名・失敗表現を採用する設計とした。

## 設計判断

### 判断1: Part ドメインを明示的に移植対象へ含める
- **コンテキスト**: 要件1.1にPartが明記されている
- **代替案**: `Part` は対象外として除外扱いにする / ドメイン型として含める
- **選択**: `PartDefinition` と `TeamLeaderConfiguration` をドメインモデルへ含める
- **根拠**: 参照実装に独立型として存在し、要件対象から外せないため
- **トレードオフ**: 型数は増えるが、移植完全性と検証可能性が向上する

### 判断2: 遷移検証コンテキストを状態内部に持つ
- **コンテキスト**: `transitionTo` 単体で `5.5` を満たす必要がある
- **代替案**: 遷移時に毎回外部から movement 一覧を渡す / 開始時に状態へ取り込む
- **選択**: `PieceExecutionState.allowedMovements` として保持
- **根拠**: インターフェースが自己完結し、隠れ依存を防げる
- **トレードオフ**: 状態サイズは増えるが、契約の明確性が高い

### 判断3: 要件ID単位のテストマトリクスを設計に内包する
- **コンテキスト**: 前回レビューで `7.1` の検証計画不足が指摘された
- **代替案**: 粒度の粗いユニット/統合方針のみ記載 / 要件ID対応表まで明示
- **選択**: 要件ごとのテストID・シナリオ・期待結果を設計に追加
- **根拠**: 実装フェーズでの受け入れ判定を機械的に行えるため
- **トレードオフ**: 設計記述量は増えるが、レビュー工数と解釈差を削減できる

## リスクと緩和策
| リスク | 緩和策 |
|--------|--------|
| 参照実装との差分（未解決Facetの扱いなど）で移植互換性が揺れる | 差分を意図的変更として設計書に明記し、要件4.5/6.xのテストで固定する |
| エラー種別の分岐順によって6.3が不安定になる | バリデーション順序（プリミティブ→構造→参照）を固定し、反復入力テストを追加する |
| Part系の制約不足でteam leader経路が無効状態を許す | `TeamLeaderConfiguration` と `PartDefinition` に個別コンストラクタ制約を定義する |
| 実行状態遷移が外部情報依存になり5.5を破る | `allowedMovements` を状態に取り込み、`transitionTo` 内で完結検証する |
