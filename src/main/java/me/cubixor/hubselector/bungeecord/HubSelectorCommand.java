package me.cubixor.hubselector.bungeecord;

import com.google.common.collect.Iterables;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class HubSelectorCommand extends Command implements TabExecutor {

    HubSelectorBungee plugin;

    public HubSelectorCommand(HubSelectorBungee hsb) {
        super("hubselector", "", "hs");
        plugin = hsb;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(plugin.getMessage("command.not-player"));
                return;
            }

            if (!sender.hasPermission("hub.help")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            for (String s : plugin.getMessageList("command.help")) {
                sender.sendMessage(TextComponent.fromLegacyText(s));
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("hub.reload")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (plugin.getProxy().getOnlineCount() == 0) {
                sender.sendMessage(plugin.getMessage("command.no-players"));
                return;
            }

            plugin.loadConfigs();
            new JoinMethod(plugin).joinMethodSetup();

            for (String server : plugin.getConfig().getSection("hub-servers").getKeys()) {
                if (plugin.getProxy().getServers().get(server).getPlayers().size() == 0) {
                    plugin.serversToReload.add(server);
                } else {
                    new BungeeChannel(plugin).getConfiguration(Iterables.getFirst(plugin.getProxy().getServers().get(server).getPlayers(), null));
                }
            }

            sender.sendMessage(plugin.getMessage("command.reload-complete"));

        } else {
            sender.sendMessage(plugin.getMessage("command.unknown-command"));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (args.length == 1) {
            if (p.hasPermission("hub.help")) {
                if ("help".startsWith(args[0])) {
                    result.add("help");
                }
            }

            if (p.hasPermission("hub.reload")) {
                if ("reload".startsWith(args[0])) {
                    result.add("reload");
                }
            }
        }

        return result;

    }
}
