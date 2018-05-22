package com.re.paas.internal.clustering.nic_utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import com.google.common.collect.Lists;
import com.re.paas.internal.base.core.Exceptions;

public class NIC {

	/**
	 * Scans all network interfaces , to check if any addresses found matches the
	 * one provided
	 * 
	 * @throws SocketException
	 */
	public static NetworkInterface getMatchingNIC(InetAddress address) throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

		while (interfaces.hasMoreElements()) {
			NetworkInterface iface = interfaces.nextElement();
			Enumeration<InetAddress> addresses = iface.getInetAddresses();

			while (addresses.hasMoreElements()) {
				if (addresses.nextElement().equals(address)) {
					return iface;
				}
			}
		}
		return NetworkInterface.getByName("eth0");
	}

	public static List<InetAddress> getAllAddress() {

		List<InetAddress> result = Lists.newArrayList();

		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			Exceptions.throwRuntime(e);
		}

		while (interfaces.hasMoreElements()) {
			NetworkInterface iface = interfaces.nextElement();
			Enumeration<InetAddress> addresses = iface.getInetAddresses();

			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				result.add(address);
			}
		}

		return result;
	}

	private static boolean isPrivateAddress(InetAddress address) {
		return address.isSiteLocalAddress() && !address.isLoopbackAddress();
	}

	private static boolean isPublicAddress(InetAddress address) {
		return !(address.isSiteLocalAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress()
				|| address.isLoopbackAddress() || address.isMulticastAddress());
	}
	
	public static boolean isInetAddressPublic(InetAddress address) {
		return NIC.isPublicAddress(address)  && !IPAddresses.isWithinPrivateRange(address);
	}
	
	public static boolean isInetAddressPrivate(InetAddress address) {
		return !NIC.isPrivateAddress(address)  && IPAddresses.isWithinPrivateRange(address);
	}

}
