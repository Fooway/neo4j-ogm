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

package org.neo4j.ogm.model;

public class Property<K, V> {

    K key;
    V value;

    /**
     * Constructs a new {@link Property} inferring the generic type arguments of the key and the value.
     *
     * @param key The property key or name
     * @param value The property value
     * @return A new {@link Property} based on the given arguments
     */
    public static <K, V> Property<K, V> with(K key, V value) {
        return new Property<K, V>(key, value);
    }

    public Property() {}

    public Property(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public String toString() {
        return String.format("%s : %s", this.key, asParameter());
    }

    public Object asParameter() {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return value;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e1) {
            try {
                return Double.parseDouble(value.toString());
            } catch (Exception e2) {
                return value.toString();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Property property = (Property) o;

        if (!key.equals(property.key)) return false;
        if (!value.equals(property.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
