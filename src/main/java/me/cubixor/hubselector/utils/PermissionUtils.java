package me.cubixor.hubselector.utils;

import net.md_5.bungee.api.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtils {

    public static boolean hasPermission(CommandSender player, String permission) {
        if (player.hasPermission(permission)) {
            return true;
        }

        List<String> permissionSplit = new ArrayList<>(Arrays.asList(permission.split("\\.")));
        permissionSplit.remove(permissionSplit.size() - 1);

        for (int i = 0; i < permissionSplit.size(); i++) {
            String str = "";
            for (int j = 0; j <= i; j++) {
                str = str + permissionSplit.get(j) + ".";
            }
            str = str + "*";
            if (player.hasPermission(str)) {
                return true;
            }
        }
        return false;
    }
}
