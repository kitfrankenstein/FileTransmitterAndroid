package com.kit.filetransmitter.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kit.filetransmitter.R;
import com.kit.filetransmitter.entity.MessageItem;
import com.kit.filetransmitter.util.Topics;

import java.util.List;

/**
 * Created by Kit
 * Time: 2019/9/30
 */
public class MessageAdapter extends RecyclerView.Adapter{

	private Context context;
	private List<MessageItem> itemList;

	public MessageAdapter(Context context, List<MessageItem> itemList) {
		this.context = context;
		this.itemList = itemList;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder = null;
		switch (viewType){
			case Topics.RECV:
				View view = LayoutInflater.from(context).inflate(R.layout.recv_item,parent,false);
				holder = new RecvViewHolder(view);
				break;
			case Topics.SEND:
				View view1 = LayoutInflater.from(context).inflate(R.layout.send_item,parent,false);
				holder = new SendViewHolder(view1);
				break;
		}
		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		int itemViewType = getItemViewType(position);
		switch (itemViewType){
			case Topics.RECV:
				RecvViewHolder recvViewHolder = (RecvViewHolder) holder;
				recvViewHolder.recvMsg.setText(itemList.get(position).getMessage());
				recvViewHolder.recvTime.setText(itemList.get(position).getTime());
				break;
			case Topics.SEND:
				SendViewHolder sendViewHolder = (SendViewHolder) holder;
				sendViewHolder.sendMsg.setText(itemList.get(position).getMessage());
				sendViewHolder.sendTime.setText(itemList.get(position).getTime());
				break;
		}
	}

	@Override
	public int getItemCount() {
		return itemList == null ? 0 : itemList.size();
	}


	@Override
	public int getItemViewType(int position) {
		return this.itemList.get(position).getType();
	}

	class SendViewHolder extends RecyclerView.ViewHolder{
		private TextView sendMsg;
		private TextView sendTime;
		SendViewHolder(View itemView) {
			super(itemView);
			sendMsg = itemView.findViewById(R.id.send_msg_tv);
			sendTime = itemView.findViewById(R.id.send_time_tv);
		}
	}

	class RecvViewHolder extends RecyclerView.ViewHolder{
		private TextView recvMsg;
		private TextView recvTime;
		RecvViewHolder(View itemView) {
			super(itemView);
			recvMsg = itemView.findViewById(R.id.recv_msg_tv);
			recvTime = itemView.findViewById(R.id.recv_time_tv);
		}
	}

}
