package app.arifnurrohim.simsetter;

import android.os.Message;

interface IPhoneNumberSetter {

    int getPhoneCount();
    String getLine1Number(int phoneId);
    boolean setLine1Number(int phoneId, String number, in Message onComplete);

}
