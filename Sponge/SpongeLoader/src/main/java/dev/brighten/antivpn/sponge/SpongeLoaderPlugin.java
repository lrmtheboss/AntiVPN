package dev.brighten.antivpn.sponge;

import com.google.inject.Inject;
import dev.brighten.antivpn.loader.JarInJarClassLoader;
import dev.brighten.antivpn.loader.LoaderBootstrap;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Map;

@Plugin("kaurivpn")
public class SpongeLoaderPlugin {

    private static final String JAR_NAME = "antivpn-sponge.jarinjar";
    private static final String SOURCE_NAME = "antivpn-source.jarinjar";
    private static final String BOOTSTRAP_CLASS = "dev.brighten.antivpn.bungee.BungeePlugin";

    private final LoaderBootstrap plugin;

    @Inject
    private PluginContainer container;
    @Inject
    private Logger logger;

    public SpongeLoaderPlugin() {
        Map<Class<?>, Object> instances = Map.of(PluginContainer.class, container, Logger.class, logger);
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME, SOURCE_NAME);
        this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, Map.class, instances);

        ConfigManager configManager = Sponge.configManager();

        var path = configManager.sharedConfig(container).directory();

        this.plugin.onLoad(path.toFile());
    }

    @Listener
    public void onConstruct(final ConstructPluginEvent event) {
        this.plugin.onEnable();
    }

    @Listener
    public void onServer(final StoppingEngineEvent<Server> event) {
        this.plugin.onDisable();
    }


}
