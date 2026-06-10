package me.desire.bridge;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DesireBridge extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {
    private File rubiesFile;
    private FileConfiguration rubies;

    @Override
    public void onEnable() {
        loadRubies();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new DesireExpansion(this).register();
            getLogger().info("PlaceholderAPI placeholders registered.");
        }

        if (getCommand("points") != null) {
            getCommand("points").setExecutor(this);
            getCommand("points").setTabCompleter(this);
        }
        if (getCommand("rubies") != null) {
            getCommand("rubies").setExecutor(this);
            getCommand("rubies").setTabCompleter(this);
        }
    }

    private void loadRubies() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        rubiesFile = new File(getDataFolder(), "rubies.yml");
        rubies = YamlConfiguration.loadConfiguration(rubiesFile);
    }

    private void saveRubies() {
        try {
            rubies.save(rubiesFile);
        } catch (IOException exception) {
            getLogger().warning("Could not save rubies.yml: " + exception.getMessage());
        }
    }

    public long getRubies(OfflinePlayer player) {
        if (player == null || player.getUniqueId() == null) {
            return 0;
        }
        return rubies.getLong("players." + player.getUniqueId(), 0L);
    }

    private void setRubies(OfflinePlayer player, long amount) {
        if (player == null || player.getUniqueId() == null) {
            return;
        }
        rubies.set("players." + player.getUniqueId(), Math.max(0L, amount));
        saveRubies();
    }

    private void addRubies(OfflinePlayer player, long amount) {
        setRubies(player, getRubies(player) + amount);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String rawMessage = event.getMessage();
        boolean global = rawMessage.startsWith("!");
        String message = global ? rawMessage.substring(1).stripLeading() : rawMessage;
        if (message.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        Bukkit.getScheduler().runTask(this, () -> {
            if (!player.isOnline()) {
                return;
            }

            String prefix = trimFormatting(getLuckPermsPrefix(player));
            String clan = trimFormatting(getClanDisplay(player));
            String nick = color("&f" + player.getName());
            String scope = global ? color(" &7(&6G&7)") : color(" &7(&#55dfffL&7)");
            String formatted = joinParts(prefix, clan, nick) + scope + color(" &7| &f") + message;
            List<Player> recipients = global
                ? Bukkit.getOnlinePlayers().stream().collect(Collectors.toList())
                : Bukkit.getOnlinePlayers().stream()
                    .filter(target -> target.getWorld().equals(player.getWorld()))
                    .filter(target -> target.getLocation().distanceSquared(player.getLocation()) <= 120.0 * 120.0)
                    .collect(Collectors.toList());
            for (Player recipient : recipients) {
                recipient.sendMessage(formatted);
            }
            Bukkit.getConsoleSender().sendMessage(formatted);
        });
    }

    private String joinParts(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(part.trim());
        }
        return builder.toString();
    }

    private String trimFormatting(String value) {
        return value == null ? "" : value.trim();
    }

    public String getClanName(Player player) {
        if (player == null) {
            return "";
        }

        try {
            Plugin clanSystem = Bukkit.getPluginManager().getPlugin("ClanSystem");
            if (clanSystem == null || !clanSystem.isEnabled()) {
                return "";
            }

            Object manager = clanSystem.getClass().getMethod("getClanManager").invoke(clanSystem);
            Method getPlayerClan = manager.getClass().getMethod("getPlayerClan", Player.class);
            Object clanName = getPlayerClan.invoke(manager, player);
            if (clanName == null || clanName.toString().isBlank()) {
                return "";
            }

            return clanName.toString();
        } catch (ReflectiveOperationException exception) {
            return "";
        }
    }

    public String getClanDisplay(Player player) {
        String clanTag = getClanTag(player);
        if (clanTag.isEmpty()) {
            return "";
        }

        return " &8[&#95090c" + clanTag + "&8]";
    }

    public String getClanTag(Player player) {
        String clanName = getClanName(player);
        if (clanName.isEmpty()) {
            return "";
        }

        try {
            Plugin clanSystem = Bukkit.getPluginManager().getPlugin("ClanSystem");
            Object manager = clanSystem.getClass().getMethod("getClanManager").invoke(clanSystem);
            Object clan = manager.getClass().getMethod("getClan", String.class).invoke(manager, clanName);
            if (clan != null) {
                Object tag = clan.getClass().getMethod("getTag").invoke(clan);
                if (tag != null && !tag.toString().isBlank()) {
                    return tag.toString();
                }
            }
        } catch (ReflectiveOperationException ignored) {
            return clanName;
        }

        return clanName;
    }

    private String getLuckPermsPrefix(Player player) {
        try {
            Class<?> luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
            @SuppressWarnings({"rawtypes", "unchecked"})
            RegisteredServiceProvider<?> registration = Bukkit.getServicesManager().getRegistration((Class) luckPermsClass);
            if (registration == null) {
                return "";
            }

            Object luckPerms = registration.getProvider();
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUniqueId());
            if (user == null) {
                return "";
            }

            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            Object prefix = metaData.getClass().getMethod("getPrefix").invoke(metaData);
            return prefix == null ? "" : prefix.toString();
        } catch (ReflectiveOperationException | LinkageError exception) {
            return "";
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage(color("&#95090cРубины: &#750000" + getRubies((Player) sender)));
            } else {
                sender.sendMessage(color("&#95090cИспользование: /" + label + " give|take|set|balance <ник> [кол-во]"));
            }
            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("balance") && args.length == 1 && sender instanceof Player) {
            sender.sendMessage(color("&#95090cРубины: &#750000" + getRubies((Player) sender)));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(color("&#95090cИспользование: /" + label + " give|take|set|balance <ник> [кол-во]"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(color("&#750000Игрок не найден."));
            return true;
        }

        if (action.equals("balance")) {
            sender.sendMessage(color("&#95090cРубины " + target.getName() + ": &#750000" + getRubies(target)));
            return true;
        }

        if (!sender.hasPermission("desirebridge.points.admin")) {
            sender.sendMessage(color("&#750000Недостаточно прав."));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(color("&#95090cУкажи количество."));
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(color("&#750000Количество должно быть числом."));
            return true;
        }

        switch (action) {
            case "give":
            case "add":
                addRubies(target, amount);
                break;
            case "take":
                addRubies(target, -amount);
                break;
            case "set":
                setRubies(target, amount);
                break;
            default:
                sender.sendMessage(color("&#95090cИспользование: /" + label + " give|take|set|balance <ник> [кол-во]"));
                return true;
        }

        sender.sendMessage(color("&#95090cРубины " + target.getName() + ": &#750000" + getRubies(target)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("balance", "give", "take", "set"), args[0]);
        }
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(value);
            }
        }
        return result;
    }

    private String color(String message) {
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00a7x");
            for (char character : hex.toCharArray()) {
                replacement.append('\u00a7').append(character);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(result);
        return ChatColor.translateAlternateColorCodes('&', result.toString());
    }

    private static final class DesireExpansion extends PlaceholderExpansion {
        private final DesireBridge plugin;

        private DesireExpansion(DesireBridge plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getIdentifier() {
            return "desire";
        }

        @Override
        public String getAuthor() {
            return "Desire";
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(Player player, String params) {
            if (params == null) {
                return "";
            }

            switch (params.toLowerCase(Locale.ROOT)) {
                case "rubies":
                    return String.valueOf(plugin.getRubies(player));
                case "clan":
                    return plugin.getClanDisplay(player);
                case "clan_name":
                    return plugin.getClanName(player);
                case "clan_tag":
                    return plugin.getClanTag(player);
                case "kills":
                    return player == null ? "0" : String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS));
                case "deaths":
                    return player == null ? "0" : String.valueOf(player.getStatistic(Statistic.DEATHS));
                case "hours":
                    if (player == null) {
                        return "0";
                    }
                    double hours = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000.0;
                    return String.format(Locale.ROOT, "%.1f", hours);
                default:
                    return "";
            }
        }
    }
}
