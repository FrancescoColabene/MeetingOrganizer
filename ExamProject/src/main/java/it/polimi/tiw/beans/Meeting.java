package it.polimi.tiw.beans;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.List;

public class Meeting {

	private int id;
	private int idHost;
	private String fullNameHost;
	private int counter;
	private String title;
	private Date dataStart;
	private Time hourStart;
	private Time duration;
	private int max;
	private List<Integer> idPeople;
	
	public Meeting() {}
	
	public Meeting(int idHost, int counter, String title, java.util.Date dataStart, java.util.Date hourStart, java.util.Date duration, int max) {
		this.idHost = idHost;
		this.counter = counter;
		this.title = title;
		this.dataStart = new java.sql.Date(dataStart.getTime());
		this.hourStart = new Time(hourStart.getTime());
		this.duration = new Time(duration.getTime());
		this.max = max;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCounter() {
		return counter;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	public int getIdHost() {
		return idHost;
	}
	public void setIdHost(int idHost) {
		this.idHost = idHost;
	}
	public String getFullNameHost() {
		return fullNameHost;
	}
	public void setFullNameHost(String fullNameHost) {
		this.fullNameHost = fullNameHost;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getDataStart() {
		return dataStart;
	}
	public void setDataStart(Date dataStart) {
		this.dataStart = dataStart;
	}
	public Time getHourStart() {
		return hourStart;
	}
	public void setHourStart(Time hourStart) {
		this.hourStart = hourStart;
	}
	public Time getDuration() {
		return duration;
	}
	public void setDuration(Time duration) {
		this.duration = duration;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public List<Integer> getIdPeople(){
		return idPeople;
	}
	public void setIdPeople(List<Integer> idPeople) {
		this.idPeople = idPeople;
	}
	public String hourToString() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(hourStart);
	}
	public String durationToString() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(duration);
	}
}
