package me.cubixor.hubselector.bungeecord;

import me.cubixor.hubselector.bungeecord.queue.QueueMainBungee;
import me.cubixor.hubselector.bungeecord.queue.QueueSetupBungee;
import me.cubixor.hubselector.bungeecord.queue.QueueUtils;
import me.cubixor.hubselector.bungeecord.socket.SocketServerSender;
import me.cubixor.hubselector.utils.Hub;
import me.cubixor.hubselector.utils.PermissionUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HubSelectorCommand extends Command implements TabExecutor {

    HubSelectorBungee plugin;

    public HubSelectorCommand() {
        super("hubselector", "", "hs");
        plugin = HubSelectorBungee.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.help")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            for (String s : plugin.getMessageList("command.help")) {
                sender.sendMessage(TextComponent.fromLegacyText(s));
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.reload")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (plugin.getConfig().getBoolean("queue.use-queue")) {
                new QueueSetupBungee().queueReload();
            }

            plugin.loadConfigs();
            new JoinMethod().setupJoinMethod();

            sender.sendMessage(plugin.getMessage("command.reload-complete"));
        } else if (args[0].equalsIgnoreCase("setactive")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.setactive")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (args.length != 3 || !(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))) {
                sender.sendMessage(plugin.getMessage("command.setactive-usage"));
                return;
            }

            Hub hub = HubUtils.getHub(args[1]);

            if (hub == null) {
                sender.sendMessage(plugin.getMessage("command.invalid-hub"));
                return;
            }

            boolean active = Boolean.parseBoolean(args[2]);

            if (active && hub.isActive()) {
                sender.sendMessage(plugin.getMessage("command.setactive-already-active"));
                return;
            }

            if (!active && !hub.isActive()) {
                sender.sendMessage(plugin.getMessage("command.setactive-already-inactive"));
                return;
            }

            hub.setActive(active);
            plugin.getHubServers().set("hub-servers." + args[1] + ".active", active);
            plugin.saveHubServersFile();


            if (active) {
                new QueueUtils().putInHub();
                sender.sendMessage(plugin.getMessage("command.setactive-success-active"));
            } else {
                sender.sendMessage(plugin.getMessage("command.setactive-success-inactive"));
            }
        } else if (args[0].equalsIgnoreCase("setname")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.setname")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (args.length < 3) {
                sender.sendMessage(plugin.getMessage("command.setname-usage"));
                return;
            }

            Hub hub = HubUtils.getHub(args[1]);

            if (hub == null) {
                sender.sendMessage(plugin.getMessage("command.invalid-hub"));
                return;
            }

            List<String> nameList = new ArrayList<>(Arrays.asList(args));
            nameList.remove(args[0]);
            nameList.remove(args[1]);


            StringBuilder stringBuilder = new StringBuilder();
            for (String arg : nameList) {
                stringBuilder.append(arg);
                if (nameList.indexOf(arg) != nameList.size() - 1) {
                    stringBuilder.append(" ");
                }
            }

            String name = stringBuilder.toString();
            hub.setName(name);
            plugin.getHubServers().set("hub-servers." + args[1] + ".name", name);
            plugin.saveHubServersFile();

            sender.sendMessage(plugin.getMessage("command.setname-success"));
        } else if (args[0].equalsIgnoreCase("setslots")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.setslots")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (args.length != 3) {
                sender.sendMessage(plugin.getMessage("command.setslots-usage"));
                return;
            }

            Hub hub = HubUtils.getHub(args[1]);

            if (hub == null) {
                sender.sendMessage(plugin.getMessage("command.invalid-hub"));
                return;
            }

            int slots;
            try {
                slots = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("command.setslots-usage"));
                return;
            }

            hub.setSlots(slots);
            plugin.getHubServers().set("hub-servers." + args[1] + ".slots", slots);
            plugin.saveHubServersFile();

            new QueueUtils().putInHub();

            sender.sendMessage(plugin.getMessage("command.setslots-success"));
        } else if (args[0].equalsIgnoreCase("setvip")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.setvip")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (args.length != 3 || !(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))) {
                sender.sendMessage(plugin.getMessage("command.setvip-usage"));
                return;
            }

            Hub hub = HubUtils.getHub(args[1]);

            if (hub == null) {
                sender.sendMessage(plugin.getMessage("command.invalid-hub"));
                return;
            }

            boolean vip = Boolean.parseBoolean(args[2]);

            if (vip && hub.isVip()) {
                sender.sendMessage(plugin.getMessage("command.setvip-already-vip"));
                return;
            }

            if (!vip && !hub.isVip()) {
                sender.sendMessage(plugin.getMessage("command.setvip-already-notvip"));
                return;
            }

            hub.setVip(vip);
            plugin.getHubServers().set("hub-servers." + args[1] + ".vip", vip);
            plugin.saveHubServersFile();

            new QueueUtils().putInHub();

            if (vip) {
                sender.sendMessage(plugin.getMessage("command.setvip-success-vip"));
            } else {
                sender.sendMessage(plugin.getMessage("command.setvip-success-notvip"));
            }
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.kick")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (args.length != 2) {
                sender.sendMessage(plugin.getMessage("command.kick-usage"));
                return;
            }

            Hub hub = HubUtils.getHub(args[1]);

            if (hub == null) {
                sender.sendMessage(plugin.getMessage("command.invalid-hub"));
                return;
            }

            if (!HubUtils.checkIfOnline(hub)) {
                sender.sendMessage(plugin.getMessage("command.kick-offline"));
                return;
            }

            if (hub.isActive()) {
                sender.sendMessage(plugin.getMessage("command.kick-active"));
                return;
            }

            if (plugin.getProxy().getServerInfo(args[1]).getPlayers().isEmpty()) {
                sender.sendMessage(plugin.getMessage("command.kick-empty"));
                return;
            }

            boolean active = hub.isActive();
            if (active) hub.setActive(false);

            for (ProxiedPlayer player : plugin.getProxy().getServerInfo(args[1]).getPlayers()) {
                if (HubUtils.canBypassInactive(player)) {
                    continue;
                }

                String hubString = HubChoose.chooseServer(player, true, true, false);

                if (hubString != null) {
                    Hub newHub = HubUtils.getHub(hubString);
                    if (newHub != null) {
                        new HubChoose().sendToServer(player, HubUtils.getServerInfo(newHub));
                    } else {
                        new QueueUtils().putInQueue(player);
                        player.connect(plugin.getProxy().getServerInfo(plugin.getHubServers().getString("queue-server")));
                    }
                } else {
                    player.disconnect(plugin.getMessage("connect.kick-stopped"));
                }
            }

            if (active) hub.setActive(true);

            new QueueUtils().putInHub();

            sender.sendMessage(plugin.getMessage("command.kick-success"));


        } else if (args[0].equalsIgnoreCase("skipqueue")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.skipqueue")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (args.length != 2) {
                sender.sendMessage(plugin.getMessage("command.skipqueue-usage"));
                return;
            }

            if (!QueueUtils.queueAvailable()) {
                sender.sendMessage(plugin.getMessage("command.skipqueue-not-available"));
                return;
            }

            ProxiedPlayer player = null;
            for (ProxiedPlayer p : plugin.getQueueServer().getPlayers()) {
                if (p.getName().equalsIgnoreCase(args[1])) {
                    player = p;
                    break;
                }
            }

            if (player == null) {
                sender.sendMessage(plugin.getMessage("command.skipqueue-not-in-queue"));
                return;
            }

            if (new HubChoose().connectToHub(player, false, true)) {
                QueueMainBungee.getInstance().getQueuePlayers().get(QueueUtils.getQueueRank(player)).remove(player);
            } else {
                sender.sendMessage(plugin.getMessage("command.skipqueue-no-servers"));
            }


        } else if (args[0].equalsIgnoreCase("pausequeue")) {
            if (!PermissionUtils.hasPermission(sender, "hub.command.pausequeue")) {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }

            if (args.length != 2) {
                sender.sendMessage(plugin.getMessage("command.pausequeue-usage"));
                return;
            }

            if (plugin.getQueueServer() == null) {
                sender.sendMessage(plugin.getMessage("command.queue-not-available"));
                return;
            }

            boolean paused = Boolean.parseBoolean(args[1]);
            QueueMainBungee queue = QueueMainBungee.getInstance();

            if (paused && queue.isQueuePaused()) {
                sender.sendMessage(plugin.getMessage("command.pausequeue-already-paused"));
                return;
            }

            if (!paused && !queue.isQueuePaused()) {
                sender.sendMessage(plugin.getMessage("command.pausequeue-already-unpaused"));
                return;
            }

            queue.setQueuePaused(paused);
            plugin.getHubServers().set("queue-paused", paused);
            plugin.saveHubServersFile();


            if (paused) {
                sender.sendMessage(plugin.getMessage("command.pausequeue-success-paused"));
            } else {
                sender.sendMessage(plugin.getMessage("command.pausequeue-success-unpaused"));
                new QueueUtils().putInHub();
            }
        } else if (args[0].equalsIgnoreCase("menu")) {

            String target;

            if (PermissionUtils.hasPermission(sender, "hub.command.menu.others")) {
                switch (args.length) {
                    case 2: {
                        target = args[1];
                        break;
                    }
                    case 1: {
                        target = sender.getName();
                        break;
                    }
                    default:
                        sender.sendMessage(plugin.getMessage("command.menu-others-usage"));
                        return;
                }
            } else if (PermissionUtils.hasPermission(sender, "hub.command.menu")) {
                if (args.length == 1) {
                    target = sender.getName();
                } else {
                    sender.sendMessage(plugin.getMessage("command.menu-usage"));
                    return;
                }
            } else {
                sender.sendMessage(plugin.getMessage("command.no-permission"));
                return;
            }


            ProxiedPlayer player = plugin.getProxy().getPlayer(target);

            if (player == null) {
                sender.sendMessage(plugin.getMessage("command.invalid-player"));
                return;
            }

            if (!HubUtils.isInHub(player, false)) {
                sender.sendMessage(plugin.getMessage("command.not-in-hub"));
                return;
            }

            SocketServerSender socketServerSender = new SocketServerSender();
            socketServerSender.openMenu(player);
            socketServerSender.sendALlHubsInfo(player);

            if (sender.getName().equals(target)) {
                sender.sendMessage(plugin.getMessage("command.menu-success"));
            } else {
                sender.sendMessage(plugin.getMessage("command.menu-others-success", "%player%", target));
            }

        } else {
            sender.sendMessage(plugin.getMessage("command.unknown-command"));
        }

    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        switch (args.length) {
            case 1: {
                if (PermissionUtils.hasPermission(sender, "hub.command.help") && "help".startsWith(args[0])) {
                    result.add("help");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.reload") && "reload".startsWith(args[0])) {
                    result.add("reload");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.setactive") && "setactive".startsWith(args[0])) {
                    result.add("setactive");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.setname") && "setname".startsWith(args[0])) {
                    result.add("setname");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.setslots") && "setslots".startsWith(args[0])) {
                    result.add("setslots");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.setvip") && "setvip".startsWith(args[0])) {
                    result.add("setvip");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.kick") && "kick".startsWith(args[0])) {
                    result.add("kick");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.skipqueue") && "skipqueue".startsWith(args[0])) {
                    result.add("skipqueue");
                }

                if (PermissionUtils.hasPermission(sender, "hub.command.pausequeue") && "pausequeue".startsWith(args[0])) {
                    result.add("pausequeue");
                }

                if ((PermissionUtils.hasPermission(sender, "hub.command.menu")
                        || PermissionUtils.hasPermission(sender, "hub.command.menu.others")) && "menu".startsWith(args[0])) {
                    result.add("menu");
                }
                break;
            }
            case 2: {
                if ((PermissionUtils.hasPermission(sender, "hub.command.setactive") && "setactive".startsWith(args[0])) ||
                        (PermissionUtils.hasPermission(sender, "hub.command.setname") && "setname".startsWith(args[0])) ||
                        (PermissionUtils.hasPermission(sender, "hub.command.setslots") && "setslots".startsWith(args[0])) ||
                        (PermissionUtils.hasPermission(sender, "hub.command.setvip") && "setvip".startsWith(args[0])) ||
                        (PermissionUtils.hasPermission(sender, "hub.command.kick") && "kick".startsWith(args[0]))) {
                    for (Hub hub : plugin.getHubs()) {
                        if (hub.getServer().startsWith(args[1])) {
                            result.add(hub.getServer());
                        }
                    }
                }
                if (PermissionUtils.hasPermission(sender, "hub.command.skipqueue") && "skipqueue".startsWith(args[0])) {
                    if (QueueUtils.queueAvailable()) {
                        for (ProxiedPlayer player : plugin.getQueueServer().getPlayers()) {
                            if (player.getName().startsWith(args[1])) {
                                result.add(player.getName());
                            }
                        }
                    }
                }
                if (PermissionUtils.hasPermission(sender, "hub.command.pausequeue") && "pausequeue".startsWith(args[0])) {
                    if ("true".startsWith(args[1])) {
                        result.add("true");
                    }
                    if ("false".startsWith(args[1])) {
                        result.add("false");
                    }
                }
                if (PermissionUtils.hasPermission(sender, "hub.command.menu.others") && "menu".startsWith(args[0])) {
                    for (List<String> list : plugin.getServerSlots().values()) {
                        for (String player : list) {
                            if (player.startsWith(args[1])) {
                                result.add(player);
                            }
                        }
                    }
                }
                break;
            }
            case 3: {
                if ((PermissionUtils.hasPermission(sender, "hub.command.setvip") && "setvip".startsWith(args[0]) ||
                        (PermissionUtils.hasPermission(sender, "hub.command.setactive") && "setactive".startsWith(args[0])))) {
                    if ("true".startsWith(args[2])) {
                        result.add("true");
                    }
                    if ("false".startsWith(args[2])) {
                        result.add("false");
                    }
                }
                break;
            }
        }

        return result;
    }
}