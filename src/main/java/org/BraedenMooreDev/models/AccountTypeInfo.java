package org.BraedenMooreDev.models;

import net.runelite.client.hiscore.Skill;

public class AccountTypeInfo {

    public final AccountType type;
    public final Skill skill;

    public AccountTypeInfo(AccountType type, Skill skill) {
        this.type = type;
        this.skill = skill;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
