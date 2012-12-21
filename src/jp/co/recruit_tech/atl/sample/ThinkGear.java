package jp.co.recruit_tech.atl.sample;

import jp.co.recruit_tech.atl.sample.MainActivity.MyView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

public class ThinkGear {
	BluetoothAdapter bluetoothAdapter=null;
	TGDevice tgDevice=null;
	TGEegPower tgEegPower=null;
	TGData tgData = new TGData();
	final boolean rawEnabled = false;
	MyView   myView = null;

	public ThinkGear(){
		createTGDevice();
	}

	public void setMyView(MyView v){
		myView = v;
	}

	private void createTGDevice(){
		// B3Bandとのbluetooth接続
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter == null) {
			// Alert user that Bluetooth is not available
			Log.w("EEGReader", "Bluetooth not available");
		}else {
			// create the TGDevice
			tgDevice = new TGDevice(bluetoothAdapter, handler);
			if(tgDevice == null){
				Log.w("EEGReader", "BrainBand not available");
			}
		}
	}

	public TGData tgData(){
		return tgData;
	}

	public boolean isConnecting(){
		if(tgDevice == null) return false;
		if(tgDevice.getState() == TGDevice.STATE_CONNECTING) return true;
		if(tgDevice.getState() == TGDevice.STATE_CONNECTED) return true;
		return false;
	}

	public void connect() {
		if(!this.isConnecting()) tgDevice.connect(rawEnabled);
	}

	public void disconnect() {
		Log.d("EEGReader", "ThinkGear Disconnecting");
		if(this.isConnecting()) {
			Log.d("EEGReader", "ThinkGear close");
			tgDevice.close();
		}
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TGDevice.MSG_STATE_CHANGE:
				switch (msg.arg1) {
				case TGDevice.STATE_IDLE:
					break;
				case TGDevice.STATE_CONNECTING:
					Log.d("EEGReader", "ThinkGear Connecting...");
					tgData.status("connecting");
					if(myView != null) myView.b3status(tgData.status());
					break;
				case TGDevice.STATE_CONNECTED:
					Log.d("EEGReader", "ThinkGear Connected");
					tgData.status("connected");
					if(myView != null) myView.b3status(tgData.status());
					tgDevice.start();
					break;
				case TGDevice.STATE_NOT_FOUND:
					Log.w("EEGReader", "ThinkGear Can't find");
					break;
				case TGDevice.STATE_NOT_PAIRED:
					Log.w("EEGReader", "ThinkGear not paired");
					break;
				case TGDevice.STATE_DISCONNECTED:
					Log.d("EEGReader", "ThinkGear Disconnected");
					tgData.status("disconnected");
					if(myView != null) myView.b3status(tgData.status());
				}
				break;

			case TGDevice.MSG_POOR_SIGNAL:
				tgData.poor_signal(msg.arg1);
				if(myView != null) myView.sigQ(tgData.poor_signal());
				break;

			case TGDevice.MSG_HEART_RATE:
				break;

			case TGDevice.MSG_ATTENTION:
				tgData.attention(msg.arg1);
				if(myView != null) myView.attention(tgData.attention());
				break;

			case TGDevice.MSG_MEDITATION:
				tgData.meditation(msg.arg1);
				if(myView != null) myView.meditation(tgData.meditation());
				break;

			case TGDevice.MSG_BLINK:
				tgData.blink(msg.arg1);
				if(myView != null) myView.blink(tgData.blink());
				break;

			case TGDevice.MSG_LOW_BATTERY:
				Log.w("EEGReader", "ThinkGear Low battery!");
				break;

			case TGDevice.MSG_EEG_POWER:
				tgEegPower = (TGEegPower)msg.obj;
				tgData.eeg(tgEegPower);
				if(myView != null) myView.eeg(tgData.eeg());
				break;

			default:
				break;
			}
		}
	};

	public class TGData{
		String status;
		int poor_signal;
		int meditation;
		int attention;
		int blink;
		int[] eeg = new int[8];

		public TGData(){
			clear();
		}

		public String status()            {	return status;	}
		public void   status(String value){
			status = value;
		}
		public int  poor_signal()         {	return poor_signal;	}
		public void poor_signal(int value){
			poor_signal = value;
		}
		public int  meditation()         {	return meditation;	}
		public void meditation(int value){
			meditation = value;
		}
		public int  attention()         {	return attention;	}
		public void attention(int value){
			attention = value;
		}
		public int  blink()         {	return blink;	}
		public void blink(int value){
			blink = value;
		}
		public int[] eeg()                     {	return eeg;	}
		public void  eeg(TGEegPower tgEegPower){
			eeg[0] = tgEegPower.delta;
			eeg[1] = tgEegPower.theta;
			eeg[2] = tgEegPower.lowAlpha;
			eeg[3] = tgEegPower.highAlpha;
			eeg[4] = tgEegPower.lowBeta;
			eeg[5] = tgEegPower.highBeta;
			eeg[6] = tgEegPower.lowGamma;
			eeg[7] = tgEegPower.midGamma;
		}

		private void clear(){
			poor_signal = -1;
			meditation = -1;
			attention = -1;
			blink = -1;
			for (int i = 0; i < eeg.length; i++) {
				eeg[i] = -1;
			}
		}
	}
}
