/**
 * 
 */
package com.example.bluetoothapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Luis Miguel
 * 
 */
public class Chat extends Fragment {

	onChatActivityListener onActivityListener;

	public interface onChatActivityListener {

		void onChatActivityCreated();
		void onChatActivityDestroyed();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		onActivityListener = (onChatActivityListener) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.activity_chat, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		onActivityListener.onChatActivityCreated();

	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		onActivityListener.onChatActivityDestroyed();
	}

}
