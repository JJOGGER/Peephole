package com.bairuitech.anychat.config;

import android.util.Log;

import java.io.Serializable;

public class UserItem implements Serializable, Cloneable {

	private String mStrNameOther;//设备别名
	private int mIsOnline;
	private String mStrName;//设备号名
	private String mStrIp;
	private int mUserId;
	private int mUserIdenty;
	private int mGroupId;
	private String mDeviceVer;
	private static final long serialVersionUID = 8502706820090766507L;
	public static final int USERSTATUS_OFFLINE = 0;
	public static final int USERSTATUS_ONLINE = 1;
	public static final int USERINFO_NAME = 1;
	public static final int USERINFO_IP = 2;

	public UserItem(int mUserId, String strName, String strNameOther, String strIp) {
		this.mStrName = strName;
		this.mUserId = mUserId;
		this.mStrIp = strIp;
		this.mStrNameOther = strNameOther;
	}
	
	public void setIsOnline(int online){
		this.mIsOnline = online;
	}
	
	public int getIsOnline(){
		return mIsOnline;
	}
	
	public void setUserNameOther(String nameOther){
		this.mStrNameOther = nameOther;
	}
	
	public String getUserNameOther(){
		return this.mStrNameOther;
	}

	public UserItem() {
		mStrName = "";
	}

	public void setUserId(int pUserid) {
		mUserId = pUserid;
	}

	public void setUserIenty(int pIndety) {
		mUserIdenty = pIndety;
	}

	public void setUserName(String pUserName) {
		mStrName = pUserName;
	}

	public void setIp(String ip) {
		mStrIp = ip;
	}

	public String getIp() {
		return mStrIp;
	}

	public int getUserId() {
		return mUserId;
	}

	public int getUserIdenty() {
		return mUserIdenty;
	}

	public String getUserName() {
		return mStrName;
	}

	public void setGroupId(int dwGroupId) {
		mGroupId = dwGroupId;
	}

	public int getGroupId() {
		return mGroupId;
	}
	
	public void setDeviceVer(String ver){
		mDeviceVer = ver;
	}

	public String getDeviceVer(){
		return mDeviceVer;
	}
	public UserItem clone() {
		UserItem item = null;
		try {
			item = (UserItem) super.clone();
		} catch (Exception e) {
			Log.i("useritem-clone", e.toString());
		}
		return item;
	}
}
