# VexelCore
VexelCore is an API that's designed to be used with Bukkit derivative (i.e. Paper, Purpur, etc.) and Velocity plugins so developers can write less code and offload complexity.

### 🔥 Why It's Awesome
- Feature management system simplifies on the fly disabling/enabling/reloading
- Reliable utilities to simplify code and speed up development
- Necessitates standards of format in configuration files for end user simplicity
- Two editions (meant for Bukkit and Velocity setups respectively) that share a common utils
- Targeted at the latest versions of Minecraft
- Developed with the community

# 🕹 ️Features
The VexelCore API has features for Bukkit-based and Velocity-based Minecraft server software platforms.
"Common" features are shared by both.

## Common
- Feature management system
  Features are groups of related commands and listeners.
  - On the fly disabling, enabling, and reloading of commands and listeners
- You're likely to interact with it through the platform Feature Manager `reload()` method.
Main class Example:
`java
this.bukkitFeatureManager = new BukkitFeatureManager();
bukkitFeatureManager.register(new CoolFeature(instance));
bukkitFeatureManager.reload();
`
CoolFeature class example:
`java
public class CoolFeature extends BukkitFeature {

  public CoolFeature(JavaPlugin instance) {
    super("cool", "Cool description", null, instance);
    registerCommand(new CoolCommand(instance));
  }
}`

TIP: Remember to call `bukkitFeatureManager.reload()` in your plugins `onEnable()` method to enable the features. 
- Command Helper
Helps track arguments. Planning to add type and regex validation.
Example:
`java
CMDHelper cmdHelper = new CMDHelper(args);
if (cmdHelper.argExists(0)) {
  Player target = Bukkit.getPlayer(args[0]);
  if (target == null) {
    new BukkitMsgBuilder("&cThat player is offline!").send(player);
  return;
}
target.sendMessage(new BukkitMsgBuilder("&7Hello!").get());
}
`
- BoostedYAML Config
  - Easily write, save, and reload BoostedYAML configs.
- Icon
Set of useful icons.

and more.
## Bukkit
- Custom command (will eventually work for Velocity too)
A command registered with the feature manager.
  - Automatically creates help page (@Override `help()` to customize)
  - Automatic tab completion of subcommands and additional completions (@Override `getAdditionalCompletions()`).
- Menu System (WIP)
  - Pages
  - Right and left click actions
  - Prevents players from taking the item
- Message Builder
Easily create messages that automatically transform color codes (ex. &7) to 
- Utilities
  Various useful utilities

and more.

# Velocity
Not much has been done with Velocity as the current focus is on Bukkit. This will change sometime soon.

## 📝 Planned Features
- Localization//i18n system
- MySQL database manager
- And more

## ⌨️ Developers
The following developer documentation is a WIP.

### Importing
VexelCore is currently a massive work-in-progress that is changing weekly.
Please obtain the latest Bukkit or Velocity edition and add it to your project build configuration.

Gradle example:

`compileOnly files('/home/martin/GitHub/VexelCore/bukkit/build/libs/VexelCore-Bukkit-1.0.1-SNAPSHOT.jar')`

Note: This will change once the API is ready for an official 1.0 release.

### Note on Editions
The majority of work so far has been put into the Bukkit edition of VexelCore.

### Dependency Reminder
Don't forget to make "VexelCore" a dependency for your project's plugin platform dependency management system.

## Contribute
All contributions are appreciated, but the project desperately needs more documentation.

## ✋ Help
Please join [Martin's Community](https://discord.gg/QW2m6bYG4S) Discord server.

## ⚖️ License
VexelCore is currently licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)

## 📦 Dependencies
VexelCore aims to not shade dependencies. The Bukkit edition handles dependencies through the plugin.yml but the Velocity edition is undecided.

This project utilizes the following dependencies:
- [json-simple](https://github.com/fangyidong/json-simple)
  - Licensed under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
  - Used in (as of Aug 3rd, 2022):
    - MojangUtils
    - PlayerSkinInformation