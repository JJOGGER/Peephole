//package cn.jcyh.peephole;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.TextView;
//
//import com.spotmau.main.BcManager;
//
//
//public class TEST extends Activity implements OnClickListener {
//	BcManager mMgr = null;
//	TextView mTxtInfo=null;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		mTxtInfo = (TextView) findViewById(R.id.textView1);
//		mMgr = new BcManager(this);
//	}
//
//
//	@Override
//	public void onClick(View v) {
//		boolean ret=false;
//		switch (v.getId()) {
//			case R.id.btn_get_pir:
//				ret = mMgr.getPIRStatus();
//				if (ret){
//					show("检测到有人");
//				}else {
//					show("检测到无人");
//				}
//				break;
//			case R.id.btn_get_pir_en:
//				ret = mMgr.getPIRSensorOn();
//				if (ret){
//					show("PIR传器是打开状态");
//				}else {
//					show("PIR传器是关闭状态");
//				}
//				break;
//			case R.id.btn_pir_en_on:
//				mMgr.setPIRSensorOn(true);
//				show("PIR传器打开完成");
//				break;
//			case R.id.btn_pir_en_off:
//				mMgr.setPIRSensorOn(false);
//				show("PIR传器关闭完成");
//				break;
//			case R.id.btn_get_lock_detect:
//				ret = mMgr.getTamperSensorStatus();
//				if (ret){
//					show("已经拆掉了");
//				}else {
//					show("连接正常");
//				}
//				break;
//			case R.id.btn_open_irled:
//				mMgr.setInfraredLightPowerOn(true);
//				show("红外灯打开完成");
//				break;
//			case R.id.btn_close_irled:
//				mMgr.setInfraredLightPowerOn(false);
//				show("红外灯关闭完成");
//				break;
//			case R.id.btn_get_irled_status:
//				ret = mMgr.getInfraredLightStatus();
//				if (ret){
//					show("红外灯是打开状态");
//				}else {
//					show("红外灯是关闭状态");
//				}
//				break;
//
//			case R.id.btn_open_pa1:
//				mMgr.setSpeakerPowerOn(0, true);
//				show("PA1打开完成");
//				break;
//			case R.id.btn_open_pa2:
//				mMgr.setSpeakerPowerOn(1,true);
//				show("PA2打开完成");
//				break;
//			case R.id.btn_close_pa1:
//				mMgr.setSpeakerPowerOn(0,false);
//				show("PA1关闭完成");
//				break;
//			case R.id.btn_close_pa2:
//				mMgr.setSpeakerPowerOn(1,false);
//				show("PA2关闭完成");
//				break;
//			case R.id.btn_status_pa1:
//				ret = mMgr.getSpeakerStatus(0);
//				if (ret){
//					show("PA1是打开状态");
//				}else {
//					show("PA1是关闭状态");
//				}
//				break;
//			case R.id.btn_status_pa2:
//				ret = mMgr.getSpeakerStatus(1);
//				if (ret){
//					show("PA2是打开状态");
//				}else {
//					show("PA2是关闭状态");
//				}
//				break;
//			case R.id.btn_bell_key_led_on:
//				mMgr.setRingKeyLedOn(true);
//				show("门铃键背光灯打开完成");
//				break;
//			case R.id.btn_bell_key_led_off:
//				mMgr.setRingKeyLedOn(false);
//				show("门铃键背光灯关闭完成");
//				break;
//			case R.id.btn_bell_key_led_status:
//				ret = mMgr.getRingKeyLedStatus();
//				if (ret){
//					show("门铃键背光灯是打开状态");
//				}else {
//					show("门铃键背光灯是关闭状态");
//				}
//				break;
//			case R.id.btn_light_value:
//				int x=mMgr.getLightSensorValue();
//				show("光感值为:" + x);
////			Message msg = new Message();
////			msg.what = 1;
////			mHandler.sendMessage(msg);
//				break;
//
//			default:
//				break;
//		}
//	}
//
//	private Handler mHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			switch (msg.what) {
//				case 1:
//					mMgr.setIntTurnOnScreen(BcManager.INT_TYPE.OURDOOR_PRESS,true);//设置门铃送开亮屏     true为亮屏 false为不亮屏
//					break;
//				case 2:
//					mMgr.setIntTurnOnScreen(BcManager.INT_TYPE.TAMPER,true);//设置防拆亮屏     true为亮屏 false为不亮屏
//					break;
//				case 3:
//					mMgr.setIntTurnOnScreen(BcManager.INT_TYPE.INDOOR_PRESS,true);//设置门铃按下亮屏     true为亮屏 false为不亮屏
//					break;
//				case 4:
//					mMgr.setIntTurnOnScreen(BcManager.INT_TYPE.PIR,true);//设置人体感应亮屏     true为亮屏 false为不亮屏
//					break;
//
//			}
//		}
//	};
//	public void show(String mes) {
//		mTxtInfo.setText(mes);
//	}
//}
