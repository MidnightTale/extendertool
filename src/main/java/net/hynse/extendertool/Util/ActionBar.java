package net.hynse.extendertool.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.format.NamedTextColor;

public class ActionBar {

    public static void sendActionBar(Player player, TextComponent textComponent) {
        player.sendActionBar(textComponent);
    }


    public static Component createWarningBar(int currentValue, int maxValue, int barLength, char barCharacter, char barCharacterPlaceholder) {
        TextComponent.Builder builder = Component.text();
        int progress = (int) Math.ceil((double) currentValue / maxValue * barLength);
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                if (i < 5) {
                    builder.append(Component.text(barCharacter).color(NamedTextColor.GREEN));
                } else if (i < 8) {
                    builder.append(Component.text(barCharacter).color(NamedTextColor.YELLOW));
                } else {
                    builder.append(Component.text(barCharacter).color(NamedTextColor.RED));
                }
            } else {
                builder.append(Component.text(barCharacterPlaceholder).color(NamedTextColor.GRAY));
            }
        }
        return builder.build();
    }
    public static void sendWarningBar(Player player, Component bar) {
        sendActionBar(player, Component.text("Pressure: ").append(bar));
    }


}
