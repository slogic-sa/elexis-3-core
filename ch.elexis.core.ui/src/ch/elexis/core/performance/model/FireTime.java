package ch.elexis.core.performance.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "fireTime")
@XmlAccessorType(XmlAccessType.FIELD)
public class FireTime {
	private double min;
	private double max;
	private double mean;
	@XmlTransient
	private List<Long> times = new ArrayList<Long>();
	
	public double getMin(){
		return min;
	}
	
	public void setMin(double min){
		this.min = min;
	}
	
	public double getMax(){
		return max;
	}
	
	public void setMax(double max){
		this.max = max;
	}
	
	public double getMean(){
		mean = 0;
		if (times.isEmpty()) {
			return 0;
		}
		
		for (Long time : times) {
			mean += time;
		}
		return mean / times.size();
	}
	
	public void setMean(double mean){
		this.mean = mean;
	}
	
	public void addTime(long time){
		times.add(time);
		setMin(calculateMin());
		setMax(calculateMax());
		setMean(calculateMean());
	}
	
	private double calculateMin(){
		double minValue = times.get(0);
		for (Long time : times) {
			if (time < minValue) {
				minValue = time;
			}
		}
		return minValue;
	}
	
	private double calculateMax(){
		double maxValue = times.get(0);
		for (Long time : times) {
			if (time > maxValue) {
				maxValue = time;
			}
		}
		return maxValue;
	}
	
	private double calculateMean(){
		double sum = 0;
		for (Long time : times) {
			sum += time;
		}
		return sum / times.size();
	}
}
