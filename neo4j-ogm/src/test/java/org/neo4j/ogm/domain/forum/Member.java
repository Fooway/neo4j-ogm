/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.activity.Activity;

import java.util.Date;
import java.util.List;

@NodeEntity(label ="User")
public class Member extends Login  {

    private IMembership memberShip;
    private Date renewalDate;
    @Relationship(type ="HAS_ACTIVITY")
    private Iterable<Activity> activityList;
    private List<Member> followers;
    private List<Member> followees;
    private Long membershipNumber;
    private int[] nicknames;

    public IMembership getMemberShip() {
        return memberShip;
    }

    public void setMemberShip(IMembership memberShip) {
        this.memberShip = memberShip;
    }

    public Date getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(Date renewalDate) {
        this.renewalDate = renewalDate;
    }

    @Relationship(type ="HAS_ACTIVITY")
    public Iterable<Activity> getActivityList() {
        return activityList;
    }

    @Relationship(type ="HAS_ACTIVITY")
    public void setActivityList(Iterable<Activity> activityList) {
        this.activityList = activityList;
    }

    public List<Member> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Member> followers) {
        this.followers = followers;
    }

    public List<Member> getFollowees() {
        return followees;
    }

    public void setFollowees(List<Member> followees) {
        this.followees = followees;
    }

    public long getMembershipNumber() {
        return membershipNumber;
    }

    public void setMembershipNumber(long membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    public int[] getNicknames() {
        return nicknames;
    }

    public void setNicknames(int[] nicknames) {
        this.nicknames = nicknames;
    }

}
