# VexelCore
Modular plugins for Bukkit-based servers and the Velocity proxy software with standard APIs.

### Why?
After working in the Minecraft server scene for a few years now, I've come to realize a few problems teams often face:
- Whole plugins are made to add simple features.
- Common methods are copy and pasted into multiple projects instead of being called from a central API.
- Getting started if difficult due to lack of experience and all the initial setup that must be done even after the IDE is configured.
- Having multiple systems makes it more difficult to manage.

VexelCore aims to solve these issues by:
- Being modular. Enable what works, disable what doesn't.
- Having standard APIs and utilities. Stop writing the same code over and over again.
- Lowering the level of entry when coding plugins through modules. Get started coding faster.
- Containing lots of documentation. Read or watch and learn.

### Simple Usage Example
Let's say there are three modules, a TNT Run mini-game module, a join/quit message module, and a scoreboard module. Here's how one might choose to enable them based on their different servers:

Hub Server: `Custom Messages, Scoreboard`

Survival Server: `Custom Messages`

TNT Run Server: `TNT Run, Custom Messages, Scoreboard`


## Understanding Our APIs
There are 3 important APIs in VexelCore.

### Main API
Contains the basic module management system and some general utilities.

### Bukkit API
Contains an extended version of the module management system to work with Bukkit along with additional utilities.

### Velocity API
Contains an extended version of the module management system to work with Velocity along with additional utilities.

## Project Goals
- Have a common module system for multiple platforms. ✅
- Provide useful utilities for developers.
  - General useful methods ✅
  - UUID & username+UUID validation ✅
  - Icons ✅
  - Simple web requests ✅
  - Debugging tools
- Have modules be loaded in at runtime from jar files (help wanted).
- Allow modules to load their dependencies at runtime (help wanted).
- Allow modules to talk with each other and call their methods.
- Allow modules to have dependencies for plugins and other modules.