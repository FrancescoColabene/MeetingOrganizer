package it.polimi.tiw.beans;

public class Person {

	private int id;
	private String email;
	private String name;
	private String surname;
	private String username;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String username) {
		this.email = username;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	
	@Override
	public boolean equals(Object obj) {
		return email.equals(((Person) obj).getEmail());
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", email=" + email + ", name=" + name + ", surname=" + surname + "]";
	}



	
	
}
