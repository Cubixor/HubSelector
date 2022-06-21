package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.PermissionUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class HubCommand extends Command implements TabExecutor {

    HubSelectorBungee plugin;

    public HubCommand() {
        super("hub", "", "lobby", "l");
        plugin = HubSelectorBungee.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(plugin.getMessage("command.not-player"));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (!PermissionUtils.hasPermission(player, "hub.use")) {
            sender.sendMessage(plugin.getMessage("command.no-permission"));
            return;
        }

        HubChoose hubChoose = new HubChoose();

        if (!PermissionUtils.hasPermission(player, "hub.choose") && !PermissionUtils.hasPermission(player, "hub.choose.others")) {
            if (args.length != 0) {
                sender.sendMessage(plugin.getMessage("command.hub-usage"));
            } else {
                hubChoose.connectToHub(player, true, false);
            }

        } else {
            if (!PermissionUtils.hasPermission(player, "hub.choose.others")) {
                switch (args.length) {
                    case (1):
                        hubChoose.connectToSpecifiedHub(player, player.getName(), args[0]);
                        break;
                    case (0):
                        hubChoose.connectToHub(player, true, false);
                        break;
                    default:
                        sender.sendMessage(plugin.getMessage("command.hub-choose-usage"));
                        break;
                }

            } else {
                switch (args.length) {
                    case (2):
                        hubChoose.connectToSpecifiedHub(player, args[1], args[0]);
                        break;
                    case (1):
                        hubChoose.connectToSpecifiedHub(player, player.getName(), args[0]);
                        break;
                    case (0):
                        hubChoose.connectToHub(player, true, false);
                        break;
                    default:
                        sender.sendMessage(plugin.getMessage("command.hub-choose-others-usage"));
                        break;
                }
            }
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (PermissionUtils.hasPermission(player, "hub.choose") || PermissionUtils.hasPermission(player, "hub.choose.others")) {
            switch (args.length) {
                case 1:
                    result.add("*");
                    for (Hub hub : plugin.getHubs()) {
                        String server = hub.getServer();
                        if (server.toLowerCase().startsWith(args[0].toLowerCase())) {
                            result.add(server);
                        }
                    }
                    break;
                case 2:
                    if (PermissionUtils.hasPermission(player, "hub.choose.others")) {
                        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                            String pString = p.getName();
                            if (pString.toLowerCase().startsWith(args[1].toLowerCase())) {
                                result.add(pString);
                            }
                        }
                    }
                    break;
            }
        }
        return result;
    }
}
