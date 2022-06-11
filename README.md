# VexelCore
VexelCore is an API that's designed to be used with your Bukkit derivative (i.e. Paper) and Velocity plugins so developers can write less code.

### 🔥 API Advantages
- Reliable utilities to simplify code and speed up development.

## 📈 Current Features
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
WIP

## 📝 Planned Features
WIP

## ⌨️ Developers
WIP

## ✋ Help
Please join [Martin's Community](https://discord.gg/QW2m6bYG4S) Discord server.

## ⚖️ License
VexelCore is currently licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)

## 📦 Dependencies
This project utilizes the following dependencies:
- [json-simple](https://github.com/fangyidong/json-simple)
  - Licensed under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)