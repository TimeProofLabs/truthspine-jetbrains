# TruthSpine Connector for JetBrains IDEs

**TruthSpine keeps your project’s memory, automatically on your computer and is used by every AI tool you connect, so no new chat starts from zero and the whole project has a full memory to work from.**

**You lead the work. TruthSpine keeps the truth.**

Every new AI chat forgets your project, so you explain it again. TruthSpine keeps one record of it on your own computer: what was decided, what's done, what's next, and the evidence for each. You don't write that record; your AI chats add to it as they work, and TruthSpine reads your project's own files. Every AI tool you connect, from Claude and ChatGPT to Cursor and 16 more, works from the same record. It uses no AI of its own, so it never spends your AI allowance by itself.

This connector needs the TruthSpine app: [truthspine.app/download](https://truthspine.app/download)

## Start here

1. Install this free plugin.
2. Get the TruthSpine app: the [Microsoft Store](https://apps.microsoft.com/detail/9pbn25k75vdj) on Windows, the [Mac App Store](https://apps.apple.com/us/app/truthspine/id6794478470?mt=12) on Mac, or truthspine.app on Linux (AppImage). The plugin opens the right one.
3. Say yes when TruthSpine shows the project you have open. It never looks through the rest of your computer.
4. Add the connection in **Settings > Tools > AI Assistant > Model Context Protocol**: choose **Add**, then **As JSON**, paste what **Copy MCP Configuration** copied, set **Level** to **Project**, and choose **Apply**.
5. Open a chat and say **Connect TruthSpine** once. From then on it knows your project.

## Actions

- **Connect Current Project** connects the project you have open, after you say yes.
- **Open TruthSpine** opens the app.
- **Download TruthSpine App** opens the right store or download page.
- **Copy MCP Configuration** copies the connection for AI Assistant.
- **Copy Attach TruthSpine** copies the short phrase for a chat.
- **Review TruthSpine Connector** opens the Marketplace review page.

## Your project and your privacy

Your project stays on your computer. When you open a store page from the plugin, it tells our license server which page it opened. It never sends your project or where it is on your computer.

## The TruthSpine app

This connector is open source (Apache License 2.0). It needs the TruthSpine app, which you get separately. The connector needs no license of its own.

On Windows, get the TruthSpine app from the Microsoft Store.

On Mac, get the TruthSpine app from the App Store.

On Linux, get the TruthSpine AppImage from truthspine.app.

## Build

Use Gradle 9 and Java 21:

```powershell
gradle buildPlugin verifyPlugin -PlocalIdePath="C:\Path\To\JetBrains\IDE"
```

The Marketplace upload archive is written to `build/distributions`.
