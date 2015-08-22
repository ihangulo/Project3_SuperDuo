package barqsoft.footballscores;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.format.Time;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *   ================================================
 *       SuperDuo: Football Scores
 *       Utility.java
 *
 *
 *   ================================================
 *     Kwanghyun JUNG   ihangulo@gmail.com  AUG.2015
 *      Original version Created by yehya khaled on  3/3/2015.
 **/

public class Utility {

    public static final int BUNDESLIGA1 = 394; //http://api.football-data.org/alpha/soccerseasons/394"
    public static final int BUNDESLIGA2 = 395;  // "http://api.football-data.org/alpha/soccerseasons/395"
    public static final int LIGUE_1 = 396; //http://api.football-data.org/alpha/soccerseasons/396"
    public static final int LIGUE_2 = 397; //http://api.football-data.org/alpha/soccerseasons/396"
    public static final int PREMIER_LEGAUE = 398; // "http://api.football-data.org/alpha/soccerseasons/398"

    public static final int CHAMPIONS_LEAGUE = 362; // no..

    public static final int ONEDAY_MS = 86400000;
    public static final int HALFDAY_MS = ONEDAY_MS/2;


    public static String getLeague(int league_num) {
        switch (league_num) {

            case BUNDESLIGA1:
                return "1. Bundesliga 2015/16";
            case BUNDESLIGA2:
                return "2. Bundesliga 2015/16";
            case LIGUE_1:
                return "Ligue 1 2015/16";
            case LIGUE_2:
                return "Ligue 2 2015/16";
            case PREMIER_LEGAUE:
                return "Premier League 2015/16";
            default:
                return "Not known League Please report";
        }
    }

    public static String getMatchDay(int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return "Group Stages, Matchday : 6";
            } else if (match_day == 7 || match_day == 8) {
                return "First Knockout round";
            } else if (match_day == 9 || match_day == 10) {
                return "QuarterFinal";
            } else if (match_day == 11 || match_day == 12) {
                return "SemiFinal";
            } else {
                return "Final";
            }
        } else {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int away_goals) {
        if (home_goals < 0 || away_goals < 0) {
            return " - ";
        } else {
            String homeScore= NumberFormat.getInstance().format(home_goals); // change to locale number
            String awayScore=NumberFormat.getInstance().format(away_goals);

            // it is changed the direction by locale
            // if LTR then it returns home_goals - away_goals (left to right order)
            // if RTL then it returns away_golas - home_goals (right to left order)
            // but if it is added some english character, it returns always original order(left to right)

            return homeScore + " - " + awayScore;

        }
    }


    /*
    public static String getModifiedTime(Locale locale, long modified) {
        SimpleDateFormat dateFormat = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            dateFormat = new SimpleDateFormat( DateFormat.getBestDateTimePattern(locale, "MM/dd/yyyy hh:mm:ss aa"));
            return "ddd";
        }
        return null;

    }*/

    public static int getTeamCrestByTeamName (String teamname)
    {
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname)
        {
            case "AFC Bournemouth": return R.drawable.bournemouth; // BOURNEMOUTH, BOU
            case "Arsenal London FC" : return R.drawable.arsenal;// ARSENAL, ARS
            case "Aston Villa FC" : return R.drawable.aston_villa; //ASTON VILLA, AVL
            case "Burnley FC" : return R.drawable.burney_fc_hd_logo;
            case "Chelsea FC" : return R.drawable.chelsea;// CHELSEA, CHE
            case "Crystal Palace FC" : return R.drawable.crystal_palace_fc;// CRYSTAL PALACE, CRY
            case "Everton FC" : return R.drawable.everton_fc_logo1; // EVERTON, EVE
            case "Hull City AFC" : return R.drawable.hull_city_afc_hd_logo;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;// LEICESTER, LEI
            case "Liverpool FC" : return R.drawable.liverpool; // LIVERPOOL, LIV
            case "Manchester City FC"  : return R.drawable.manchester_city;// MAN CITY, MCI
            case "Manchester United FC" : return R.drawable.manchester_united; //MAN UTD, MUN
            case "Newcastle United" : return R.drawable.newcastle_united; // NEWCASTLE, NEW
            case "Queens Park Rangers FC" : return R.drawable.queens_park_rangers_hd_logo;
            case "FC Southampton" : return R.drawable.southampton_fc; // SOUTHAMPTON , SOU
            case "Stoke City FC" : return R.drawable.stoke_city; // STOKE , STK
            case "Sunderland AFC" : return R.drawable.sunderland; // SUNDERLAND, SUN
            case "Swansea City" : return R.drawable.swansea_city_afc; // SWANSEA , SWA
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur; // SPURS , TOT
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo; // WEST BROM, WBA
            case "West Ham United FC" : return R.drawable.west_ham; // WEST HAM, WHU
            case "Norwich City Football Club" : return R.drawable.norwich_city; // NORWICH, NOR
            case "Watford Football Club" : return R.drawable.watford; // WATFORD, WAT
            // http://www.premierleague.com/en-gb/clubs/profile.overview.html/norwich
            default: return R.drawable.team_pic_default;
        }
    }

    // not used (but good for reusing next time
    // time method
    // get match time
    // convert from Millisecond time (Unix) to Text format date & time (with Timezone)
    void fromMsToString(long match_time_ms) {


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(match_time_ms);

        String retDate = formatter.format(calendar.getTime());

        int myIndex = retDate.indexOf(":");

        String matchTime = retDate.substring(myIndex + 1);
        String matchDate = retDate.substring(0, myIndex);

    }

    String convertToUnixMillisecond(String sDate, String sTime) {

        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.ENGLISH); // prohibit other language number problem
        simpleDate.setTimeZone(TimeZone.getDefault()); // current timezone
        try {
            Date parsedDate = simpleDate.parse(sDate+sTime); // convert to unix time (UTC)
            return String.valueOf(parsedDate.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // convert from yyyy-MM-dd &  HH:mm type time --> unix style millisecond(UTC) time
    public static long convertStringToMs(String mDate, String mTime) {

        long match_date_ms;

        SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.ENGLISH);
        match_date.setTimeZone(TimeZone.getTimeZone("UTC")); // data is UTC (GMT+0)
        try {

            Date parseddate = match_date.parse(mDate + mTime);
            match_date_ms = parseddate.getTime(); // convert time to unix time (millisecond)

            return match_date_ms;

        } catch (Exception e) {
            //Log.d(LOG_TAG, "error here!");
            return -1;
        }
    }

    public static String getDayNameFromMillis(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.
        Time t = new Time();
        t.setToNow();

        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);


        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);

        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        }
        else if ( julianDay == currentJulianDay -1) {
            return context.getString(R.string.yesterday);

        }
        else {
            Time time = new Time();
            time.setToNow();

            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE"); // get Locale day name
            return dayFormat.format(dateInMillis);
        }
    }


    // return true if Rtl mode
    public static boolean isRtlMode(Context context) {
        // Support RTL
        Configuration config = context.getResources().getConfiguration();
        return (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }


    // get the tab pager index (regarding RTL)
    public static int getPagerIndexFromMillis(long dateInMillis , boolean isRtl)
    {

        Time t = new Time();
        t.setToNow();


        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

        int diff = julianDay-currentJulianDay;


        if (diff<-2)
            diff= -2;
        if (diff>2)
            diff=2;

        // LTR mode -> 0(D-2) : 1 (D-1) : 2 (D+0) : 3 (D+1) : 4(D+2) --> diff+2
        // RTL mode -> 0(D+2) : 1 (D+1) : 2 (D+0) : 3 (D-1) : 4(D-2) --> 2-diff

        Log.d("UTIL_TAG", "juilan "+julianDay+" current "+currentJulianDay+" gmt "+ t.gmtoff);
        return (isRtl)? 2-diff : 2+diff; // depend on rtl mode return the index
    }

    public static String getLocaleDate(Context context, String oldDate) {

        // http://stackoverflow.com/questions/9235934/get-preferred-date-format-string-of-android-system
        return DateFormat.getDateInstance().format(oldDate); //http://developer.android.com/intl/ko/reference/java/text/DateFormat.html
    }

    // Check internet connection
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


}
