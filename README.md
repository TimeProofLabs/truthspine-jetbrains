# TruthSpine for JetBrains IDEs

TruthSpine keeps current project Truth available to the AI workspaces you use.

## Actions

- **Connect Current Project** sends the exact open project root to the local TruthSpine app and prepares the JetBrains connection.
- **Open TruthSpine** opens the local app without changing projects.
- **Copy Attach TruthSpine** copies the short phrase used when a chat requires a manual attach.

The plugin does not run at IDE startup, collect telemetry, upload project content, or silently change the selected TruthSpine project.

The TruthSpine desktop app must be installed on the same computer.

## Build

Use Gradle 9 and Java 21:

```powershell
gradle buildPlugin verifyPlugin -PlocalIdePath="C:\Path\To\JetBrains\IDE"
```

The Marketplace upload archive is written to `build/distributions`.
