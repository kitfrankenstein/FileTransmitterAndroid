package com.kit.filetransmitter.chat;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileChooser;
import com.kit.filetransmitter.R;
import com.kit.filetransmitter.adapter.MessageAdapter;
import com.kit.filetransmitter.entity.MessageItem;
import com.kit.filetransmitter.util.InfoUtil;
import com.kit.filetransmitter.util.MqttManager;
import com.kit.filetransmitter.util.Topics;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, ChatCallback {

	private RecyclerView chatView;
	private MqttManager mqttManager;
	private Handler handler;
	private MessageAdapter adapter;
	private List<MessageItem> itemList;
	private Button sendButton;
	private Button fileButton;
	private EditText sendText;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		chatView = findViewById(R.id.recyclerView);
		itemList = new ArrayList<>();
		adapter = new MessageAdapter(this, itemList);
		chatView.setAdapter(adapter);
		LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		chatView.setLayoutManager(manager);

		mqttManager = MqttManager.getInstance(this);
		mqttManager.creatConnect(InfoUtil.getClientID(), "bye");
		mqttManager.subscribe(Topics.PC + Topics.SEPARATOR + Topics.EXTRA_MORE, 1);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case Topics.RECV_TEXT:
						String recvText = (String)msg.obj;
						Date recvDate = new Date();
						MessageItem recvMessage = new MessageItem(recvText, recvDate, Topics.RECV);
						itemList.add(recvMessage);
						adapter.notifyDataSetChanged();
						break;
					case Topics.RECV_FILE:
						String recvFile = (String)msg.obj;
						Date recvFileDate = new Date();
						MessageItem recvFileMessage = new MessageItem(recvFile, recvFileDate, Topics.RECV);
						itemList.add(recvFileMessage);
						adapter.notifyDataSetChanged();
						break;
					case Topics.SEND_TEXT:
						sendText.getText().clear();
						String sendText = (String)msg.obj;
						Date sendDate = new Date();
						MessageItem sendMessage = new MessageItem(sendText, sendDate, Topics.SEND);
						itemList.add(sendMessage);
						adapter.notifyDataSetChanged();
						break;
					case Topics.SEND_FILE:
						String sendFile = (String)msg.obj;
						Date sendFileDate = new Date();
						MessageItem sendFileMessage = new MessageItem(sendFile + Topics.SEND_HINT, sendFileDate, Topics.SEND);
						itemList.add(sendFileMessage);
						adapter.notifyDataSetChanged();
						break;
					default:break;
				}
				chatView.scrollToPosition(itemList.size() - 1);
			}
		};

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		sendButton = findViewById(R.id.sendBtn);
		sendText = findViewById(R.id.sendText);
		sendButton.setOnClickListener(v -> {
			String message = sendText.getText().toString();
			if (InfoUtil.isValid(message) && InfoUtil.isValid(mqttManager)) {
				new Thread(() -> mqttManager.publish(Topics.MOBILE_TEXT, 0, message.getBytes())).start();
			}
		});

		fileButton = findViewById(R.id.fileBtn);
		fileButton.setOnClickListener(v -> {
			//requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//			intent.setType("*/*");
//			startActivityForResult(intent, Topics.SEND_FILE);
			Intent i2 = new Intent(getApplicationContext(), FileChooser.class);
			i2.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
			startActivityForResult(i2, Topics.SEND_FILE);
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isFinishing()) {
			MqttManager.release();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {  //把操作放在用户点击的时候
			if (isShouldHideKeyboard(chatView, ev)) { //判断用户点击的是否是输入框以外的区域
				hideKeyboard(chatView.getWindowToken());   //收起键盘
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean isShouldHideKeyboard(View v, MotionEvent event) {
		int[] l = {0, 0};
		v.getLocationInWindow(l);
		int left = l[0],    //得到输入框在屏幕中上下左右的位置
				top = l[1],
				bottom = top + v.getHeight(),
				right = left + v.getWidth();
		if (event.getX() > left && event.getX() < right
				&& event.getY() > top && event.getY() < bottom) {
			// 点击位置如果是chatView的区域收起键盘。
			return true;
		} else {
			return false;
		}
	}

	private void hideKeyboard(IBinder token) {
		if (token != null) {
			InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Topics.SEND_FILE && data != null) {
			if (resultCode == RESULT_OK) {
				try {
					Uri uri = data.getData();
					File file = new File(uri.getPath());
					byte[] payload = FileUtils.readFileToByteArray(file);
					new Thread(() -> mqttManager.publish(Topics.MOBILE_FILE + file.getName(), 0, payload)).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			openAssignFolder(getApplicationContext().getExternalFilesDir(null));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_camera) {
			// Handle the camera action
		} else if (id == R.id.nav_gallery) {

		} else if (id == R.id.nav_slideshow) {

		} else if (id == R.id.nav_manage) {

		} else if (id == R.id.nav_share) {

		} else if (id == R.id.nav_send) {

		}

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onMessageArrive(String topic, byte[] payload) {
		String[] topics = topic.split(Topics.SEPARATOR);
		Message message = new Message();
		if (topics.length == 2) {
			message.what = Topics.RECV_TEXT;
			message.obj = new String(payload);
		} else if (topics.length == 3) {
			message.what = Topics.RECV_FILE;
			File file = new File(getApplicationContext().getExternalFilesDir(null), topics[2]);
			try {
				FileUtils.writeByteArrayToFile(file, payload);
				message.obj = topics[2] + Topics.RECV_HINT;
			} catch (IOException e) {
				e.printStackTrace();
				message.obj = topics[2] + Topics.RECV_FAIL;
			}
		} else {
			message.what = Topics.ERROR;
		}
		handler.sendMessage(message);
	}

	@Override
	public void onDeliveryComplete(String topic, byte[] payload) {
		String[] topics = topic.split(Topics.SEPARATOR);
		Message message = new Message();
		if (topics.length == 2) {
			message.what = Topics.SEND_TEXT;
			message.obj = new String(payload);
		} else if (topics.length == 3) {
			message.what = Topics.SEND_FILE;
			message.obj = topics[2];
		} else {
			message.what = Topics.ERROR;
		}
		handler.sendMessage(message);
	}

	private void openAssignFolder(File folder){
		if(!folder.exists() || !folder.isDirectory()){
			return;
		}
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".util.GenericFileProvider", folder);
		intent.setDataAndType(uri, "*/*");
		try {
			startActivity(Intent.createChooser(intent, "Open folder"));
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}
}
