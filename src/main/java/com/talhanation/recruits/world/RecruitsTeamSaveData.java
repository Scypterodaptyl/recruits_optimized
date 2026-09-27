package com.talhanation.recruits.world;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

public class RecruitsTeamSaveData extends SavedData {

    public static final String FILE_ID = "recruitsTeamSaveData";
    private Map<String, RecruitsFaction> teams = new HashMap<>();

    public static RecruitsTeamSaveData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public static RecruitsTeamSaveData load(CompoundTag nbt) {
        RecruitsTeamSaveData data = new RecruitsTeamSaveData();
        if (nbt.contains("Teams")) {
            data.teams = loadTeams(nbt.getListOrEmpty("Teams"));
        }
        return data;
    }

    private static Map<String, RecruitsFaction> loadTeams(ListTag list) {
        Map<String, RecruitsFaction> loadedTeams = new HashMap<>();
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag nbt = list.getCompoundOrEmpty(i);
            RecruitsFaction recruitsFaction = new RecruitsFaction();

            recruitsFaction.setStringID(nbt.getStringOr("TeamName", ""));
            recruitsFaction.setTeamDisplayName(nbt.getStringOr("TeamDisplayName", ""));
            recruitsFaction.setTeamLeaderID(nbt.read("TeamLeaderID", UUIDUtil.CODEC).orElse(null));
            recruitsFaction.setTeamLeaderName(nbt.getStringOr("TeamLeaderName", ""));
            recruitsFaction.setBanner((CompoundTag) nbt.get("TeamBanner"));
            recruitsFaction.setPlayers(nbt.getIntOr("Players", 0));
            recruitsFaction.setNPCs(nbt.getIntOr("NPCs", 0));

            recruitsFaction.setMaxPlayers(nbt.getIntOr("MaxPlayers", 0));
            recruitsFaction.setMaxNPCs(nbt.getIntOr("MaxNPCs", 0));

            ListTag joinRequestsList = nbt.getListOrEmpty("JoinRequests");
            for (int j = 0; j < joinRequestsList.size(); ++j) {
                recruitsFaction.getJoinRequests().add(joinRequestsList.getStringOr(j, ""));
            }

            recruitsFaction.setUnitColor(nbt.getByteOr("Color", (byte) 0));
            recruitsFaction.setTeamColor(nbt.getIntOr("TeamColor", 0));
            recruitsFaction.setMaxNPCsPerPlayer(nbt.getIntOr("maxNpcsPerPlayer", 0));

            loadedTeams.put(recruitsFaction.getStringID(), recruitsFaction);
        }
        return loadedTeams;
    }

        public CompoundTag save(CompoundTag nbt) {
        nbt.put("Teams", saveTeams());
        return nbt;
    }

    private ListTag saveTeams() {
        ListTag listTag = new ListTag();
        for (RecruitsFaction team : teams.values()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("TeamName", team.getStringID());
            nbt.putString("TeamDisplayName", team.getTeamDisplayName());
            nbt.store("TeamLeaderID", UUIDUtil.CODEC, team.getTeamLeaderUUID());
            nbt.putString("TeamLeaderName", team.getTeamLeaderName());
            nbt.put("TeamBanner", team.getBanner());
            nbt.putInt("Players", team.getPlayers());
            nbt.putInt("NPCs", team.getNPCs());

            nbt.putInt("MaxPlayers", team.getMaxPlayers());
            nbt.putInt("MaxNPCs", team.getMaxNPCs());

            ListTag joinRequestsTag = new ListTag();
            for (String request : team.getJoinRequests()) {
                joinRequestsTag.add(StringTag.valueOf(request));
            }
            nbt.put("JoinRequests", joinRequestsTag);
            nbt.putByte("Color", team.getUnitColor());
            nbt.putInt("TeamColor", team.getTeamColor());
            nbt.putInt("maxNpcsPerPlayer", team.getMaxNPCsPerPlayer());

            listTag.add(nbt);
        }
        return listTag;
    }

    public Map<String, RecruitsFaction> getTeams() {
        return teams;
    }

    public void setTeams(Map<String, RecruitsFaction> teams) {
        this.teams = teams;
    }

    public static final net.minecraft.world.level.saveddata.SavedDataType<RecruitsTeamSaveData> TYPE = com.talhanation.recruits.util.SavedDataUtil.type(FILE_ID, RecruitsTeamSaveData::new, RecruitsTeamSaveData::load, data -> data.save(new CompoundTag()));
}
