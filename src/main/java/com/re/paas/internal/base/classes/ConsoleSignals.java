package com.re.paas.internal.base.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.re.paas.internal.base.core.Exceptions;


public class ConsoleSignals {

	private static final Map<String, Runnable> signalHooks = new HashMap<>();

	public static void addHook(String signal, Runnable hook) {
		signalHooks.put(signal, hook);
	}
	
	public static void removeHook(String signal) {
		signalHooks.remove(signal);
	}

	public static void start() {

		new Thread(() -> {

				try {

					String outLine;
					BufferedReader errInput = new BufferedReader(new InputStreamReader(System.in));

					while ((outLine = errInput.readLine()) != null && (outLine = outLine.trim()) != null) {
						if(signalHooks.containsKey(outLine)) {
							signalHooks.get(outLine).run();
						}
					}
					errInput.close();

				} catch (IOException e) {
					Exceptions.throwRuntime(e);
				}
		}).start();

	}

}
