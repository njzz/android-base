package com.njzz.bases.common;

import android.app.Activity;

public interface LiftAble {
    default Activity getAttached(){return null;}
}
