package dev.brighten.antivpn.bukkit.loader;

import dev.brighten.antivpn.loader.JarInJarClassLoader;
import dev.brighten.antivpn.loader.LoaderBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class BukkitLoaderPlugin extends JavaPlugin {

    private static final String JAR_NAME = "antivpn-bukkit.jarinjar";
    private static final String BOOTSTRAP_CLASS = "dev.brighten.antivpn.bukkit.BukkitPlugin";

    private final LoaderBootstrap plugin;

    public BukkitLoaderPlugin() throws IOException {
        try(JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME)) {
            this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, JavaPlugin.class, this);
        }
    }

    @Override
    public void onLoad() {
        this.plugin.onLoad(getDataFolder());
    }

    @Override
    public void onEnable() {
        this.plugin.onEnable();
    }

    @Override
    public void onDisable() {
        this.plugin.onDisable();
    }


}
