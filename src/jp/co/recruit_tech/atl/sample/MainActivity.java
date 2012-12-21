package jp.co.recruit_tech.atl.sample;

import java.lang.Thread.UncaughtExceptionHandler;

import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Color;

public class MainActivity extends Activity {
	MyView myView=null;
	ThinkGear thinkGear = null;
	GestureDetector gestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gestureDetector = new GestureDetector(this, simpleOnGestureListener);

		myView = new MyView();

	    Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		exit();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {
	    @Override
	    public void uncaughtException(Thread th, Throwable e) {
			String msg = "uncaughtException - "+e.toString();
			Log.d("EEGReader", msg);
			exit();
	    }
	}

	public void exit(){
		Log.d("EEGReader", "exit");

		if(thinkGear != null) {
			Log.d("EEGReader", "thinkGear Disconnect");
			thinkGear.disconnect();
		}

		Log.d("EEGReader", "killProcess");
		android.os.Process.killProcess(Process.myPid());
	}
	
	private SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if(thinkGear != null){
				Toast.makeText(getApplicationContext(), "Already connected...", Toast.LENGTH_SHORT).show();

			}else{
				try{
					thinkGear = new ThinkGear();
					if(myView != null) thinkGear.setMyView(myView);
					thinkGear.connect();
					Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
				}catch(Exception ex){
					Toast.makeText(getApplicationContext(), "Cannot connect to ThinkGear - "+ex.toString(), Toast.LENGTH_LONG).show();
				}
			}
			return super.onSingleTapConfirmed(e);
		}
	};

/*
 * Public class MyView
 */
	public class MyView{
		B3Status b3status=null;
		TextView sigQ=null;
		Ball meditation = null;
		Ball attention = null;
		Eye blink = null;
		Bar[] eeg = new Bar[8];

		public MyView(){
			b3status = new B3Status(R.id.txtConnected);
			b3status.disconnected();
			sigQ = (TextView)findViewById(R.id.txtSigQ);
			meditation = new Ball(R.id.imgMeditation);
			attention = new Ball(R.id.imgAttention);
			blink =  new Eye(R.id.imgBlink);
			eeg[0] = new Bar(R.id.imgEEG01);
			eeg[1] = new Bar(R.id.imgEEG02);
			eeg[2] = new Bar(R.id.imgEEG03);
			eeg[3] = new Bar(R.id.imgEEG04);
			eeg[4] = new Bar(R.id.imgEEG05);
			eeg[5] = new Bar(R.id.imgEEG06);
			eeg[6] = new Bar(R.id.imgEEG07);
			eeg[7] = new Bar(R.id.imgEEG08);
		}
		public void b3status(String st){
			if      (st.equals("connected")){
				b3status.connected();
			}else if(st.equals("disconnected")){
				b3status.disconnected();
			}else if(st.equals("connecting")){
				b3status.connecting();
			}else{
				b3status.set(st);
			}
		}
		public void sigQ(int value){
			sigQ.setText(Integer.toString(value));
		}
		public void meditation(int to){
			if(to <= 0) return;
			meditation.scale((float) (to / 10.0));
		}
		public void attention(int to){
			if(to <= 0) return;
			attention.scale((float) (to / 10.0));
		}
		public void blink(int to){
			if(to <= 0) return;
			float f = (float) (to / 10.0);
			if(f > 10.0) f = 10.0f;
			blink.scale(f);
		}
		public void eeg(int[] val){
			for (int i = 0; i < val.length; i++) {
				if(val[i] <= 0) continue;
				eeg[i].scale(scaledEeg(val[i]));
			}
		}
		private float scaledEeg(int val){
			float eegVal = (float)((int)(val / 1000));
			return (eegVal < 1.0) ? 1.0f : (eegVal > 20.0) ? 20.0f : eegVal;
		}
	}

	private class B3Status{
		TextView txtStatus=null;

		public B3Status(int rid){
			txtStatus = (TextView)findViewById(rid);
		}
		public void set(String st){
			txtStatus.setVisibility(View.INVISIBLE);
			txtStatus.setText(st);
			txtStatus.setVisibility(View.VISIBLE);
		}
		public void connected(){
			txtStatus.setVisibility(View.INVISIBLE);
			txtStatus.setText(R.string.connected);
			txtStatus.setBackgroundColor(Color.GREEN);
			txtStatus.setVisibility(View.VISIBLE);
		}
		public void disconnected(){
			txtStatus.setVisibility(View.INVISIBLE);
			txtStatus.setText(R.string.disconnected);
			txtStatus.setBackgroundColor(Color.RED);
			txtStatus.setVisibility(View.VISIBLE);
		}
		public void connecting(){
			txtStatus.setVisibility(View.INVISIBLE);
			txtStatus.setText(R.string.connecting);
			txtStatus.setBackgroundColor(Color.MAGENTA);
			txtStatus.setVisibility(View.VISIBLE);
		}
	}

	private class Ball{
		ImageView img = null;
		float now = 1.0f;

		public Ball(int rid){
			img = (ImageView)findViewById(rid);
			img.setAlpha(100); // 動いていない時は、半透明
		}
		public void scale(float to){
			ScaleAnimation scale = new ScaleAnimation(
					now, to, // x
					now, to, // y
					Animation.RELATIVE_TO_SELF, 0.5f, // 中心(x)を原点に拡大縮小
					Animation.RELATIVE_TO_SELF, 0.5f  // 中心(y)を原点に拡大縮小
			);
			scale.setDuration(1000);  // アニメーション時間=1000msec
			scale.setFillAfter(true); // アニメーション後の状態を維持
			img.setAlpha(255); // 非透明
			img.startAnimation(scale); // 動作開始
			now = to;
		}
	}

	private class Bar{
		ImageView img = null;
		float now = 1.0f;

		public Bar(int rid){
			img = (ImageView)findViewById(rid);
			img.setAlpha(100); // 動いていない時は、半透明
		}
		public void scale(float to){
			ScaleAnimation scale = new ScaleAnimation(
					now,  to,  // x
					1.0f, 1.0f // y方向（縦）は固定
			);
			scale.setDuration(1000);  // アニメーション時間=1000msec
			scale.setFillAfter(true); // アニメーション後の状態を維持
			img.setAlpha(255); // 非透明
			img.startAnimation(scale); // 動作開始
			now = to;
		}
	}

	private class Eye{
		ImageView img = null;

		public Eye(int rid){
			img = (ImageView)findViewById(rid);
			img.setAlpha(100); // 動いていない時は、半透明
		}
		public void scale(float to){
			ScaleAnimation scale = new ScaleAnimation(
					1.0f, 1.0f, // x方向（横）は固定
					1.0f, to,   // y
					Animation.RELATIVE_TO_SELF, 0.5f, // 中心(x)を原点に拡大縮小
					Animation.RELATIVE_TO_SELF, 0.5f  // 中心(y)を原点に拡大縮小
			);
			scale.setDuration(500);  // アニメーション時間=500msec
			// アニメーション後の状態は維持しないで、元の形状に戻るように指定
			scale.setRepeatCount(1); // 繰り返し回数=1回
			scale.setRepeatMode(Animation.REVERSE); // 反復
			img.setAlpha(255); // 非透明
			img.startAnimation(scale); // 動作開始
		}
	}

}
