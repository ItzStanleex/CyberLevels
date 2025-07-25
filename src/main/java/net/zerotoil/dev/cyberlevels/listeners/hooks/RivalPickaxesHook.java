package net.zerotoil.dev.cyberlevels.listeners.hooks;

import net.zerotoil.dev.cyberlevels.CyberLevels;
import net.zerotoil.dev.cyberlevels.objects.exp.EXPEarnEvent;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.rivaldev.pickaxes.api.events.RivalPickaxesBlockBreakEvent;

public class RivalPickaxesHook implements Listener {

    private final CyberLevels plugin;

    public RivalPickaxesHook(final CyberLevels plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRivalBlockBreak(final RivalPickaxesBlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // prevent silk touch abuse
        if (this.plugin.expCache().isPreventSilkTouchAbuse()) {
            if (this.plugin.serverVersion() > 8 && event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))  {
                return;
            } else if (this.plugin.serverVersion() <= 8 && event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                return;
            }
        }

        if (this.plugin.expCache().isOnlyNaturalBlocks() && event.getBlock().hasMetadata("CLV_PLACED")) {

            if (!(event.getBlock().getBlockData() instanceof Ageable) || this.plugin.expCache().isIncludeNaturalCrops()) {
                return;
            }

            final Ageable ageable = (Ageable) event.getBlock().getBlockData();

            if (ageable.getAge() != ageable.getMaximumAge()) {
                return;
            }
        }

        sendExp(event.getPlayer(), this.plugin.expCache().expEarnEvents().get("rivalpick-breaking"), event.getBlock().getType().toString());

    }

    public void sendExp(Player player, EXPEarnEvent expEarnEvent, String item) {

        if (checkAbuse(player, expEarnEvent)) return;

        double counter = 0;

        if (expEarnEvent.isEnabled() && expEarnEvent.isInGeneralList(item))
            counter += expEarnEvent.getGeneralExp();

        if (expEarnEvent.isSpecificEnabled() && expEarnEvent.isInSpecificList(item))
            counter += expEarnEvent.getSpecificExp(item);

        if (counter > 0) this.plugin.levelCache().playerLevels().get(player).addExp(counter, this.plugin.levelCache().doEventMultiplier());
        else if (counter < 0) this.plugin.levelCache().playerLevels().get(player).removeExp(Math.abs(counter));
    }

    public boolean checkAbuse(Player player, EXPEarnEvent event) {
        return (this.plugin.expCache().isAntiAbuse(player, event.getCategory()));
    }






}
