package com.faisal.jna.windows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import com.sun.jna.platform.win32.Wtsapi32;

public class WindowsMonitor implements WindowProc {

	private static final int WM_POWERBROADCAST = 536;
	private static final int PBT_APMPOWERSTATUSCHANGE = 10;
	private static final int PBT_APMRESUMESUSPEND = 7;
	private static final int PBT_APMSUSPEND = 4;
	private static final int PBT_POWERSETTINGCHANGE = 32787;

	private String msg;

	private WindowsMonitor() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String winClass = "WindowsMonitor";
				HMODULE hInst = Kernel32.INSTANCE.GetModuleHandle("");

				WNDCLASSEX wClass = new WNDCLASSEX();
				wClass.hInstance = hInst;
				wClass.lpfnWndProc = WindowsMonitor.this;
				wClass.lpszClassName = winClass;

				// register window class
				User32.INSTANCE.RegisterClassEx(wClass);

				// create new window
				HWND hWnd = User32.INSTANCE.CreateWindowEx(User32.WS_EX_TOPMOST, winClass,
						"My hidden helper window, used only to catch the windows events", 0, 0, 0, 0, 0, null, // WM_DEVICECHANGE
																												// contradicts
																												// parent=WinUser.HWND_MESSAGE
						null, hInst, null);

				Wtsapi32.INSTANCE.WTSRegisterSessionNotification(hWnd, Wtsapi32.NOTIFY_FOR_THIS_SESSION);

				MSG msg = new MSG();
				while (User32.INSTANCE.GetMessage(msg, hWnd, 0, 0) != 0) {
					User32.INSTANCE.TranslateMessage(msg);
					User32.INSTANCE.DispatchMessage(msg);
				}

				Wtsapi32.INSTANCE.WTSUnRegisterSessionNotification(hWnd);
				User32.INSTANCE.UnregisterClass(winClass, hInst);
				User32.INSTANCE.DestroyWindow(hWnd);

			}
		}).start();
	}

	@Override
	public LRESULT callback(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
		// System.out.println(hwnd + " " + uMsg + " " + wParam + " " + lParam);
		switch (uMsg) {
		case WM_POWERBROADCAST: {
			this.onPowerChange(wParam, lParam);
			return new LRESULT(0);
		}
		case WinUser.WM_DESTROY: {
			User32.INSTANCE.PostQuitMessage(0);
			return new LRESULT(0);
		}
		case WinUser.WM_SESSION_CHANGE: {
			this.onSessionChange(wParam, lParam);
			return new LRESULT(0);
		}
		default:
			log(" :: default :: msg: " + uMsg + " :: wparam: " + wParam);
			return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
		}

	}

	private void onSessionChange(WPARAM wParam, LPARAM lParam) {
		switch (wParam.intValue()) {
		case Wtsapi32.WTS_SESSION_LOGON: {
			msg = " :: logon";
			log(msg);
			break;
		}
		case Wtsapi32.WTS_SESSION_LOGOFF: {
			msg = " :: logoff";
			log(msg);
			break;
		}
		case Wtsapi32.WTS_SESSION_LOCK: {
			msg = " :: lock";
			log(msg);
			break;
		}
		case Wtsapi32.WTS_SESSION_UNLOCK: {
			msg = " :: unlock";
			log(msg);
			break;
		}
		}
	}

	private void onPowerChange(WPARAM wParam, LPARAM lParam) {
		log(" :: power :: wparam: " + wParam + " :: lparam: " + lParam);
	}

	private void log(final String msg) {
		Calendar calendar = Calendar.getInstance();
		Date time = calendar.getTime();
		String homeDir = System.getProperty("user.home");
		String jnaDir = homeDir + File.separator + "jna";
		File jna = new File(jnaDir);
		if (!jna.exists()) {
			jna.mkdir();
			System.out.println("directory created: " + jna.getAbsolutePath());
		}
		File file = new File(jnaDir + File.separator + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH)
				+ "-" + calendar.get(Calendar.DAY_OF_MONTH) + ".log");
		try {
			file.createNewFile();
			System.out.println("file created: " + file.getAbsolutePath());
			FileWriter writer = new FileWriter(file, true);
			writer.write(time + msg + System.lineSeparator());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new WindowsMonitor();
	}

}
