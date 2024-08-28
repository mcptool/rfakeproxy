package dev.wrrulos.rFakeProxy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageUtil {
    public static Component createMessage(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }
}
