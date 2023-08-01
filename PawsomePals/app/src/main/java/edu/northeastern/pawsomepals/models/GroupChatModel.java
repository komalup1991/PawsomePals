package edu.northeastern.pawsomepals.models;

import java.util.List;

public class GroupChatModel {
    List<String> groupMembers;
    String groupName;

    public GroupChatModel(List<String> groupMembers, String groupName) {
        this.groupMembers = groupMembers;
        this.groupName = groupName;
    }

    public List<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
