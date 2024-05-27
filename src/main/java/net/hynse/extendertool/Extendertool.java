package net.hynse.extendertool;

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
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

public final class Extendertool extends JavaPlugin implements Listener {

    private static final String CUSTOM_TOOL_KEY = "extendertool_item";
    private static final String RAW_ZINC_KEY = "raw_zinc";
    private static final String ZINC_KEY = "zinc";
    private static final String BRASS_INGOT_KEY = "brass_ingot";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        craftExtenderTool();
        smithingBrassIngot();
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
            meta.setRarity(ItemRarity.EPIC);
            meta.setMaxStackSize(1);

            NamespacedKey toolKey = new NamespacedKey(this, CUSTOM_TOOL_KEY);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(toolKey, PersistentDataType.BYTE, (byte) 1);
            extendertool.setItemMeta(meta);
        }
        return extendertool;
    }

    private ItemStack createRawZincItem() {
        ItemStack rawzinc = new ItemStack(Material.RAW_IRON);
        ItemMeta meta = rawzinc.getItemMeta();
        final int CustomModelData = 86002;
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(this, RAW_ZINC_KEY), PersistentDataType.BYTE, (byte) 1);
            meta.setCustomModelData(CustomModelData);
            meta.displayName(Component.text("Raw Zinc").decoration(TextDecoration.ITALIC, false));
            meta.setMaxStackSize(64);
            rawzinc.setItemMeta(meta);
        }
        return rawzinc;
    }
    private ItemStack createZincItem() {
        ItemStack zinc = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = zinc.getItemMeta();
        final int CustomModelData = 86003;
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(this, ZINC_KEY), PersistentDataType.BYTE, (byte) 1);
            meta.setCustomModelData(CustomModelData);
            meta.displayName(Component.text("Zinc").decoration(TextDecoration.ITALIC, false));
            meta.setMaxStackSize(64);
            zinc.setItemMeta(meta);
        }
        return zinc;
    }

    private ItemStack createBrassIngotItem() {
        ItemStack brassIngot = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = brassIngot.getItemMeta();
        final int CustomModelData = 86004;
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(this, BRASS_INGOT_KEY), PersistentDataType.BYTE, (byte) 1);
            meta.setCustomModelData(CustomModelData);
            meta.displayName(Component.text("Brass Ingot").decoration(TextDecoration.ITALIC, false));
            meta.setMaxStackSize(64);
            brassIngot.setItemMeta(meta);
        }
        return brassIngot;
    }

    private void craftExtenderTool() {
        NamespacedKey recipeKey = new NamespacedKey(this, "extender_tool_recipe");
        ItemStack brassIngot = createBrassIngotItem();
        ItemStack recoveryCompass = new ItemStack(Material.RECOVERY_COMPASS);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createExtenderToolItem());
        recipe.shape("XOX", "XKX", "XKX");
        recipe.setIngredient('X', new RecipeChoice.ExactChoice(brassIngot));
        recipe.setIngredient('O', recoveryCompass);
        recipe.setIngredient('K', Material.BREEZE_ROD);

        Bukkit.addRecipe(recipe);
    }

    private void smithingBrassIngot() {
        NamespacedKey recipeKey = new NamespacedKey(this, "brass_ingot_recipe");

        ItemStack result = createBrassIngotItem();
        RecipeChoice base = new RecipeChoice.ExactChoice(createZincItem());
        RecipeChoice template = new RecipeChoice.ExactChoice(createZincItem());
        RecipeChoice addition = new RecipeChoice.ExactChoice(new ItemStack(Material.COPPER_INGOT));

        SmithingRecipe smithingRecipe = new SmithingTransformRecipe (recipeKey, result, template, base, addition);
        getServer().addRecipe(smithingRecipe);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NamespacedKey extendertoolrecipeKey = new NamespacedKey(this, "extender_tool_recipe");
        NamespacedKey brassingotrecipeKey = new NamespacedKey(this, "brass_ingot_recipe");
        player.discoverRecipe(extendertoolrecipeKey);
        player.discoverRecipe(brassingotrecipeKey);
    }


    @EventHandler
    public void onZincDrop(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.DIORITE) {
            if (Math.random() < 0.005) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), createRawZincItem());
            }
        }
    }

    @EventHandler
    public void onBrassIngotCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        NamespacedKey extenderToolKey = new NamespacedKey(this, "extender_tool_recipe");
        Recipe recipe = event.getRecipe();

        if (recipe != null && recipe instanceof ShapedRecipe && ((ShapedRecipe) recipe).getKey().equals(extenderToolKey)) {
            return;
        }

        if (matrix.length == 9) {
            for (ItemStack item : matrix) {
                if (item != null && item.getType() == Material.GOLD_INGOT) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(this, BRASS_INGOT_KEY), PersistentDataType.BYTE)) {
                        event.getInventory().setResult(null);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onZincCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (matrix.length == 9) {
            for (ItemStack item : matrix) {
                if (item != null && item.getType() == Material.RAW_IRON) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(this, RAW_ZINC_KEY), PersistentDataType.BYTE)) {
                        event.getInventory().setResult(null);
                        return;
                    }
                }
            }
        }
    }


    @EventHandler
    public void onZincSmelt(FurnaceSmeltEvent event) {
        ItemStack item = event.getSource();
        if (item != null && item.getType() == Material.RAW_IRON) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(this, RAW_ZINC_KEY), PersistentDataType.BYTE)) {
                event.setResult(createZincItem());
            }
        }
    }



}
