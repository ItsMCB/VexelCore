# VexelCore

> The VexelCore system works for Bukkit-based servers and Velocity-based proxies. Each has a respective plugin that handles modules; Java jars built with the VexelCore API designed for that target platform.

VexelCore is for developers and server owners who want to simplify their development and update workflow.

### 🔌 Plugin Advantages
- Instantly enjoy changes without restarting the server.
- Easily debug your code with commands that output key information.

### 🔥 API Advantages
- Use utilities to simplify your code and speed up development.

## 📈 Current Features
- Module reloading system to enjoy code changes without restarting
- Debugging Utilities
  - Velocity: Call Events
- Useful icon set with unambiguous names
- Chat Utilities
  - Easily send multiple messages in one line with color code translation
``` java
public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        BukkitUtils.send(sender, "&aMessage 1","&cMessage 2","&dMessage 3");
        return true;
}
```

- Command Helper
  - Assists with the handling of arguments and tab completion

``` java
public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CMDHelper cmdHelper = new CMDHelper(args);
        if (cmdHelper.isCalling("test")) {
            BukkitUtils.send(sender, "&7Hello!");
        }
        if (cmdHelper.isCalling("test2") && sender.hasPermission("example.permission")) {
            if (cmdHelper.argEquals(1, "hello")) {
                BukkitUtils.send(sender, "&7Hello!");
            }
            if (cmdHelper.argExists(2) {
                BukkitUtils.send(sender, "&7Yet another argument exists!");
            }
        }
        return true;
    }
 ```

``` java
public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    CMDHelper cmdHelper = new CMDHelper(args);
    cmdHelper.addTabCompletion(cmdHelper.getMap(0,null),List.of("test1","test2"));
    cmdHelper.addTabCompletion(cmdHelper.getMap(1,"test1"),List.of("option1","option2"));
    return cmdHelper.generateTabComplete();
}
 ```

- Misc. Utilities
  - Files
  - Mojang
    - Validate existence of UUIDs.
    - Get username from UUID.
  - Website Content Requester

## 🕹 ️Demo Clip
Shows off some key features as of 3/29/2022.

[![](https://img.youtube.com/vi/b1AsLoSj86Q/hqdefault.jpg)](https://youtu.be/b1AsLoSj86Q)

## 📝 Planned Features
- Ability to update module jar files by downloading GitHub CI workflow artifacts.

## ⌨️ Developers
Getting started is easy. First, understand the purpose of each API.

> Common: Module handling template and universal utilities, notably a command handler, in-game icon set, and web response utility.

> Bukkit: Extends the module handling template to work with Bukkit-based servers. Notably includes message utilities for Bukkit.

> Velocity: Extends the module handling template to work with Velocity-based proxies. Notably includes message utilities for Velocity.

Second, choose the platform you're developing for, set up the respective server environment, and put the corresponding VexelCore plugin in the platform's plugin folder.

Third, set up your IDE to use the VexelCore API designed for the platform you're developing.

Fifth, create a main class that extends "VexelCoreModule" and use super method calls to register your commands, listeners, and other module data. Additionally, ensure that "Main-Class" is added to your META-INF in the built jar.

Last, move the exported module jar file to the "Modules" folder under "VexelCore" and use the built-in reload command. Continue to do this as you change your module's code.


## ✋ Help
Please join [Martin's Community](https://discord.gg/QW2m6bYG4S) Discord server.

## ⚖️ License
VexelCore is currently licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)

## 📦 Dependencies
This project utilizes the following dependencies:
- [json-simple](https://github.com/fangyidong/json-simple)
  - Licensed under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)