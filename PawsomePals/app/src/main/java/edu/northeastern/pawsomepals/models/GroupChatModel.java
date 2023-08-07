package edu.northeastern.pawsomepals.models;

import java.util.List;

public class GroupChatModel {
    List<String> groupMembers;
    String groupName;
    List<String> groupMemberNames;

    public GroupChatModel(List<String> groupMembers,List<String> groupMemberNames, String groupName) {
        this.groupMembers = groupMembers;
        this.groupMemberNames = groupMemberNames;
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

    public List<String> getGroupMemberNames() {
        return groupMemberNames;
    }

    public void setGroupMemberNames(List<String> groupMemberNames) {
        this.groupMemberNames = groupMemberNames;
    }
}
