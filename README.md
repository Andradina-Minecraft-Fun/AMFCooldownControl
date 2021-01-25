# AMFCooldownControl

### Control attack speed, ignoring minecraft attack cooldown system

Minecraft damage system is nice, but we want to add bonus os items. So the problem are the [attack cooldown calculation](https://minecraft.gamepedia.com/Damage#Attack_cooldown "attack cooldown calculation").

Example: If you can do a hit every 1 second, and you hit on 0.5 second, your damage will be calculated by the table, and not will cause 100% of damage

To control that, we remove minecraft attack speed and do our attack speed system. Now,you can hit only when you can hit, and all attack cause 100% of the damage

Watch on Youtube: [![Watch on Youtube](https://img.youtube.com/vi/Ed3oYDNuxpA/maxresdefault.jpg)](https://youtu.be/Ed3oYDNuxpA)

# Hook your plugin

To create a plugin who can give attack speed, just hook `AMFCooldownControl` and add the tag that you add on config.yml

For example:

```
// ...

@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
{   
    // Get command  
    String commandName = cmd.getName().toLowerCase();

    // Get AMFCooldownControl plugin
    Plugin AMFCooldownControl = Bukkit.getServer().getPluginManager().getPlugin("AMFCooldownControl");
    
    // If is my command (/test <attack_speed_integer_value>)
    if(commandName.equals("test")) {
    
        // Get item on hand and meta
        ItemStack item = player.getEquipment().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
    
        // Add lore
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("Attack Speed " + args[0]);
        meta.setLore(lore);
    
        // Add tag with name added on config.yml
        NamespacedKey key = new NamespacedKey(AMFCooldownControl, "AMFAttackSpeed");
        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, Double.parseDouble(args[0]));
    
        // Return meta to item
        item.setItemMeta(meta);
    }
    
    return true;
}
// ...
```

# AMFCooldownControl config.yml

You can use the AMFCooldownControl without external plugin if you just want remove cooldown hits, and can ignore custom tag name. All the fields of config documentated are:

```
# If true, will show msg to player: time to next hit, weapon attack speed bonus, alert when cooldown finish and alert 
#     that hit cannot done. This will appear for every player hit
debug: false

# Default base attack speed. Without modifiers, the player will hit every baseAttackSpeed seconds
baseAttackSpeed: 1.4

# Name of custom tag name. If you will add custom tag to give attack speed bonus, this is the name tag to be added.
# Don't forget that custom tags are plugin related, so you need to add custom tag to AMFCooldownControl plugin
CustomTagName: AMFAttackSpeed

```



