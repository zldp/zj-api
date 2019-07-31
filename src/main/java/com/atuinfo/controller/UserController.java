package com.atuinfo.controller;

import com.jfinal.core.Controller;

public class UserController extends Controller {
    public void index(){
        renderText("这是user的controller");
    }
}
