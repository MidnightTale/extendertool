package net.hynse.extendertool;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class Extendertool extends JavaPlugin implements Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("extendertool")) {
                giveRangeItem(player);
                return true;
            }
        }
        return false;
    }

    private void giveRangeItem(Player player) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "Interaction Range", 5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            meta.addAttributeModifier(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, modifier);
            item.setItemMeta(meta);

            player.getInventory().addItem(item);
        }
    }
}