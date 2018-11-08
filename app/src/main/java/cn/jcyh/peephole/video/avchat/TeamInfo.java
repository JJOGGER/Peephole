package cn.jcyh.peephole.video.avchat;

import java.util.List;

/**
 * Created by jogger on 2018/10/31.
 */
public class TeamInfo {
    String roomName;
    String teamID;
    List<String> accounts;
    String teamName;

    public TeamInfo(String roomName, String teamID, List<String> accounts, String teamName) {
        this.roomName = roomName;
        this.teamID = teamID;
        this.accounts = accounts;
        this.teamName = teamName;
    }
}
