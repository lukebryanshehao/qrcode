package com.qrcode.model;

import net.sf.json.JSONObject;

public class ResultData {

    public String Code;
    public Object Data;
    public String Msg;

    public Object getData() {
        return Data;
    }

    public void setData(Object data) {
        Data = data;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String NewResultData(String code, Object data, String msg){
        ResultData result = new ResultData();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);

        return JSONObject.fromObject(result).toString();
    }
}


