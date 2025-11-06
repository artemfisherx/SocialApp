package mainPackage.Entities;

import java.time.LocalDate;

import mainPackage.Enums.HoroscopeSign;

public class Horoscope {
	
	private int id;
	private HoroscopeSign sign;
	private LocalDate date;
	private String text;
	
	public Horoscope(int id, HoroscopeSign sign, LocalDate date, String text)
	{
		this(sign, date, text);
		this.id = id;
	}
	
	public Horoscope(HoroscopeSign sign, LocalDate date, String text)
	{
		this.sign = sign;
		this.date = date;
		this.text = text;		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public HoroscopeSign getSign() {
		return sign;
	}
	public void setSign(HoroscopeSign sign) {
		this.sign = sign;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public String toString()
	{
		return this.sign.toString() + " " + this.date;
	}
	
}
