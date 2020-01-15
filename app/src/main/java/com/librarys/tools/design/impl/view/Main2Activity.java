package com.librarys.tools.design.impl.view;

import android.os.Bundle;

import com.librarys.tools.R;
import com.njzz.bases.common.BaseActivity;
import com.librarys.tools.design.impl.presenter.Main2ActivityPresenter;
import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.ToastUtils;

public class Main2Activity extends BaseActivity {

    static public String paramName="PARAM_TRANS";
    static public class UserInfo{
        public UserInfo(String name,String sex){
            strName=name;
            strSex=sex;
        }

        public String showText(){
            return "姓名:"+strName+" 性别:"+strSex;
        }

        String strName;
        String strSex;
    }

    Main2ActivityPresenter  mMyPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        LogUtils.e("main2 is oncreate.....");

        UserInfo user = getParam(paramName);
        if(user!=null)
            ToastUtils.show(this,user.showText());

        initStatusBar(true,false,0);

        mMyPresenter=new Main2ActivityPresenter(this,findViewById(R.id.recyler_show));
        mMyPresenter.start();
    }
}
