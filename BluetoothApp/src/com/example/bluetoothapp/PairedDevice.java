package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;

public class PairedDevice {

	BluetoothDevice device;
	boolean paired;
	
	
	public PairedDevice(BluetoothDevice device, boolean paired){
		
		this.device=device;
		this.paired=paired;
		
	}


	public BluetoothDevice getDevice() {
		return device;
	}


	public boolean isPaired() {
		return paired;
	}
	
	@Override
    public String toString() {
        return device.getName()+"\n"+device.getAddress();
    }
	
}
