package com.agames.thuruppugulan.webrequest.model.request;

import com.agames.thuruppugulan.webrequest.model.BaseWebModel;

public class Authenticate extends BaseWebModel {
    //{"auth":"<yourID>","passwd":"<yourPass>"}
    public String auth;
    public String passwd;
}
