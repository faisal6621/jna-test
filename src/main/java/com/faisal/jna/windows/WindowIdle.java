package com.faisal.jna.windows;

import java.util.Calendar;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.LASTINPUTINFO;

public class WindowIdle {
	enum State {
		ONLINE, IDLE, AWAY, UNKNOWN
	}

	public static void main(String[] args) {
		State oldState = State.ONLINE;
		State newState = State.ONLINE;
		while (true) {
			int idleSec = getIdleTimeMillis() / 1000;
			switch (idleSec) {
			case 1:
				newState = State.ONLINE;
				break;
			case 10:
				newState = State.IDLE;
				break;
			case 20:
				newState = State.AWAY;
				break;
			case 30:
				newState = State.UNKNOWN;
				break;
			}
			if (oldState != newState) {
				oldState = newState;
				System.out.println(Calendar.getInstance().getTime() + " :: " + oldState);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static int getIdleTimeMillis() {
		LASTINPUTINFO plii = new LASTINPUTINFO();
		User32.INSTANCE.GetLastInputInfo(plii);
		return Kernel32.INSTANCE.GetTickCount() - plii.dwTime;
	}

}
