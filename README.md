# VexelCore
VexelCore is an API that's designed to be used with Bukkit derivative (i.e. Paper) and Velocity plugins so developers can write less code and offload complexity.

### 🔥 Why It's Awesome
- Feature management system simplifies on the fly disabling/enabling/reloading
- Reliable utilities to simplify code and speed up development
- Necessitates standards of format in configuration files for end user simplicity
- Two editions (meant for Bukkit and Velocity setups respectively) that share a common utils
- Targeted at the latest versions of Minecraft
- Developed with the community

## 🕹 ️Demo Clip
Coming soon

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