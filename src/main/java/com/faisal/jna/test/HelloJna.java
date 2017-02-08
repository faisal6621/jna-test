package com.faisal.jna.test;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class HelloJna {

	public interface CLibrary extends Library {
		final CLibrary INSTANCE = Native.loadLibrary(Platform.isWindows() ? "msvcrt" : "c", CLibrary.class);

		void printf(String format, Object... args);
	}

	public static void main(String[] args) {
		CLibrary.INSTANCE.printf("%s", "Hello JNA");
	}

}
