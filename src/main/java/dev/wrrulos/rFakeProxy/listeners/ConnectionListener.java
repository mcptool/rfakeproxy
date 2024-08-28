package dev.wrrulos.rFakeProxy.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;

public class ConnectionListener {
    /**
     * Constructor for the onServerConnection class
     * @param server The proxy server
     */
    @Subscribe
    public void onServerConnection(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        String playerIP = player.getRemoteAddress().getAddress().getHostAddress();
        System.out.println("[RFakeProxy] " + "[CONNECTING] " + playerName + " " + playerIP);
    }

    /**
     * Constructor for the onServerDisconnect class
     * @param server The proxy server
     */
    @Subscribe
    public void onServerDisconnection(DisconnectEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        String playerIP = player.getRemoteAddress().getAddress().getHostAddress();
        System.out.println("[RFakeProxy] " + "[DISCONNECTING] " + playerName + " " + playerIP);
    }
}
