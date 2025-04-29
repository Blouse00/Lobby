package com.stewart.lobby.instances;

import java.sql.Timestamp;
import java.util.UUID;

public class PlayerServerInfo {

    private final String sockName;
    private final UUID uuid;
    private final Timestamp sentToServer;

    public PlayerServerInfo(String sockName, UUID uuid, Timestamp sentToServer) {
        this.sockName = sockName;
        this.uuid = uuid;
        this.sentToServer = sentToServer;
    }

    public String getSockName() {
        return sockName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Timestamp getTimeSentToServer() {
        return sentToServer;
    }

}
