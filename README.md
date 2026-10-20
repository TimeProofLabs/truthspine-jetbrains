# TruthSpine Connector for JetBrains IDEs

**Whole project truth for every AI you use. On your computer. 14-day free trial, then $99.99 once, down from $249.99 for a limited time.**

**You lead the work. TruthSpine keeps the truth.**

The keeper of your project. One checked record of what was decided, what was done, what failed and what comes next. Every connected chat starts already knowing your project. Your project stays on your computer. You lead the work. TruthSpine keeps the truth.

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

## App and price

This connector is free (Apache License 2.0). It needs the TruthSpine app, which you get and buy separately. Installing the connector does not start a trial or need its own license.

On Windows, start the free 14-day trial in the TruthSpine app. Then buy once in the Microsoft Store: $99.99 at the launch price (regular price $249.99), for V1 and every 1.x update. Reinstalling does not restart the trial.

On Mac, start the free 14-day trial in the TruthSpine app. Then buy once in the App Store: $99.99 at the launch price (regular price $249.99), for V1 and every 1.x update. Restore Purchases brings it back on your other Macs.

On Linux, the TruthSpine AppImage from truthspine.app starts with a free 14-day trial. Then buy once from us: $99.99 at the launch price (regular price $249.99), for V1 and every 1.x update, on up to three of your own Linux computers. You can move it between them in the app.

Future major versions are sold separately.

## Build

Use Gradle 9 and Java 21:

```powershell
gradle buildPlugin verifyPlugin -PlocalIdePath="C:\Path\To\JetBrains\IDE"
```

The Marketplace upload archive is written to `build/distributions`.
