package com.talhanation.recruits.world;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RecruitsFaction {
    public String stringID;
    public String teamDisplayName;
    public UUID teamLeaderID;
    public String teamLeaderName;
    public CompoundTag banner;
    public List<String> joinRequests = new ArrayList<>();
    public List<RecruitsPlayerInfo> members = new ArrayList<>();
    public int players;
    public int npcs;
    public byte unitColor;
    public int teamColor;
    public int maxPlayers;
    public int maxNPCs;
    public int maxNPCsPerPlayer = -1;
    private int biome = -1;

    public RecruitsFaction(String stringID, String teamLeaderName, CompoundTag banner) {
        this.stringID = stringID;
        this.teamDisplayName = stringID;
        this.teamLeaderName = teamLeaderName;
        this.banner = banner;
    }

    public RecruitsFaction() {

    }

    public CompoundTag getBanner() {
        return banner;
    }

    public UUID getTeamLeaderUUID() {
        return teamLeaderID;
    }

    public String getStringID() {
        return stringID;
    }

    public String getTeamDisplayName() {
        return teamDisplayName;
    }

    public String getTeamLeaderName()  {
        return teamLeaderName;
    }

    public void setStringID(String stringID) {
        this.stringID = stringID;
    }

    public void setTeamDisplayName(String teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
    }

    public void setTeamLeaderID(UUID uuid) {
        teamLeaderID = uuid;
    }

    public void setTeamLeaderName(String leaderName) {
        teamLeaderName = leaderName;
    }

    public void setBanner(CompoundTag nbt) {
        banner = nbt;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public void setNPCs(int npcs) {
        this.npcs = npcs;
    }

    public void setMaxPlayers(int max) {
        this.maxPlayers = max;
    }

    public void setMaxNPCs(int max) {
        this.maxNPCs = max;
    }

    public boolean addPlayerAsJoinRequest(String player) {
        if (!joinRequests.contains(player)){
            joinRequests.add(player);
            return true;
        }
        return false;
    }

    public void removeJoinRequest(String player) {
        joinRequests.remove(player);
    }

    public List<String> getJoinRequests() {
        return joinRequests;
    }

    public void addMember(UUID uuid, String name) {
        if (members.stream().noneMatch(m -> m.getUUID().equals(uuid))) {
            members.add(new RecruitsPlayerInfo(uuid, name));
        }
    }

    public void removeMember(String name) {
        members.removeIf(m -> m.getName().equalsIgnoreCase(name));
    }

    public List<RecruitsPlayerInfo> getMembers() {
        return members;
    }

    public int getNPCs() {
        return npcs;
    }

    public int getPlayers() {
        return players;
    }

    public byte getUnitColor() {
        return unitColor;
    }

    public int  getMaxNPCsPerPlayer() {
        return maxNPCsPerPlayer;
    }

    public int getTeamColor() {
        return teamColor;
    }

    public int getMaxNPCs() {
        return maxNPCs;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void addNPCs(int x) {
        npcs += x;
        if (npcs < 0) npcs = 0;
    }

    public void addPlayer(int x) {
        players += x;
        if (players < 0) players = 0;
    }

    public void setUnitColor(byte unitColor) {
        this.unitColor = unitColor;
    }

    public void setTeamColor(int color) {
        this.teamColor = color;
    }

    public void setMaxNPCsPerPlayer(int maxNPCsPerPlayer) {
        this.maxNPCsPerPlayer = maxNPCsPerPlayer;
    }

    @Override
    public String toString() {
        return this.getStringID();
    }

    public boolean equalsFaction(RecruitsFaction faction){
        if(faction == null) return false;
        return this.getStringID().equals(faction.getStringID());
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("teamName", this.stringID);
        nbt.putString("teamDisplayName", this.teamDisplayName);
        nbt.store("teamLeaderID", UUIDUtil.CODEC, this.teamLeaderID);
        nbt.putString("teamLeaderName", this.teamLeaderName);
        nbt.put("banner", this.banner);

        ListTag joinRequestsTag = new ListTag();
        for (String request : joinRequests) {
            joinRequestsTag.add(StringTag.valueOf(request));
        }
        nbt.put("joinRequests", joinRequestsTag);

        ListTag membersTag = new ListTag();
        for (RecruitsPlayerInfo member : members) {
            membersTag.add(member.toNBT());
        }
        nbt.put("members", membersTag);

        nbt.putInt("players", this.players);
        nbt.putInt("npcs", this.npcs);
        nbt.putInt("maxPlayers", this.maxPlayers);
        nbt.putInt("maxNpcs", this.maxNPCs);
        nbt.putByte("unitColor", this.unitColor);
        nbt.putInt("teamColor", this.teamColor);
        nbt.putInt("biome", this.biome);
        nbt.putInt("maxNPCsPerPlayer", this.maxNPCsPerPlayer);

        return nbt;
    }

    public static RecruitsFaction fromNBT(CompoundTag nbt) {
        if(nbt == null || nbt.isEmpty()) {
            return null;
        }
        RecruitsFaction team = new RecruitsFaction();
        team.setStringID(nbt.getStringOr("teamName", ""));
        if(nbt.getString("teamDisplayName").isEmpty()){
            team.setTeamDisplayName(team.getStringID());
        }
        else
            team.setTeamDisplayName(nbt.getStringOr("teamDisplayName", ""));

        team.setTeamLeaderID(nbt.read("teamLeaderID", UUIDUtil.CODEC).orElse(null));
        team.setTeamLeaderName(nbt.getStringOr("teamLeaderName", ""));
        team.setBanner(nbt.getCompoundOrEmpty("banner"));

        ListTag joinRequestsTag = nbt.getListOrEmpty("joinRequests");
        for (int i = 0; i < joinRequestsTag.size(); i++) {
            team.addPlayerAsJoinRequest(joinRequestsTag.getStringOr(i, ""));
        }

        ListTag membersTag = nbt.getListOrEmpty("members");
        for (int i = 0; i < membersTag.size(); i++) {
            RecruitsPlayerInfo member = RecruitsPlayerInfo.getFromNBT(membersTag.getCompoundOrEmpty(i));
            if (member != null) {
                team.members.add(member);
            }
        }

        team.setPlayers(nbt.getIntOr("players", 0));
        team.setNPCs(nbt.getIntOr("npcs", 0));
        team.setMaxPlayers(nbt.getIntOr("maxPlayers", 0));
        team.setMaxNPCs(nbt.getIntOr("maxNpcs", 0));
        team.setUnitColor(nbt.getByteOr("unitColor", (byte) 0));
        team.setTeamColor(nbt.getIntOr("teamColor", 0));
        team.biome = nbt.getIntOr("biome", 0);
        team.setMaxNPCsPerPlayer(nbt.getIntOr("maxNPCsPerPlayer", 0));
        return team;
    }

    public static CompoundTag toNBT(List<RecruitsFaction> list) {
        CompoundTag nbt = new CompoundTag();
        ListTag teamList = new ListTag();

        for (RecruitsFaction team : list) {
            teamList.add(team.toNBT());
        }

        nbt.put("Teams", teamList);
        return nbt;
    }

    public static List<RecruitsFaction> getListFromNBT(CompoundTag nbt) {
        List<RecruitsFaction> list = new ArrayList<>();
        ListTag teamList = nbt.getListOrEmpty("Teams");

        for (int i = 0; i < teamList.size(); i++) {
            CompoundTag teamTag = teamList.getCompoundOrEmpty(i);
            list.add(RecruitsFaction.fromNBT(teamTag));
        }

        return list;
    }

    public boolean canAddNPC() {
        if(getMaxNPCs() == 0) return true;
        else return getMaxNPCs() > getNPCs();
    }

    public boolean canAddPlayer() {
        if(getMaxPlayers() == 0) return true;
        else return getMaxPlayers() > getPlayers();
    }

    public enum PlayerRank {
        NONE,
        LEADER,
        CAPTAIN,
        COMMANDER,
    }
}
