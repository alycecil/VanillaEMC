![](https://i.imgur.com/vOPofQQ.jpeg "VanillaEMC Banner")

# Dissolver

_Dissolver (originally VanillaEMC) is a Minecraft Fabric mod that adds the ability to convert items into any other stored items based on its value (EMC)._

## Version

Currently working on Minecraft 1.21! Because this is Fabric you will also need the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)!

You are free to include this in a modpack.

## Inspiration

This mod is inspired by the Transmutation Table from ProjectE/Equivalent Exchange, I could not find any similar mods for newer versions of Minecraft, and I wanted something simple with just the transmutation aspect, so I decided to try and make my own. :)

# How does it work?

Each item will be a unique EMC value, some have a fixed value, but most of the values are dynamically added based on the crafting recipe, meaning other mod items might also have EMC values. It's also multiplayer compatible!

Here is an example: Dirt has the EMC value 1, and a Diamond has the value 4200, so you will need 4200 Dirt to get one Diamond, but you can also convert the other way.

### The Dissolver:

The main block of this mod is called "Dissolver" because it _dissolves_ the items, to begin with there will be no items stored in its database, but when you add items into this block it will remember it for later & also add the EMC value. It works almost like an Ender Chest because the data is the same anywhere in the world, but the data is also shared with other players unless you turn on [Private mode](#mod-config). (It can be broken with a stone pickaxe or better.)

### The Crystal Frame:

The other block of this mod is called "Crystal Frame", it's used to craft the Dissolver, but you can also use it to remotely access the Dissolver inventory if you are within 40 blocks of one.

# Screenshots

![screenshot](https://i.imgur.com/H9Ug8rE.png)
![screenshot](https://i.imgur.com/277hqs7.png)

# Crafting

Here's how you craft the "Crystal Frame":

![screenshot](https://i.imgur.com/6yI94wB.png)

And here's how the default recipe for the "Dissolver" is, it needs a Nether Star because this can be a very OP item:

![screenshot](https://i.imgur.com/nCiWKMZ.png)

But if you want an easier recipe you can tweak the recipe difficulty in the [Mod Confg](#mod-config), here's "normal" difficulty:

![screenshot](https://i.imgur.com/525sk7u.png)

And here's "easy" difficulty (useful for skyblock):

![screenshot](https://i.imgur.com/w8UpOF9.png)

# Mod Config

-   `emc_on_hud=*false*|true`: Display current EMC on HUD (top left corner)
-   `private_emc=*false*|true`: Should each player have their own EMC storage?
-   `creative_items=*false*|true`: Should creative items have EMC?
-   `difficulty=easy|normal|*hard*`: Changes crafting recipe for Dissolver block.
-   `mode=*default*|skyblock`: Changes some EMC values.
-   `emc:{id}={number}: Set custom EMC values on any item! (Example: emc:minecraft:dirt=50)

# Mod Commands

-   `/emc`: Get player EMC
-   `/emc list`: List all players & their EMC
-   `/emc give {amount} ({player})`: Give EMC to a player
-   `/emc take {amount} ({player})`: Take EMC from a player
-   `/emc set {amount} ({player})`: Set EMC for a player
-   `/emcmemory fill`: Store all items in the Dissolver!
-   `/emcmemory clear`: Forget all items stored in the Dissolver!
-   `/emcmemory add {item}`: Store a specific item in the Dissolver!
-   `/emcmemory remove {item}`: Remove a specific item from the Dissolver!
-   `/opendissolver`: Open the Dissolver screen if a block is within 40 blocks.

# License

Dissolver is licensed under the CC0-1.0 license. Read [more here](https://github.com/vassbo/Dissolver/blob/main/LICENSE).
