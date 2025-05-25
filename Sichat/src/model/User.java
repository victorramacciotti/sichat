package model;

public class User {
	private int id;
	private String ip;
	private String name;
	private int port;
	
	public User(int id, String ip, String name, int port) {
		this.id = id;
		this.ip = ip;
		this.name = name;
		this.port = port;
	}
	
	/*GETTERS*/
	public int getId() {
		return this.id;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getPort() {
		return this.port;
	}
	
	/*SETTERS*/
	public void setId(int id) {
		this.id = id;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}
