/*
 * Copyright 2026 Dawson Hessler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
