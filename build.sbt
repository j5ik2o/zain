ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.1"

lazy val mcpSdkVersion = "0.18.1"

lazy val commonSettings = Seq(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
)

lazy val root = (project in file("."))
  .aggregate(core, mcpSdkClient, contractTests, e2eTests)
  .settings(commonSettings)
  .settings(
    name := "zain"
  )

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)
  .settings(
    name := "core"
  )

lazy val mcpSdkClient = (project in file("modules/mcp-sdk-client"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "mcp-sdk-client",
    libraryDependencies ++= Seq(
      "io.modelcontextprotocol.sdk" % "mcp-core" % mcpSdkVersion,
      "io.modelcontextprotocol.sdk" % "mcp-json-jackson3" % mcpSdkVersion
    )
  )

lazy val contractTests = (project in file("modules/contract-tests"))
  .dependsOn(core, mcpSdkClient)
  .settings(commonSettings)
  .settings(
    name := "contract-tests"
  )

lazy val e2eTests = (project in file("modules/e2e-tests"))
  .dependsOn(core, mcpSdkClient)
  .settings(commonSettings)
  .settings(
    name := "e2e-tests",
    libraryDependencies ++= Seq(
      "io.modelcontextprotocol.sdk" % "mcp-core" % mcpSdkVersion % Test,
      "io.modelcontextprotocol.sdk" % "mcp-json-jackson3" % mcpSdkVersion % Test
    ),
    Test / fork := true
  )
