package me.cubixor.hubselector.bungeecord;

import java.util.*;

interface JoinMethods {
    String method(List<String> availableServers);
}

interface VipJoinMethods {
    String method(List<String> availableServers, List<String> availableVipServers);
}

public class JoinMethod {

    HubSelectorBungee plugin;

    public JoinMethod() {
        plugin = HubSelectorBungee.getInstance();
    }

    public void setupJoinMethod() {
        String method = plugin.getConfig().getString("hub-choose-method");
        String vipPriority = plugin.getConfig().getString("vip-priority");

        JoinMethods joinMethods = null;
        VipJoinMethods vipJoinMethods = null;

        if (method.equalsIgnoreCase("RANDOM")) {
            Random r = new Random();
            if (vipPriority.equalsIgnoreCase("HIGH")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    String chosenServer;
                    if (availableVipServers.size() != 0) {
                        int server = r.nextInt(availableVipServers.size());
                        chosenServer = availableVipServers.get(server);
                    } else {
                        int server = r.nextInt(availableServers.size());
                        chosenServer = availableServers.get(server);
                    }
                    return chosenServer;
                };
            } else if (vipPriority.equalsIgnoreCase("MEDIUM")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedList<String> availableAll = new LinkedList<>();
                    availableAll.addAll(availableServers);
                    availableAll.addAll(availableVipServers);

                    int server = r.nextInt(availableAll.size());
                    return availableAll.get(server);
                };
            } else if (vipPriority.equalsIgnoreCase("LOW")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    String chosenServer;

                    if (availableServers.size() != 0) {
                        int server = r.nextInt(availableServers.size());
                        chosenServer = availableServers.get(server);
                    } else {

                        int server = r.nextInt(availableVipServers.size());
                        chosenServer = availableVipServers.get(server);
                    }
                    return chosenServer;
                };
            }

            joinMethods = (availableServers) -> {
                int server = r.nextInt(availableServers.size());
                return availableServers.get(server);
            };


        } else if (method.equalsIgnoreCase("LEAST-PLAYERS")) {
            if (vipPriority.equalsIgnoreCase("HIGH")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedHashMap<String, Integer> availableServersSorted = sortServers(availableServers);
                    LinkedHashMap<String, Integer> availableVipServersSorted = sortServers(availableVipServers);

                    String chosenServer;
                    if (availableVipServers.size() != 0) {
                        chosenServer = new ArrayList<>(availableVipServersSorted.keySet()).get(0);
                    } else {
                        chosenServer = new ArrayList<>(availableServersSorted.keySet()).get(0);
                    }
                    return chosenServer;
                };
            } else if (vipPriority.equalsIgnoreCase("MEDIUM")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedHashMap<String, Integer> availableAllSorted = sortServers(availableServers, availableVipServers);

                    return new ArrayList<>(availableAllSorted.keySet()).get(0);
                };
            } else if (vipPriority.equalsIgnoreCase("LOW")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedHashMap<String, Integer> availableServersSorted = sortServers(availableServers);
                    LinkedHashMap<String, Integer> availableVipServersSorted = sortServers(availableVipServers);

                    String chosenServer;
                    if (availableServers.size() != 0) {
                        chosenServer = new ArrayList<>(availableServersSorted.keySet()).get(0);
                    } else {
                        chosenServer = new ArrayList<>(availableVipServersSorted.keySet()).get(0);
                    }
                    return chosenServer;
                };
            }
            joinMethods = (availableServers) -> {
                LinkedHashMap<String, Integer> availableServersSorted = sortServers(availableServers);
                return new ArrayList<>(availableServersSorted.keySet()).get(0);
            };

        } else if (method.equalsIgnoreCase("MAX-PLAYERS")) {
            if (vipPriority.equalsIgnoreCase("HIGH")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedHashMap<String, Integer> availableServersSorted = sortServers(availableServers);
                    LinkedHashMap<String, Integer> availableVipServersSorted = sortServers(availableVipServers);

                    String chosenServer;

                    if (availableVipServers.size() != 0) {
                        chosenServer = new ArrayList<>(availableVipServersSorted.keySet()).get(availableVipServersSorted.keySet().size() - 1);
                    } else {
                        chosenServer = new ArrayList<>(availableServersSorted.keySet()).get(availableServersSorted.keySet().size() - 1);
                    }
                    return chosenServer;
                };
            } else if (vipPriority.equalsIgnoreCase("MEDIUM")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedHashMap<String, Integer> availableAllSorted = sortServers(availableServers, availableVipServers);

                    return new ArrayList<>(availableAllSorted.keySet()).get(availableAllSorted.keySet().size() - 1);
                };
            } else if (vipPriority.equalsIgnoreCase("LOW")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedHashMap<String, Integer> availableServersSorted = sortServers(availableServers);
                    LinkedHashMap<String, Integer> availableVipServersSorted = sortServers(availableVipServers);

                    String chosenServer;

                    if (availableServers.size() != 0) {
                        chosenServer = new ArrayList<>(availableServersSorted.keySet()).get(availableServersSorted.keySet().size() - 1);
                    } else {
                        chosenServer = new ArrayList<>(availableVipServersSorted.keySet()).get(availableVipServersSorted.keySet().size() - 1);
                    }
                    return chosenServer;
                };
            }

            joinMethods = (availableServers) -> {
                LinkedHashMap<String, Integer> availableServersSorted = sortServers(availableServers);
                return new ArrayList<>(availableServersSorted.keySet()).get(availableServersSorted.keySet().size() - 1);
            };

        } else if (method.equalsIgnoreCase("FIRST-AVAILABLE")) {
            if (vipPriority.equalsIgnoreCase("HIGH")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    String chosenServer;

                    if (availableVipServers.size() != 0) {
                        chosenServer = availableVipServers.get(0);
                    } else {
                        chosenServer = availableServers.get(0);
                    }
                    return chosenServer;
                };
            } else if (vipPriority.equalsIgnoreCase("MEDIUM")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    LinkedList<String> availableAll = new LinkedList<>();
                    availableAll.addAll(availableServers);
                    availableAll.addAll(availableVipServers);

                    return availableAll.get(0);
                };
            } else if (vipPriority.equalsIgnoreCase("LOW")) {
                vipJoinMethods = (availableServers, availableVipServers) -> {
                    String chosenServer;

                    if (availableServers.size() != 0) {
                        chosenServer = availableServers.get(0);
                    } else {
                        chosenServer = availableVipServers.get(0);
                    }
                    return chosenServer;
                };
            }
            joinMethods = (availableServers) -> availableServers.get(0);

        }

        plugin.setJoinMethodsInstance(joinMethods);
        plugin.setVipJoinMethodsInstance(vipJoinMethods);

    }

    private LinkedHashMap<String, Integer> sortByValue(HashMap<String, Integer> hashMap) {
        LinkedHashMap<String, Integer> temp = new LinkedHashMap<>();

        hashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> temp.put(x.getKey(), x.getValue()));

        System.out.println(temp);

        return temp;
    }

    private LinkedHashMap<String, Integer> sortServers(List<String> availableServers) {
        HashMap<String, Integer> availableServersHash = new HashMap<>();

        for (String s : availableServers) {
            availableServersHash.put(s, plugin.getProxy().getServerInfo(s).getPlayers().size());
        }

        return sortByValue(availableServersHash);
    }

    private LinkedHashMap<String, Integer> sortServers(List<String> availableServers, List<String> availableVipServers) {
        LinkedList<String> availableAll = new LinkedList<>();
        availableAll.addAll(availableServers);
        availableAll.addAll(availableVipServers);

        HashMap<String, Integer> availableAllHash = new HashMap<>();
        for (String s : availableAll) {
            availableAllHash.put(s, plugin.getProxy().getServerInfo(s).getPlayers().size());
        }
        return sortByValue(availableAllHash);
    }
}
