package com.njzz.bases.common;

public class MessageSet {
    public MessageSet(){}
    public MessageSet(int what){
        this(what,0);
    }
    public MessageSet(int what ,int arg1){
        this(what,arg1,0);
    }
    public MessageSet(int what ,int arg1,int arg2){
        this(what,arg1,arg2,null);
    }
    public MessageSet(int what,Object arjObj){
        this(what,0,0,arjObj);
    }
    public MessageSet(int what,int arg1,int arg2,Object argObj){
        this.what=what;
        this.arg1=arg1;
        this.arg2=arg2;
        this.argObj=argObj;
    }
    public int what;//消息主体
    public int arg1;//参数1
    public int arg2;//参数2
    public Object argObj;//Object 参数
}
