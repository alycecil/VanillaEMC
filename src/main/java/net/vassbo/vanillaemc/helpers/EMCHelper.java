package net.vassbo.vanillaemc.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.vassbo.vanillaemc.VanillaEMC;
import net.vassbo.vanillaemc.data.EMCValues;
import net.vassbo.vanillaemc.data.PlayerData;
import net.vassbo.vanillaemc.data.StateSaverAndLoader;
import net.vassbo.vanillaemc.packets.DataSender;
import net.vassbo.vanillaemc.screen.DissolverScreenHandler;

public class EMCHelper {
    public static boolean serverAddItem(World world, String itemId, int addedValue) {
        MinecraftServer server = world.getServer();
        
        PlayerData globalData = StateSaverAndLoader.getGlobalData(server);
        List<String> learnedList = globalData.LEARNED_ITEMS;
        if (learnedList.contains(itemId)) return false;

        learnedList.add(itemId);
        StateSaverAndLoader.setGlobalLearned(server, learnedList);

        int currentValue = globalData.EMC;
        int newValue = currentValue += addedValue;
        
        StateSaverAndLoader.setGlobalEMC(server, newValue);

        return true;
    }

    public static void addEMCValue(PlayerEntity player, int addedValue) {
        if (player.getServer() == null) return;

        int currentValue = getEMCValue(player);
        int newValue = currentValue += addedValue;
        
        setEMCValue(player, newValue);
    }

    public static boolean removeEMCValue(PlayerEntity player, int removedValue) {
        if (player.getServer() == null) return false;

        int currentValue = getEMCValue(player);
        int newValue = currentValue -= removedValue;

        if (newValue < 0) return false;

        setEMCValue(player, newValue);
        return true;
    }

    public static int getEMCValue(PlayerEntity player) {
        return StateSaverAndLoader.getPlayerState(player).EMC;
    }

    public static void setEMCValue(PlayerEntity player, int value) {
        StateSaverAndLoader.setPlayerEMC(player, value);
    }

    // CHECK

    public enum Action {
        GET, ADD;
    }

    private static boolean checkValidEMC(int emc, String id, Action action) {
        if (emc == 0) {
            VanillaEMC.LOGGER.info("Tried to " + action + " item, but it does not have any EMC value. ID: " + id);
            return false;
        }

        return true;
    }

    // GET

    public static boolean getItem(PlayerEntity player, ItemStack itemStack, DissolverScreenHandler handler, int items) {
        String itemId = itemStack.getItem().toString();
        int emcValue = EMCValues.get(itemId) * items;

        if (!checkValidEMC(emcValue, itemId, Action.GET)) return false;

        if (!EMCHelper.removeEMCValue(player, emcValue)) {
            sendMessageToClient(player, "emc.action.not_enough_short");
            return false;
        }

        // refresh block inv content
        new Thread(() -> {
            // let content update before updating!
            wait(10);
            handler.refresh();
        }).start();

        return true;
    }

    public static boolean addItem(ItemStack itemStack) {
        String itemId = itemStack.getItem().toString();
        int emcValue = EMCValues.get(itemId);

        return checkValidEMC(emcValue, itemId, Action.ADD);
    }

    // ADD

    // added from another inventory & not private EMC
    public static boolean addItem(ItemStack itemStack, World world) {
        String itemId = itemStack.getItem().toString();
        int emcValue = EMCValues.get(itemId);

        if (!checkValidEMC(emcValue, itemId, Action.ADD)) return false;

        int itemCount = itemStack.getCount();
        int addedEmcValue = (int)(emcValue * itemCount * ItemHelper.getDurabilityPercentage(itemStack));

        return serverAddItem(world, itemId, addedEmcValue);
    }

    public static boolean addItem(ItemStack itemStack, PlayerEntity player, DissolverScreenHandler handler) {
        String itemId = itemStack.getItem().toString();
        int emcValue = EMCValues.get(itemId);

        if (!checkValidEMC(emcValue, itemId, Action.ADD)) return false;

        // calculated new EMC (from DissolverInventoryInput)
        int itemCount = itemStack.getCount();
        int addedEmcValue = (int)(emcValue * itemCount * ItemHelper.getDurabilityPercentage(itemStack));

        learnItem(player, itemId);

        EMCHelper.addEMCValue(player, addedEmcValue);

        // refresh block inv content
        new Thread(() -> {
            // let content update before updating!
            wait(10);
            handler.refresh();
        }).start();

        return true;
    }

    // LEARN

    public static boolean learnItem(PlayerEntity player, String itemId) {
        List<String> learnedList = StateSaverAndLoader.getPlayerState(player).LEARNED_ITEMS;
        if (learnedList.contains(itemId)) return false;

        learnedList.add(itemId);
        StateSaverAndLoader.setPlayerLearned(player, learnedList);

        // let blocklist update before sending message (to prevent empty message sent)
        new Thread(() -> {
            wait(50);
            sendMessageToClient(player, "emc.action.stored_short");
        }).start();

        return true;
    }

    public static boolean forgetItem(PlayerEntity player, String itemId) {
        List<String> learnedList = StateSaverAndLoader.getPlayerState(player).LEARNED_ITEMS;
        if (!learnedList.contains(itemId)) return false;

        learnedList.remove(itemId);
        StateSaverAndLoader.setPlayerLearned(player, learnedList);

        // let blocklist update before sending message (to prevent empty message sent)
        new Thread(() -> {
            wait(50);
            sendMessageToClient(player, "emc.action.removed_short");
        }).start();

        return true;
    }

    public static void learnAllItems(PlayerEntity player) {
        List<String> learnedList = new ArrayList<>();

        for (String key : EMCValues.getList()) {
            learnedList.add(key);
        }
        
        StateSaverAndLoader.setPlayerLearned(player, learnedList);
    }

    public static void forgetAllItems(PlayerEntity player) {
        StateSaverAndLoader.setPlayerLearned(player, new ArrayList<>());
    }

    // SEND

    public static void sendStateToClient(PlayerEntity player) {
        PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
        DataSender.sendPlayerData(player, playerState);
    }
    
    private static final HashMap<String, Integer> TIMEOUT_IDs = new HashMap<String, Integer>();
    public static void sendMessageToClient(PlayerEntity player, String message) {
        PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
        playerState.MESSAGE = message;
        DataSender.sendPlayerData(player, playerState);

        String playerId = player.getUuid().toString();
        if (!TIMEOUT_IDs.containsKey(playerId)) TIMEOUT_IDs.put(playerId, 0);
        int currentId = TIMEOUT_IDs.get(playerId) + 1;
        TIMEOUT_IDs.put(playerId, currentId);

        // timeout message
        new Thread(() -> {
            wait(1200);

            // another message was sent before this could clear!
            if (TIMEOUT_IDs.get(playerId) != currentId) return;

            PlayerData playerStateNew = StateSaverAndLoader.getPlayerState(player);
            playerStateNew.MESSAGE = "";
            DataSender.sendPlayerData(player, playerStateNew);
        }).start();
    }

    // TOOLTIP

    public static Text tooltipValue(String key) {
        return tooltipValue(key, 1);
    }

    public static Text tooltipValue(String key, double reducedEmc) {
        Integer EMC = EMCValues.get(key);
        Text text = Text.literal("");
        if (EMC == 0) return text;

        return Text.translatable("item_tooltip.vanillaemc.emc", (int)(EMC * reducedEmc));
    }

    // HELPERS

    private static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
