package org.springframework.data.neo4j.ogm.domain.forum;

import org.springframework.data.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label ="Bronze")
public class BronzeMembership extends Membership {

    @Override
    public boolean getCanPost() {
        return false;
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
        return new IMembership[] { new SilverMembership(), new GoldMembership()};
    }
}
