package org.neo4j.ogm.typeconversion;

import org.apache.commons.codec.binary.Base64;

/**
 * By default the OGM will map byte[] objects to Base64
 * String values when being stored as a node / relationship property
 */
public class ByteArrayBase64Converter implements AttributeConverter<byte[], String> {

    @Override
    public String toGraphProperty(byte[] value) {
        if (value == null) return null;
        return Base64.encodeBase64String(value);
    }

    @Override
    public byte[] toEntityAttribute(String value) {
        if (value == null) return null;
        return Base64.decodeBase64(value);
    }

}
