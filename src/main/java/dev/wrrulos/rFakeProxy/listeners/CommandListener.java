package dev.wrrulos.rFakeProxy.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;


public class CommandListener {
    /**
     * Constructor for the onPlayerCommand class
     * @param event The CommandExecuteEvent
     */
    @Subscribe
    public void onPlayerCommand(CommandExecuteEvent event) {
        // Check if the command is executed by a player
        if (event.getCommandSource() instanceof Player) {
            String playerName = ((Player) event.getCommandSource()).getUsername();
            String playerIP = ((Player) event.getCommandSource()).getRemoteAddress().getAddress().getHostAddress();
            System.out.println("[RFakeProxy] " + "[COMMAND] " + playerName + " " + playerIP + " " + event.getCommand());
        }
    }
}
