# TruthSpine for JetBrains IDEs

**TruthSpine Knows Your Project Truth and Automatically Keeps Your Agents Current, Start to Finish.**

TruthSpine knows your project truth from the sources you connect, keeps it current as your project changes, and automatically gives your connected agents the project knowledge and next work they need.

TruthSpine connects the exact project open in a JetBrains IDE to the local TruthSpine app. The plugin never guesses a project root and does not upload project content.

## Actions

- **Connect Current Project** sends the exact open project root to the local TruthSpine app and prepares the JetBrains connection.
- **Open TruthSpine** opens the local app without changing projects.
- **Copy MCP Configuration** copies the project-level configuration for **Settings > Tools > AI Assistant > Model Context Protocol**.
- **Copy Attach TruthSpine** copies the short phrase used when a chat requires a manual attach.

After approving the MCP configuration, open any chat you choose and say **Attach TruthSpine**. The TruthSpine desktop app shows separate confirmations for the project, workspace, and chat connection.

The plugin does not run at IDE startup, collect telemetry, upload project content, or silently change the selected TruthSpine project.

The TruthSpine desktop app must be installed on the same computer.

The plugin is free and never starts a trial or creates a separate JetBrains license. The TruthSpine desktop app owns the 14-day trial, one-time TruthSpine V1 purchase, restoration, and computer transfer.

## Build

Use Gradle 9 and Java 21:

```powershell
gradle buildPlugin verifyPlugin -PlocalIdePath="C:\Path\To\JetBrains\IDE"
```

The Marketplace upload archive is written to `build/distributions`.
