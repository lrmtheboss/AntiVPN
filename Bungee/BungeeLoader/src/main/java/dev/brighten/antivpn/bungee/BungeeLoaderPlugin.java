package dev.brighten.antivpn.bungee;

import dev.brighten.antivpn.loader.JarInJarClassLoader;
import dev.brighten.antivpn.loader.LoaderBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLoaderPlugin extends Plugin {

    private static final String JAR_NAME = "antivpn-bungee.jarinjar";
    private static final String SOURCE_NAME = "antivpn-source.jarinjar";
    private static final String BOOTSTRAP_CLASS = "dev.brighten.antivpn.bungee.BungeePlugin";

    private final LoaderBootstrap plugin;

    public BungeeLoaderPlugin() {
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME, SOURCE_NAME);
        this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, Plugin.class, this);
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
