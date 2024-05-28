package net.hynse.extendertool;

import me.nahu.scheduler.wrapper.FoliaWrappedJavaPlugin;
import me.nahu.scheduler.wrapper.runnable.WrappedRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class Extendertool extends FoliaWrappedJavaPlugin implements Listener {

    private static final String CUSTOM_TOOL_KEY = "extendertool_item";
    private final Map<ItemStack, Boolean> isAnimatingMap = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        craftExtenderTool();
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
        final ItemStack item = new ItemStack(Material.SHEARS);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            player.getInventory().addItem(createExtenderToolItem());
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

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);

        if (isextendertool(firstItem) || isextendertool(secondItem)) {
            if (secondItem != null && secondItem.getType() == Material.WIND_CHARGE) {
                ItemMeta meta = firstItem.getItemMeta();
                if (meta instanceof Damageable damageable) {
                    int currentDamage = damageable.getDamage();
                    damageable.setDamage(Math.max(0, currentDamage - 128));
                    firstItem.setItemMeta(meta);
                    event.setResult(firstItem);
                }
            } else {
                event.setResult(null);
            }
        } else if ((firstItem != null && firstItem.getType() == Material.SHEARS) || (secondItem != null && secondItem.getType() == Material.SHEARS)) {
            event.setResult(null);
        }
    }



    private void handleToolDurability(Player player, EquipmentSlot slot) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            final NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);

            if (container.has(toolKey, PersistentDataType.BYTE)) {
                Damageable damageable = (Damageable) meta;
                int currentDamage = damageable.getDamage();

                if (currentDamage >= item.getType().getMaxDurability()) {
                    player.getInventory().setItem(slot, null);
                    player.getWorld().spawnParticle(Particle.WAX_OFF, player.getLocation(), 100);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.6f, 1.0f);
                } else if (shouldLoseDurability(item)) {
                    damageable.setDamage(currentDamage + 1);
                    item.setItemMeta(meta);
                    animateTool(item);
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
            final NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);
            return container.has(toolKey, PersistentDataType.BYTE);
        }
        return false;
    }


    private ItemStack createExtenderToolItem() {
        ItemStack extendertool = new ItemStack(Material.SHEARS);
        ItemMeta meta = extendertool.getItemMeta();
        final int CustomModelData = 86003;
        final int Range = 7;
        final double Attackspeed = -1.6;
        final String Name = "Interaction Range";
        if (meta != null) {
            AttributeModifier mainHandModifier = new AttributeModifier(UUID.randomUUID(), Name, Range, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier offHandModifier = new AttributeModifier(UUID.randomUUID(), Name, Range, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            AttributeModifier AttackmainHandModifier = new AttributeModifier(UUID.randomUUID(), Name, Attackspeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier AttackoffHandModifier = new AttributeModifier(UUID.randomUUID(), Name, Attackspeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);

            meta.addAttributeModifier(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, mainHandModifier);
            meta.addAttributeModifier(Attribute.PLAYER_BLOCK_INTERACTION_RANGE, offHandModifier);
            meta.addAttributeModifier(Attribute.PLAYER_ENTITY_INTERACTION_RANGE, mainHandModifier);
            meta.addAttributeModifier(Attribute.PLAYER_ENTITY_INTERACTION_RANGE, offHandModifier);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttackmainHandModifier);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttackoffHandModifier);
            meta.setCustomModelData(CustomModelData);
            meta.displayName(Component.text("Extender Tool").decoration(TextDecoration.ITALIC, false));
            NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(toolKey, PersistentDataType.BYTE, (byte) 1);
            extendertool.setItemMeta(meta);
        }
        return extendertool;
    }

    private void craftExtenderTool() {
        NamespacedKey recipeKey = new NamespacedKey(this, "extender_tool_recipe");

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createExtenderToolItem());
        recipe.shape("OX ", "XKX", " XK");
        recipe.setIngredient('X', new RecipeChoice.ExactChoice(new ItemStack(Material.COPPER_BLOCK)));
        recipe.setIngredient('O', new ItemStack(Material.POWDER_SNOW));
        recipe.setIngredient('K', new RecipeChoice.ExactChoice(new ItemStack(Material.BREEZE_ROD)));

        Bukkit.addRecipe(recipe);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NamespacedKey extendertoolrecipeKey = new NamespacedKey(this, "extender_tool_recipe");
        player.discoverRecipe(extendertoolrecipeKey);
    }

    private void animateTool(ItemStack item) {
        if (isAnimatingMap.getOrDefault(item, false)) {
            return;
        }

        isAnimatingMap.put(item, true);

        new WrappedRunnable() {
            private int customModelData = 86004;
            private boolean incrementing = true;

            @Override
            public void run() {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (incrementing) {
                        if (customModelData <= 86016) {
                            meta.setCustomModelData(customModelData);
                            customModelData++;
                        } else {
                            incrementing = false;
                            customModelData--;
                        }
                    } else {
                        if (customModelData >= 86003) {
                            meta.setCustomModelData(customModelData);
                            customModelData--;
                        } else {
                            this.cancel();
                            isAnimatingMap.put(item, false);
                        }
                    }
                    item.setItemMeta(meta);
                }
            }
        }.runTaskTimer(this, 1, 1);
    }
}
