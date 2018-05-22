package com.re.paas.internal.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.re.paas.internal.base.classes.TimeUnit;
import com.re.paas.internal.base.classes.ClientResources.HtmlCharacterEntities;
import com.re.paas.internal.base.core.Exceptions;

public class Utils {

	private static SecureRandom secureRandom = new SecureRandom();

	public static final String newRandom() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static final String newShortRandom() {
		return newRandom().substring(0, 6);
	}

	public static String join(String arr[], int startIndex) {
		StringBuilder sb = new StringBuilder(24);
		for (int i = startIndex; i < arr.length; i++) {
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	public static JsonObject getJson(InputStream in) {
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(getString(in)).getAsJsonObject();
		return o;
	}
	
	public static String getString(String fileName) throws IOException {

		InputStream in = new FileInputStream(new File(fileName));
		return getString(in);
	}
	
	public static Properties getProperties(InputStream in) {
		Properties o = new Properties();
		try {
			o.load(in);
			return o;
		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}
		return null;
	}

	public static File asFile(URL uri) {

		File tempFile = null;
		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), null);

			InputStream in = uri.openStream();
			OutputStream out = Files.newOutputStream(tempFile.toPath());

			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}

		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}
		return tempFile;
	}

	public static String newSecureRandom() {
		return new BigInteger(130, secureRandom).toString(32);
	}

	/**
	 * This returns a set of randomly generated bytes
	 */
	public static byte[] randomBytes(int length) {
		byte[] b = new byte[length];
		SecureRandom rand = secureRandom;
		rand.nextBytes(b);
		return b;
	}

	public static Boolean isPortAvailable(InetAddress host, Integer port) {
		/*
		 * try { new Socket(host, port).close(); return true; } catch (SocketException
		 * e) { return false; } catch (IOException e) { Exceptions.throwRuntime(e);
		 * return false; }
		 */

		// Let's catch any exceptions, when the server attempts to start
		return true;
	}

	public static String[] readLines(File o) throws IOException {

		List<String> lines = new ArrayList<String>();

		Charset charset = Charset.forName("UTF-8");
		BufferedReader reader = Files.newBufferedReader(Paths.get(o.toURI()), charset);
		String line = null;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		reader.close();
		return lines.toArray(new String[lines.size()]);
	}

	public static String[] readLines(InputStream in) throws IOException {

		List<String> lines = new ArrayList<String>();

		Charset charset = Charset.forName("UTF-8");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
		String line = null;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		in.close();
		return lines.toArray(new String[lines.size()]);
	}

	public static String getString(InputStream in) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int c;
			while ((c = in.read()) != -1) {
				baos.write(c);
			}
			in.close();
		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}
		return baos.toString();
	}

	public static void saveString(String o, OutputStream out) throws IOException {

		StringReader in = new StringReader(o);
		int c;
		while ((c = in.read()) != -1) {
			out.write(c);
		}

		in.close();
		out.close();
	}

	public static void saveString(String o, String fileName) throws IOException {
		OutputStream out = new FileOutputStream(new File(fileName));
		saveString(o, out);
	}

	public static void copyTo(InputStream in, OutputStream out) throws IOException {
		int c;
		while ((c = in.read()) != -1) {
			out.write(c);
		}
		in.close();
		out.close();
	}

	public static String getArgument(String[] args, String key) {

		for (String arg : args) {
			if (arg.startsWith("-" + key + "=")) {
				return arg.split("=")[1];
			}
		}
		return null;
	}

	public static Boolean hasFlag(String[] args, String key) {

		for (String arg : args) {
			if (arg.equals("-" + key) || arg.equals("--" + key)) {
				return true;
			}
		}
		return false;
	}

	public static String toMACAddress(byte[] mac) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
		}
		return sb.toString().toLowerCase();
	}

	public static byte[] decompressBytes(final byte[] array) {
		try {
			final Inflater inflater = new Inflater();
			inflater.setInput(array, 0, array.length);
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(array.length);
			final byte[] array2 = new byte[1024];
			while (!inflater.finished()) {
				byteArrayOutputStream.write(array2, 0, inflater.inflate(array2));
			}
			byteArrayOutputStream.close();
			final byte[] byteArray = byteArrayOutputStream.toByteArray();
			inflater.end();
			return byteArray;
		} catch (IOException | DataFormatException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static byte[] compressBytes(final byte[] bytes) {
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bytes.length);
			final Deflater deflater = new Deflater();
			deflater.setInput(bytes);
			deflater.finish();
			final byte[] array = new byte[1024];
			while (!deflater.finished()) {
				byteArrayOutputStream.write(array, 0, deflater.deflate(array));
			}
			byteArrayOutputStream.close();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static final String prettify(String input) {

		input = input.toLowerCase().replace("_", " ");

		if (!input.contains(" ")) {
			return input.substring(0, 1).toUpperCase() + input.substring(1);
		}

		StringBuilder output = new StringBuilder();

		for (String word : input.split("[ ]+")) {
			output.append(word.substring(0, 1).toUpperCase() + word.substring(1)).append(" ");
		}

		return output.toString().trim();
	}

	public static Integer[] indexes(int count) {

		List<Integer> indexes = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			indexes.add(i);
		}

		return indexes.toArray(new Integer[indexes.size()]);
	}

	public static ArrayList<ArrayList<Integer>> permute(Integer[] num) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

		// start from an empty list
		result.add(new ArrayList<Integer>());

		for (int i = 0; i < num.length; i++) {
			// list of list in current iteration of the array num
			ArrayList<ArrayList<Integer>> current = new ArrayList<ArrayList<Integer>>();

			for (ArrayList<Integer> l : result) {
				// # of locations to insert is largest index + 1
				for (int j = 0; j < l.size() + 1; j++) {
					// + add num[i] to different locations
					l.add(j, num[i]);

					ArrayList<Integer> temp = new ArrayList<Integer>(l);
					current.add(temp);

					// System.out.println(temp);

					// - remove num[i] add
					l.remove(j);
				}
			}

			result = new ArrayList<ArrayList<Integer>>(current);
		}

		return result;
	}

	public static String ordinal(Integer i) {
		String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
		switch (i % 100) {
		case 11:
		case 12:
		case 13:
			return i + "th";
		default:
			return i + sufixes[i % 10];
		}
	}

	public static String truncate(String input, int wordCount) {
		StringBuilder output = new StringBuilder();
		String arr[] = input.split("[ ]+");

		if (wordCount > arr.length) {
			wordCount = arr.length;
		}

		for (int i = 0; i < wordCount; i++) {
			String word = arr[i];
			output.append(word).append(" ");
		}
		return output.toString() + " ...";
	}

	public static String getTimeOffsetAsString(TimeUnit unit, Long offset) {

		boolean isNegative = offset < 0;

		return !isNegative ? "in" + HtmlCharacterEntities.SPACE
				: "" + Math.abs(offset) + HtmlCharacterEntities.SPACE + unit.name().toLowerCase()
						+ (isNegative ? HtmlCharacterEntities.SPACE + "ago" : "");
	}

	public static Long getTimeOffset(TimeUnit unit, Date to) {
		return getTimeOffset(unit, null, to);
	}
	
	public static Date toDate(LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return date;
	}
	
	public static Long getTimeOffset(TimeUnit unit, Date from, Date to) {

		LocalDate now = from == null ? LocalDate.now() : from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate execTime = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		
		return getTimeOffset(unit, now, execTime);
	}
	
	public static Long getTimeOffset(TimeUnit unit, LocalDate now, LocalDate execTime) {

		Duration duration = Duration.between(now, execTime);
		Long diff = null;

		switch (unit) {
		case DAYS:
			diff = duration.toDays();
			break;
		case HOURS:
			diff = duration.toHours();
			break;
		case MILLIS:
			diff = duration.toMillis();
			break;
		case MINUTES:
			diff = duration.toMinutes();
			break;
		case NANOS:
			diff = duration.toNanos();
			break;
		}

		return diff;
	}

	public static String getUserFragment(String uri) {
	
		Pattern handleId = Pattern.compile("/[a-zA-Z0-9]+([-_\\Q.\\E]*[a-zA-Z0-9]+)*\\z");
	
		Matcher m = handleId.matcher(uri);
		if (m.find()) {
			return m.group().replaceFirst("\\Q/\\E", "");
		}
	
		throw new IllegalArgumentException("Unable to parse the social handle id from uri: " + uri);
	}

}
