package dev.wrrulos.rFakeProxy.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.proxy.server.ServerPing.Version;
import com.velocitypowered.api.util.Favicon;
import dev.wrrulos.rFakeProxy.RFakeProxy;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ProxyPing {
    private final ProxyServer server;
    private final Logger logger;
    private final RFakeProxy plugin;

    /**
     * Constructor for the ProxyPing class
     * @param server The proxy server
     * @param logger The logger
     * @param plugin The plugin
     */
    public ProxyPing(ProxyServer server, Logger logger, RFakeProxy plugin) {
        this.server = server;
        this.logger = logger;
        this.plugin = plugin;
    }

    /**
     * Modify the ping response to match the target server
     * @param event The ProxyPingEvent
     */
    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        // Get the ping builder
        ServerPing.Builder builder = event.getPing().asBuilder();

        // Initialize the variables
        Component motd;
        Version version;
        int protocol;
        List<ServerPing.SamplePlayer> samplePlayers = new ArrayList<>();
        int onlinePlayers;
        int maxPlayers;
        Favicon favicon;

        // Get the target server ping
        CompletableFuture<ServerPing> futurePing = server.getServer("lobby").get().ping();
        // Get the plugin configuration
        ConfigurationNode config = plugin.getConfig();

        // This mode is for updating the ping response to match the target server
        if (config.node("updateMode").getString("1").equals("1")) {
            try {
                ServerPing serverPing = futurePing.get();
                motd = serverPing.getDescriptionComponent();

                if (serverPing.getPlayers().isPresent()) {
                    onlinePlayers = serverPing.getPlayers().get().getOnline();
                    maxPlayers = serverPing.getPlayers().get().getMax();
                    samplePlayers = new ArrayList<>(serverPing.getPlayers().get().getSample());
                    builder.onlinePlayers(onlinePlayers);
                    builder.maximumPlayers(maxPlayers);
                    builder.samplePlayers(samplePlayers.toArray(new ServerPing.SamplePlayer[0]));
                }

                if (config.node("protocolMode").getString("1").equals("1")) {
                    protocol = serverPing.getVersion().getProtocol();  // Backend server protocol
                } else {
                    protocol = event.getPing().getVersion().getProtocol(); // Client protocol
                }

                version = new Version(protocol, serverPing.getVersion().getName());
                builder.version(version);
                serverPing.getFavicon().ifPresent(builder::favicon);
                builder.description(motd);

            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error pinging target server", e);
            }
        } else {
            // This mode is for using the config values to update the ping response
            String motdString = config.node("motd").getString("A Minecraft Server");
            motd = MiniMessage.miniMessage().deserialize(motdString);

            if (config.node("protocolMode").getString("1").equals("1")) {
                protocol = config.node("protocol").getInt(47);  // Backend server protocol
            } else {
                protocol = event.getPing().getVersion().getProtocol(); // Client protocol
            }

            version = new Version(protocol, config.node("version").getString("Spigot 1.8.8"));
            onlinePlayers = config.node("onlinePlayers").getInt(0);
            maxPlayers = config.node("maxPlayers").getInt(1);
            String faviconBase64 = config.node("favicon").getString("");

            if (!faviconBase64.isEmpty()) {
                if (faviconBase64.startsWith("data:image/png;base64,")) {
                    faviconBase64 = faviconBase64.substring("data:image/png;base64,".length());
                }

                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(faviconBase64);
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
                    BufferedImage image = ImageIO.read(inputStream);
                    favicon = Favicon.create(image);
                    builder.favicon(favicon);
                } catch (IOException | IllegalArgumentException e) {
                    logger.error("Favicon error (base64)", e);
                }
            }

            builder.description(motd);
            builder.version(version);
            builder.onlinePlayers(onlinePlayers);
            builder.maximumPlayers(maxPlayers);
            builder.samplePlayers(samplePlayers.toArray(new ServerPing.SamplePlayer[0]));
        }

        event.setPing(builder.build());
    }
}
