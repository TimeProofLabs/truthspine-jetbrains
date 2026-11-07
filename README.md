# TruthSpine Connector for JetBrains IDEs

**Stop re-explaining your project to every new AI chat. It works on your computer, with no cloud account and nothing extra to install.**

**You lead the work. TruthSpine keeps the truth.**

Every connected chat checks your project before it answers, so you never re-explain it. TruthSpine keeps one checked record of what was decided, what was done, what failed, and what comes next.

This plugin is the connector. It needs the TruthSpine app on the same computer.

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
