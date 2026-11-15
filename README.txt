Donut Pay Popup (Fabric, Minecraft 1.21.4, client-side only)

Build:
  1. Install JDK 21
  2. In this folder run:  gradle wrapper   (or use Gradle 8.10+ directly)
  3. ./gradlew build
  4. Jar is in build/libs/donutpay-1.0.0.jar  -> put in your .minecraft/mods folder
     (needs Fabric Loader + Fabric API for 1.21.4)

Usage: type /pay <player> <amount> (e.g. /pay Steve 25m) and a red -$25M slides in at the top.
