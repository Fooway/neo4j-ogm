package org.springframework.data.neo4j.ogm.unit.mapper.model.bike;

import org.springframework.data.neo4j.ogm.RequestProxy;

public class BikeRequest extends RequestProxy {

    public String[] getResponse() {
        return jsonModel;
    }

    private static String[] jsonModel = {
            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"15\",\"labels\" : [ \"Bike\"], \"properties\" : { \"colours\" :[\"red\", \"black\"] } }, " +
                    "{\"id\" : \"16\",\"labels\" : [ \"Wheel\", \"FrontWheel\" ],\"properties\" : {\"spokes\" : 3 } }, " +
                    "{\"id\" : \"17\",\"labels\" : [ \"Wheel\", \"BackWheel\" ],\"properties\" : {\"spokes\" : 5 } }, " +
                    "{\"id\" : \"18\",\"labels\" : [ \"Frame\" ],\"properties\" : {\"size\" : 27 } }, " +
                    "{\"id\" : \"19\",\"labels\" : [ \"Saddle\" ],\"properties\" : {\"price\" : 42.99, \"material\" : \"plastic\" } } " +
                    "], " +
                "\"relationships\": [" +
                    "{\"id\":\"141\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"16\",\"properties\":{ \"purchased\" : 20130917 }}, " +
                    "{\"id\":\"142\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"17\",\"properties\":{ \"purchased\" : 20130917 }}," +
                    "{\"id\":\"143\",\"type\":\"HAS_FRAME\",\"startNode\":\"15\",\"endNode\":\"18\",\"properties\":{ \"purchased\" : 20130917 }}," +
                    "{\"id\":\"144\",\"type\":\"HAS_SADDLE\",\"startNode\":\"15\",\"endNode\":\"19\",\"properties\":{\"purchased\" : 20130922 }} " +
                    "] " +
            "} }"
    };
}
