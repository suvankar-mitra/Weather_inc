package es.esy.iamsuvankar.weatherinc;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by suvankar on 12/11/17.
 */

public class Utility {
    public static int kelvinToCelcius(float k) {
        return Math.round(k -  273.15f);
    }

    public static String firstCharCaps(String s) {
        if(s==null||s.length()==0)
            return "";
        String fl = s.substring(0,1).toUpperCase();
        if(s.length()==1)
            return fl;
        else
            return fl+s.substring(1).toLowerCase();
    }

    public static String getDirection(double deg) {
        String di = "";
        if(deg>=348.75 || deg<=11.25) {
            di = "N";
        } else if(deg>=11.25 && deg<=33.75) {
            di = "NNE";
        } else if(deg>=33.75 && deg<=56.25) {
            di = "NE";
        } else if(deg>=56.25 && deg<=78.75) {
            di = "ENE";
        } else if(deg>=78.75 && deg<=101.25) {
            di = "E";
        } else if(deg>=101.25 && deg<=123.75) {
            di = "ESE";
        } else if(deg>=123.75 && deg<=146.25) {
            di = "SE";
        } else if(deg>=146.25 && deg<=168.75) {
            di = "SSE";
        } else if(deg>=168.75 && deg<=191.25) {
            di = "S";
        } else if(deg>=191.25 && deg<=213.75) {
            di = "SSW";
        } else if(deg>=213.75 && deg<=236.25) {
            di = "SW";
        } else if(deg>=236.25 && deg<=258.75) {
            di = "WSW";
        } else if(deg>=258.75 && deg<=281.25) {
            di = "W";
        } else if(deg>=281.25 && deg<=303.75) {
            di = "WNW";
        } else if(deg>=303.75 && deg<=326.25) {
            di = "NW";
        } else if(deg>=326.25 && deg<=348.75) {
            di = "NNW";
        }

        return di;
    }

    public static String getUTCtoISTTime(long time) {
        Date date = new Date(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hr = cal.get(Calendar.HOUR);
        int min = cal.get(Calendar.MINUTE);

        return (hr<10?"0"+hr:hr)+":"+(min<10?"0"+min:min);
    }
}
