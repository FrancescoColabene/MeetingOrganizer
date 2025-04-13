package it.polimi.tiw.utils;

import java.util.Comparator;

import it.polimi.tiw.beans.Meeting;

public class MeetingDateComparator implements Comparator<Meeting>{

	@Override
	public int compare(Meeting o1, Meeting o2) {
		long diff = o1.getDataStart().getTime() - o2.getDataStart().getTime();
		if( diff > 0) {
			return 1;
		}
		if( diff < 0 ) {
			return -1;
		}
		diff = o1.getHourStart().getTime() - o2.getHourStart().getTime();
		if( diff > 0) {
			return 1;
		}
		if( diff < 0 ) {
			return -1;
		}
		return 0;
	}
	
}
