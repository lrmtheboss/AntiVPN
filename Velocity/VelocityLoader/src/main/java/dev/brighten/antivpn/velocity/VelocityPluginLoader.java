package dev.brighten.antivpn.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.brighten.antivpn.loader.JarInJarClassLoader;
import dev.brighten.antivpn.loader.LoaderBootstrap;
import org.bstats.velocity.Metrics;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Plugin(id = "kaurivpn", name = "KauriVPN", version = "1.7.1", authors = {"funkemunky"})
public class VelocityPluginLoader {

    private static final String JAR_NAME = "antivpn-velocity.jarinjar";
    private static final String SOURCE_NAME = "antivpn-source.jarinjar";
    private static final String BOOTSTRAP_CLASS = "dev.brighten.antivpn.velocity.VelocityPlugin";

    private final LoaderBootstrap plugin;

    @Inject
    public VelocityPluginLoader(ProxyServer server, Logger logger, @DataDirectory Path path, Metrics.Factory metricsFactory) {
        Map<Class<?>, Object> instances = new HashMap<>();
        instances.put(ProxyServer.class, server);
        instances.put(Logger.class, logger);
        instances.put(Path.class, path);
        instances.put(String.class, metricsFactory);
        instances.put(LoaderBootstrap.class, this);
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME, SOURCE_NAME);
        this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, Map.class, instances);
        plugin.onLoad(path.toFile());
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        plugin.onEnable();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        plugin.onDisable();
    }

}
