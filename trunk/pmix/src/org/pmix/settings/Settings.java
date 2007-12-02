package org.pmix.settings;

public final class Settings {

	private String serverAddress = "192.168.0.20";
	

	private static Settings instance = new Settings();

	private Settings() {

	}

	public static Settings getInstance() {
		return instance;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

}
