# TruthSpine for JetBrains IDEs

**TruthSpine Knows Your Project Truth and Automatically Keeps Your Agents Current, Start to Finish.**

TruthSpine knows your project truth from the sources you connect, keeps it current as your project changes, and automatically gives your connected agents the project knowledge and next work they need.

TruthSpine connects the exact project open in a JetBrains IDE to the local TruthSpine app. The plugin never guesses a project root and does not upload project content.

## Actions

- **Connect Current Project** sends the exact open project root to the local TruthSpine app and prepares the JetBrains connection.
- **Open TruthSpine** opens the local app without changing projects.
- **Copy MCP Configuration** copies the project-level configuration for **Settings > Tools > AI Assistant > Model Context Protocol**.
- **Copy Attach TruthSpine** copies the short phrase used when a chat requires a manual attach.
- **Review TruthSpine Connector** opens the JetBrains Marketplace review page.

When TruthSpine opens the MCP screen, choose **Add**, select **As JSON**, paste the copied connection, set **Level** to **Project**, then choose **Apply**. Open any chat you choose and say **Attach TruthSpine**. The TruthSpine desktop app shows separate confirmations for the project, workspace, and chat connection.

The plugin checks for a one-time local connection request when a project opens. It does not collect telemetry, upload project content, or silently change the selected TruthSpine project.

The TruthSpine desktop app must be installed on the same computer.

This connector is free and licensed under Apache License 2.0. The protected TruthSpine desktop app is installed and licensed separately. Installing the plugin does not start a TruthSpine trial or create a separate JetBrains license.

On Windows, the 14-day trial starts on the first successful TruthSpine app launch. A one-time USD 95 purchase unlocks TruthSpine V1 and all 1.x updates on one active computer at a time, with deactivation and transfer managed in the app.

On Mac, the 14-day trial starts only after you choose it and Apple completes the StoreKit transaction. A one-time USD 95 non-consumable purchase unlocks TruthSpine V1 and all 1.x updates, and Restore Purchases restores access on eligible Macs.

Future major versions are separate purchases.

## Build

Use Gradle 9 and Java 21:

```powershell
gradle buildPlugin verifyPlugin -PlocalIdePath="C:\Path\To\JetBrains\IDE"
```

The Marketplace upload archive is written to `build/distributions`.
