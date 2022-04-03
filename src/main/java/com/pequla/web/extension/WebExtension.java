package com.pequla.web.extension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pequla.web.extension.models.*;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import spark.Spark;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public final class WebExtension extends JavaPlugin {

    private final ObjectMapper mapper;

    public WebExtension() {
        saveDefaultConfig();
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onEnable() {
        Spark.port(getConfig().getInt("port"));

        Spark.before((request, response) ->
                getLogger().info(request.requestMethod() + " [" + request.url() + "] from: " + request.ip()));

        Spark.after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
            response.type("application/json");
        });

        getLogger().info("Enabling status endpoint");
        Spark.get("/api/status", (request, response) -> {
            PlayerStatus status = new PlayerStatus();
            status.setMax(getServer().getMaxPlayers());

            HashSet<PlayerData> list = new HashSet<>();
            getServer().getOnlinePlayers().forEach(player -> {
                PlayerData data = new PlayerData();
                data.setName(player.getName());
                data.setId(player.getUniqueId().toString());
                list.add(data);
            });

            status.setOnline(list.size());
            status.setList(list);

            List<String> plugins = Arrays.stream(getServer().getPluginManager().getPlugins())
                    .map(Plugin::getName)
                    .collect(Collectors.toList());

            World world = getServer().getWorlds().get(0);
            WorldData wd = new WorldData();
            wd.setSeed(world.getSeed());
            wd.setTime(world.getTime());
            wd.setType(getServer().getWorldType());

            ServerStatus ss = new ServerStatus();
            ss.setPlayers(status);
            ss.setPlugins(plugins);
            ss.setWorld(wd);
            ss.setVersion(getServer().getVersion());
            return mapper.writeValueAsString(ss);
        });

        if (getConfig().getBoolean("rcon.enabled")) {
            getLogger().info("Enabling remote command execution endpoint");
            Spark.post("/api/execute", (request, response) -> {
                String key = request.headers("x-api-key");
                if (key != null && key.equals(getConfig().getString("rcon.key"))) {
                    RemoteCommand remote = mapper.readValue(request.body(), RemoteCommand.class);
                    getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                        CommandSender sender = getServer().getConsoleSender();
                        getServer().dispatchCommand(sender, remote.getCommand());
                        getLogger().info("Executed command: " + remote.getCommand());
                    });
                    response.status(204);
                    return "lmao";
                }
                response.status(401);
                SparkError error = new SparkError();
                error.setMessage("Invalid token");
                error.setTimestamp(System.currentTimeMillis());
                return mapper.writeValueAsString(error);
            });
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Stopping webserver");
        Spark.stop();
    }
}
