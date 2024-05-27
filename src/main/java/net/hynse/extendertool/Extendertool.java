package net.hynse.extendertool;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class Extendertool extends JavaPlugin implements Listener {

    private static final String CUSTOM_TOOL_KEY = "extendertool_item";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("extendertool")) {
                if (player.hasPermission("extendertool.give")) {
                    extendertoolitemGive(player);
                    return true;
                } else {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
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
        handleToolDurability(player, EquipmentSlot.HAND);
        handleToolDurability(player, EquipmentSlot.OFF_HAND);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        handleToolDurability(player, EquipmentSlot.HAND);
        handleToolDurability(player, EquipmentSlot.OFF_HAND);
    }

    private void handleToolDurability(Player player, EquipmentSlot slot) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);

            if (container.has(toolKey, PersistentDataType.BYTE)) {
                short currentDurability = item.getDurability();
                item.setDurability((short) (currentDurability - 1));
            }
        }
    }

