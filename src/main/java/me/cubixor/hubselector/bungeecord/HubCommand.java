package me.cubixor.hubselector.bungeecord;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class HubCommand extends Command implements TabExecutor {

    HubSelectorBungee plugin;

    public HubCommand(HubSelectorBungee hsb) {
        super("hub", "", "lobby");
        plugin = hsb;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(plugin.getMessage("command.not-player"));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        boolean vipPlayer = player.hasPermission("hub.vip");

        HubChoose hubChoose = new HubChoose(plugin);

        if (!player.hasPermission("hub.use")) {
            sender.sendMessage(plugin.getMessage("command.no-permission"));
            return;
        }

        if (!player.hasPermission("hub.choose") && !player.hasPermission("hub.choose.others")) {
            if (args.length != 0) {
                sender.sendMessage(plugin.getMessage("command.usage"));
            } else {
                hubChoose.hubCheck(player, vipPlayer);
            }

        } else {
            if (!player.hasPermission("hub.choose.others")) {
                switch (args.length) {
                    case (1):
                        hubChoose.chooseHub(player, player.getName(), args[0], vipPlayer);
                        break;
                    case (0):
                        hubChoose.hubCheck(player, vipPlayer);
                        break;
                    default:
                        sender.sendMessage(plugin.getMessage("command.usage-choose"));
                        break;
                }


            } else {
                switch (args.length) {
                    case (2):
                        hubChoose.chooseHub(player, args[1], args[0], vipPlayer);
                        break;
                    case (1):
                        hubChoose.chooseHub(player, player.getName(), args[0], vipPlayer);
                        break;
                    case (0):
                        hubChoose.hubCheck(player, vipPlayer);
                        break;
                    default:
                        sender.sendMessage(plugin.getMessage("command.usage-choose-others"));
                        break;
                }
            }
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (player.hasPermission("hub.choose")) {
            if (!player.hasPermission("hub.choose.others")) {
                if (args.length == 1) {
                    result.addAll(plugin.getConfig().getSection("hub-servers").getKeys());
                }
            } else {
                switch (args.length) {
                    case 1:
                        result.add("*");
                        for (String s : plugin.getConfig().getSection("hub-servers").getKeys()) {
                            if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                                result.add(s);
                            }
                        }
                        break;
                    case 2:
                        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                            String pString = p.getName();
                            if (pString.toLowerCase().startsWith(args[1].toLowerCase())) {
                                result.add(pString);

                            }
                        }
                        break;
                }
            }
        }
        return result;
    }
}
