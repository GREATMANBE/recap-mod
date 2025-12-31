package com.yourname.zombiesrecap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.*;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraft.network.play.server.S0FPacketSpawnMob;

import java.util.*;

public class RoundTracker {

    private final WaveDetector waveDetector = new WaveDetector();

    private final Map<String, PlayerSnapshot> roundStart = new HashMap<>();
    private final Map<String, PlayerSnapshot> waveStart = new HashMap<>();

    /* ---------------- PACKET LISTENER ---------------- */
    @SubscribeEvent
    public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent e) {
        if (e.packet instanceof S0FPacketSpawnMob) {
            S0FPacketSpawnMob packet = (S0FPacketSpawnMob) e.packet;

            if (packet.getEntityType() == 54) { // Zombie
                boolean newWave = waveDetector.onZombieSpawn();
                if (newWave) snapshotWaveStart();
            }
        }
    }

    /* ---------------- CHAT EVENTS ---------------- */
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        String msg = e.message.getUnformattedText();

        if (msg.contains("The game has started")) {
            snapshotRoundStart();
            waveDetector.reset();
        }

        if (msg.contains("ROUND COMPLETE") || msg.contains("Round Completed")) {
            printRoundRecap();
        }
    }

    /* ---------------- SNAPSHOTS ---------------- */
    private void snapshotRoundStart() {
        roundStart.clear();
        snapshot(roundStart);
    }

    private void snapshotWaveStart() {
        waveStart.clear();
        snapshot(waveStart);
    }

    private void snapshot(Map<String, PlayerSnapshot> map) {
        for (NetworkPlayerInfo info : Minecraft.getMinecraft()
                .getNetHandler().getPlayerInfoMap()) {

            String name = info.getGameProfile().getName();
            map.put(name, new PlayerSnapshot(
                    getKills(info),
                    getGold(name)
            ));
        }
    }

    /* ---------------- DATA READ ---------------- */
    private int getKills(NetworkPlayerInfo info) {
        Scoreboard sb = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective obj = sb.getObjectiveInDisplaySlot(0);
        if (obj == null) return 0;

        Score score = sb.getValueFromObjective(info.getGameProfile().getName(), obj);
        return score != null ? score.getScorePoints() : 0;
    }

    private int getGold(String player) {
        Scoreboard sb = Minecraft.getMinecraft().theWorld.getScoreboard();

        for (ScoreObjective obj : sb.getScoreObjectives()) {
            if (obj.getName().toLowerCase().contains("gold")) {
                Score score = sb.getValueFromObjective(player, obj);
                if (score != null) return score.getScorePoints();
            }
        }
        return 0;
    }

    /* ---------------- RECAP ---------------- */
    private void printRoundRecap() {
        Minecraft mc = Minecraft.getMinecraft();

        for (String player : roundStart.keySet()) {
            PlayerSnapshot r = roundStart.get(player);
            PlayerSnapshot w = waveStart.get(player);

            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfoMap().stream()
                    .filter(p -> p.getGameProfile().getName().equals(player))
                    .findFirst()
                    .orElse(null);

            if (info == null) continue;

            int curKills = getKills(info);
            int curGold = getGold(player);

            int roundKills = curKills - r.kills;
            int roundGold = curGold - r.gold;

            int waveKills = w != null ? curKills - w.kills : 0;
            int waveGold = w != null ? curGold - w.gold : 0;

            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§e" + player + ": §f" + roundKills));
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§f" + roundGold + " §a" + waveKills));
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§f" + waveGold));
            mc.thePlayer.addChatMessage(new ChatComponentText(""));
        }
    }
}
