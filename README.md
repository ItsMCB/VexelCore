# VexelCore

The VexelCore system works for Bukkit-based servers and Velocity-based proxies. Each has a respective plugin that handles modules; Java jars built with the VexelCore API designed for that target platform.

VexelCore is for developers and server owners who want to simplify their development and update workflow.

Plugin Advantages
- Instantly enjoy changes without restarting the server.
- Easily debug your code with commands that output key information.

API Advantages
- Use utilities to simplify your code and speed up development.

## Developers
Getting started is easy. First, understand the purpose of each API.

Common: Module handling template and universal utilities, notably a command handler, in-game icon set, and web response utility.

Bukkit: Extends the module handling template to work with Bukkit-based servers. Notably includes message utilities for Bukkit.

Velocity: Extends the module handling template to work with Velocity-based proxies. Notably includes message utilities for Velocity.

Second, choose the platform you're developing for, set up the respective server environment, and put the corresponding VexelCore plugin in the platform's plugin folder.

Third, set up your IDE to use the VexelCore API designed for the platform you're developing.

Fifth, create a main class that extends "VexelCoreModule" and use super method calls to register your commands, listeners, and other module data. Additionally, ensure that "Main-Class" is added to your META-INF in the built jar.

Last, move the exported module jar file to the "Modules" folder under "VexelCore" and use the built-in reload command. Continue to do this as you change your module's code.

## Planned Features
- Ability to update module jar files by downloading GitHub CI workflow artifacts.

## Credits
This project utilizes the following dependencies:
- [json-simple](https://github.com/fangyidong/json-simple)
  - Licensed under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)