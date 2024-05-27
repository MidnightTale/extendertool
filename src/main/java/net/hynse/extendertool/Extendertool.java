package net.hynse.extendertool;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

public final class Extendertool extends JavaPlugin implements Listener {

    private static final String CUSTOM_TOOL_KEY = "extendertool_item";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (command.getName().equalsIgnoreCase("extendertool")) {
                if (player.hasPermission("extendertool.give")) {
                    extendertoolitemGive(player);
                } else {
                    player.sendMessage("You do not have permission to use this command.");
                }
                return true;
            }
        }
        return false;
    }

    private void extendertoolitemGive(Player player) {
        ItemStack item = new ItemStack(Material.SHEARS);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            int Range = 5;
            String Name = "Interaction Range";
            int CustomModelData = 86001;
            AttributeModifier mainHandModifier = new AttributeModifier(UUID.randomUUID(), Name, Range, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier offHandModifier = new AttributeModifier(UUID.randomUUID(), Name, Range, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);

            meta.addAttributeModifier(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, mainHandModifier);
            meta.addAttributeModifier(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, offHandModifier);
            meta.setCustomModelData(CustomModelData);

            NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(toolKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);

            player.getInventory().addItem(item);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            handleToolDurability(player, EquipmentSlot.HAND);
            handleToolDurability(player, EquipmentSlot.OFF_HAND);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE) {
            handleToolDurability(player, EquipmentSlot.HAND);
            handleToolDurability(player, EquipmentSlot.OFF_HAND);
        }
    }
    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (isextendertool(mainHandItem) || isextendertool(offHandItem)) {
            event.setCancelled(true);
        }
    }


    private void handleToolDurability(Player player, EquipmentSlot slot) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);

            if (container.has(toolKey, PersistentDataType.BYTE)) {
                Damageable damageable = (Damageable) meta;
                int currentDamage = damageable.getDamage();

                if (currentDamage >= item.getType().getMaxDurability()) {
                    player.getInventory().setItem(slot, null);
                    player.getWorld().spawnParticle(Particle.WAX_OFF, player.getLocation(), 100);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 1.0f);
                } else if (shouldLoseDurability(item)) {
                    damageable.setDamage(currentDamage - 1);
                    item.setItemMeta(meta);
                }
            }
        }
    }

    private boolean shouldLoseDurability(ItemStack item) {
        if (item.containsEnchantment(Enchantment.UNBREAKING)) {
            int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            Random random = new Random();
            return random.nextInt(100) >= calculateUnbreakingChance(unbreakingLevel);
        }
        return true;
    }

    private int calculateUnbreakingChance(int unbreakingLevel) {
        if (unbreakingLevel > 0) {
            return 100 / (unbreakingLevel + 1);
        }
        return 100;
    }
    private boolean isextendertool(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);
            return container.has(toolKey, PersistentDataType.BYTE);
        }
        return false;
    }
}
