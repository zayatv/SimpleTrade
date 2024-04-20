package com.zayatv.simpletrade.utils;

import org.bukkit.entity.Player;

import java.util.List;

public final class SignCompletedEvent {
    private final Player player;
    private final List<String> lines;

    public SignCompletedEvent(Player player, List<String> lines) {
        this.player = player;
        this.lines = lines;
    }

    public Player getPlayer() {
        return player;
    }

    public List<String> getLines() {
        return lines;
    }
}