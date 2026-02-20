ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.1"

lazy val mcpSdkVersion = "0.18.1"

lazy val commonSettings = Seq(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
)

lazy val root = (project in file("."))
  .aggregate(core, codexClient, claudeCodeClient, contractTests, e2eTests)
  .settings(commonSettings)
  .settings(
    name := "zain"
  )

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)
  .settings(
    name := "core"
  )

lazy val codexClient = (project in file("modules/codex-client"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "codex-client",
    libraryDependencies ++= Seq(
      "io.modelcontextprotocol.sdk" % "mcp-core" % mcpSdkVersion,
      "io.modelcontextprotocol.sdk" % "mcp-json-jackson3" % mcpSdkVersion
    )
  )

lazy val claudeCodeClient = (project in file("modules/claude-code-client"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "claude-code-client",
    libraryDependencies ++= Seq(
      "io.modelcontextprotocol.sdk" % "mcp-core" % mcpSdkVersion,
      "io.modelcontextprotocol.sdk" % "mcp-json-jackson3" % mcpSdkVersion
    )
  )

lazy val contractTests = (project in file("modules/contract-tests"))
  .dependsOn(core, codexClient, claudeCodeClient)
  .settings(commonSettings)
  .settings(
    name := "contract-tests"
  )

lazy val e2eTests = (project in file("modules/e2e-tests"))
  .dependsOn(core, codexClient, claudeCodeClient)
  .settings(commonSettings)
  .settings(
    name := "e2e-tests",
    libraryDependencies ++= Seq(
      "io.modelcontextprotocol.sdk" % "mcp-core" % mcpSdkVersion % Test,
      "io.modelcontextprotocol.sdk" % "mcp-json-jackson3" % mcpSdkVersion % Test
    ),
    Test / fork := true
  )
