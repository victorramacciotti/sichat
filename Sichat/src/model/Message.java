package model;
import java.io.File;

public class Message {
	private int id;
	private String content;
	private File file;
	
	public Message(int id, String content, File file) {
		this.id = id;
		this.content = content;
		this.file = file;
	}
	
	/*GETTERS*/
	
	public int getId() {
		return this.id;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public File getFile() {
		return this.file;
	}
	
	/*SETTERS*/

	public void setId(int id) {
		this.id = id;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
}




