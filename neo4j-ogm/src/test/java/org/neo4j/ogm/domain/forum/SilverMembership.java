package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label ="Silver")
public class SilverMembership extends Membership {

    @Override
    public boolean getCanPost() {
        return true;
    }

    @Override
    public boolean getCanComment() {
        return true;
    }

    @Override
    public boolean getCanFollow() {
        return false;
    }

    @Override
    public IMembership[] getUpgrades() {
        return new IMembership[] { new GoldMembership() };
    }
}
