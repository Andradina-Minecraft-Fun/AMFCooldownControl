package br.com.centralandradina.amfcooldowncontrol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;

public final class AMFCooldownControl extends JavaPlugin implements Listener
{
    // Armazena o player que está em cooldown
    static HashSet<Player> cooldownAttackList = new HashSet<>();

    // Armazena a configuração
    public FileConfiguration config;

    /**
     * On load do plugin
     */
    @Override
    public void onEnable() {

        // Config
        config = this.getConfig();
        config.addDefault("debug", false);
        config.addDefault("baseAttackSpeed", 1.4);
        config.addDefault("CustomTagName", "AMFAttackSpeed");
        config.options().copyDefaults(true);
        saveConfig();

        // Plugin startup logic
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        // Recupera os players online
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            // Remove o cooldown dos ataques
            p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
        }

    }

    /**
     * On disable do plugin
     */
    @Override
    public void onDisable()
    {
        // Recupera os players online
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            // Retorna o cooldown dos ataques original
            p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
        }
    }

    /**
     * Disparado quando o jogador entra no servidor
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Player p = event.getPlayer();

        // Remove o cooldown do player
        p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
    }

    /**
     * Disparado quando uma entidade causa um dano em outra entidade
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();

            // Verifica se o atacante está na lista de espera do cooldown
            if (cooldownAttackList.contains(attacker)) {

                // Se o atacante está na lista de espera, cancela o hit
                _debug(attacker,"&cWait for next hit!");
                event.setCancelled(true);
            }
            else {

                // Se o atacante não está na lista de cooldown, adiciona ele à lista, e procede com o ataque normalmente
                cooldownAttackList.add(attacker);

                // Recupera o attack speed base
                double baseAttackSpeed = config.getDouble("baseAttackSpeed");

                // Verifica se o item de ataque tem a custom tag
                ItemStack item = attacker.getEquipment().getItemInMainHand();
                double weaponAttackSpeed = getAttackSpeedBonusFromItem(item);
                if(weaponAttackSpeed > 0) {
                    // baseAttackSpeed é a velocidade padrão. Então se a arma tem 2 de velocidade, é 2% a menos do baseAttackSpeed: baseAttackSpeed - ((baseAttackSpeed * weaponAttackSpeed) / 100)
                    baseAttackSpeed = (baseAttackSpeed - ((baseAttackSpeed * weaponAttackSpeed) / 100));
                    _debug(attacker,"&4Has weapon attack speed " + weaponAttackSpeed);
                }

                // @todo Verificar algum item particular no inventário (estilo totem)
                //      Adicionar no config se é utiliza ou nao este metodo para nao fazer processamento a toa

                // @todo Verificar algum item de armadura possui attack speed
                //      Adicionar no config se é utiliza ou nao este metodo para nao fazer processamento a toa

                // Timer para remover o atacante da lista de cooldown
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        _debug(attacker,"&dCooldown finish");
                        cooldownAttackList.remove(attacker);
                    }
                }.runTaskLater(this, (long)(baseAttackSpeed * 20.0));
                _debug(attacker,"&7Next hit on " + baseAttackSpeed + "s (" + (long)(baseAttackSpeed * 20.0) + " ticks)");
            }
        }
    }

    /**
     * Recupera o bonus do item
     */
    public double getAttackSpeedBonusFromItem(ItemStack item)
    {
        String customTagName = config.getString("CustomTagName");
        ItemMeta itemMeta = item.getItemMeta();

        NamespacedKey key = new NamespacedKey(this, customTagName);
        PersistentDataContainer tagContainer = itemMeta.getPersistentDataContainer();

        return tagContainer.getOrDefault(key, PersistentDataType.DOUBLE, 0.0);
    }

    /**
     * Metodos só pra debug
     */
    public String _color(String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    public void _debug(Player p, String msg)
    {
        if(config.getBoolean("debug")) {
            p.sendMessage(_color(msg));
        }
    }
}
