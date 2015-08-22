/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 3 SuperDuo: Football Scores
*   ================================================
*
*        from : 1th JUL 2015
*        to : 22 AUG 2015
*
*     Kwanghyun JUNG
*     ihangulo@gmail.com
*
*    Android Devlelopment Nanodegree
*    Udacity
*
*
*/

- Support 5 day (D-2, D-1, D, D+1, D+2) Football scores
- support RTL mode (it shows tab D+2, D+1, D, D-1, D-2) 
- Support 2 type widget (Today & List)
- Widget shows -36 Hours to +36 Hours scores
- It support some laguages only for day name (Korean, Chinese, Japanese and English)
- Support content description except images which is not need to descript
- if you click widget, then its score is shown with app launching
- all football data sync once only for 1 hour when app is launched. (if you refresh button, then immediately updated) 
- only support few team's crest

[for Revierws]
- put API Key into FootballFetchService.class / 

public class FootballFetchService extends IntentService {
    public static final String LOG_TAG = "FootballFetchService";
    private static final String MY_KEY = "     "; //  <--- your key here
	
	