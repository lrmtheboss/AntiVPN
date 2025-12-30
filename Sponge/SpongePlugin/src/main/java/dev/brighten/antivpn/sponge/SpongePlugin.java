package dev.brighten.antivpn.sponge;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.loader.LoaderBootstrap;
import dev.brighten.antivpn.sponge.command.SpongeCommand;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.util.Map;

@Getter
public class SpongePlugin implements LoaderBootstrap {

    //Plugin init
    @Getter
    private static SpongePlugin instance;

    private Logger logger;
    private PluginContainer container;
    private final Map<Class<?>, Object> objects;
    private File dataFolder;


    public SpongePlugin(Map<Class<?>, Object> objects) {
        this.objects = objects;
    }

    @Listener
    public void onRegisterRawCommands(final RegisterCommandEvent<Command.Raw> event){
        AntiVPN.getInstance().getExecutor().log("Registering commands...");
        for (dev.brighten.antivpn.command.Command command : AntiVPN.getInstance().getCommands()) {
            AntiVPN.getInstance().getExecutor().log("Registering command %s...", command.name());
            event.register(this.container, new SpongeCommand(command), command.name(), command.aliases());
        }
    }

    @Override
    public void onLoad(File dataFolder) {
        this.dataFolder = dataFolder;
        container = (PluginContainer) objects.get(PluginContainer.class);
        logger = (Logger) objects.get(Logger.class);
        Sponge.eventManager().registerListeners(this.container, this);
    }

    @Override
    public void onEnable() {
        instance = this;

        SpongeListener spongeListener = new SpongeListener();

        AntiVPN.start(spongeListener, new SpongePlayerExecutor(), dataFolder);
    }

    @Override
    public void onDisable() {
        AntiVPN.getInstance().getExecutor().disablePlugin();
    }
}
