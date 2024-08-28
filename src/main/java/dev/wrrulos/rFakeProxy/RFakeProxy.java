package dev.wrrulos.rFakeProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.wrrulos.rFakeProxy.listeners.ChatListener;
import dev.wrrulos.rFakeProxy.listeners.CommandListener;
import dev.wrrulos.rFakeProxy.listeners.ConnectionListener;
import dev.wrrulos.rFakeProxy.listeners.ProxyPing;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.ConfigurationNode;
import org.slf4j.Logger;

import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Plugin(
    id = "rfakeproxy",
    name = "RFakeProxy",
    version = BuildConstants.VERSION,
    description = "Plugin created for MCPTool fakeproxy command",
    url = "wrrulos.dev",
    authors = {"Pedro Agustin Vega"}
)
public class RFakeProxy {
    private final ProxyServer server;
    private static String adminKey;
    private static List<String> admins = new ArrayList<>();

    @Inject
    private Logger logger;

    private ConfigurationNode config;
    private final Path configPath = Paths.get("plugins/RFakeProxy/config.yml");
    /**
     * Constructor for the RFakeProxy class
     * @param server The proxy server
     * @param logger The logger
     */
    @Inject
    public RFakeProxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        loadConfig();

        // Generate the admin key (random string of 10 characters)
        RFakeProxy.adminKey = generateRandomString(10);
        System.out.println("[RFakeProxy] " + "[ADMINKEY] " + adminKey);
    }

    /**
     * Register the ProxyPing listener when the proxy is initialized
     * @param event The ProxyInitializeEvent
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        EventManager eventManager = server.getEventManager();
        eventManager.register(this, new ProxyPing(server, logger, this));
        eventManager.register(this, new ConnectionListener());
        eventManager.register(this, new ChatListener(server, logger, this));
        eventManager.register(this, new CommandListener());
        logger.info("RFakeProxy has been initialized successfully.");

        if (server.getServer("lobby").isPresent()) {
            logger.info("Target server: {}", server.getServer("lobby").get().getServerInfo().getAddress().toString().split("/")[0]);
        } else {
            logger.warn("Target server not found.");
        }
    }

    public static List<String> getAdmins() {
        return RFakeProxy.admins;
    }

    public static void addAdmin(String admin) {
        RFakeProxy.admins.add(admin);
    }

    public static String getAdminKey() {
        return RFakeProxy.adminKey;
    }

    public static String generateRandomString(int length) {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }

        return stringBuilder.toString();
    }


    /**
     * Load the configuration file
     */
    private void loadConfig() {
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                Files.copy(getClass().getResourceAsStream("/config.yml"), configPath);
            } catch (IOException e) {
                logger.error("Could not create configuration file", e);
            }
        }

        reloadConfig();
    }

    /**
     * Reload the configuration file
     */
    public void reloadConfig() {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .build();

        try {
            // Leer el archivo manualmente para verificar la codificación
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            logger.info("Archivo de configuración leído correctamente con UTF-8.");

            // Cargar la configuración YAML
            config = loader.load();

            logger.info("Configuración cargada exitosamente.");
        } catch (MalformedInputException e) {
            logger.error("Error de codificación en el archivo de configuración. Asegúrate de que está en UTF-8.", e);
        } catch (IOException e) {
            logger.error("Error al recargar la configuración desde el archivo.", e);
        } catch (Exception e) {
            logger.error("Error inesperado al recargar la configuración.", e);
        }
    }

    /**
     * Get the configuration node
     * @return The configuration node
     */
    public ConfigurationNode getConfig() {
        reloadConfig();
        return config;
    }
}
