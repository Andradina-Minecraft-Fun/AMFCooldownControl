package br.com.centralandradina.amfcooldowncontrol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public final class AMFCooldownControl extends JavaPlugin implements Listener
{
    // Armazena o player que está em cooldown
    static HashSet<Player> cooldownAttackList = new HashSet<>();

    /**
     * On load do plugin
     */
    @Override
    public void onEnable() {
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
                attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cWait for next hit!"));
                event.setCancelled(true);
            }
            else {

                // Se o atacante não está na lista de cooldown, adiciona ele à lista, e procede com o ataque normalmente
                cooldownAttackList.add(attacker);

                // @todo Calcular o attack speedy em segundos, baseado nos atributos do jogador, e bonus da arma
                long delay = 1;

                // Timer para remover o atacante da lista de cooldown
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dCooldown finish"));
                        cooldownAttackList.remove(attacker);
                    }
                }.runTaskLater(this, delay * 20);

            }
        }
    }

    public String _color(String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
