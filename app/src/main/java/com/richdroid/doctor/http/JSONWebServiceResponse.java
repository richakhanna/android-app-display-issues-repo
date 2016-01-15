package com.richdroid.doctor.http;

/**
 * Created by richa.khanna on 8/8/15.
 */


import org.json.JSONArray;

public class JSONWebServiceResponse {

    private final JSONArray result;
    private final int code;

    public JSONWebServiceResponse(JSONArray result, int code) {
        super();
        this.result = result;
        this.code = code;
    }

    public JSONArray getResult() {
        return result;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "JSONWebServiceResponse {result: " + result + ", code: " + code
                + "}";
    }

}
