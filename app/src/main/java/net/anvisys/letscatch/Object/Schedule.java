package net.anvisys.letscatch.Object;

/**
 * Created by Amit Bansal on 09-01-2017.
 */
public class Schedule {

    public Schedule() {
    }

    public Schedule(String MEETING_NAME, String YOUR_MOBILE_NO, String YOUR_NAME, int MEETING_STATUS, int SCHEDULE_TYPE, String DATE, String TIME) {

        this.MEETING_NAME = MEETING_NAME;
        this.YOUR_MOBILE_NO = YOUR_MOBILE_NO;
        this.YOUR_NAME = YOUR_NAME;
        this.TYPE = SCHEDULE_TYPE;
        this.MEETING_STATUS = MEETING_STATUS;
        SCHEDULE_DATE = DATE;
        SCHEDULE_TIME = TIME;
    }

    public Schedule(String sessionString) {
        String[] arr = sessionString.split("&");
        this.MEETING_NAME = arr[0];
        this.MEETING_STATUS = Integer.parseInt(arr[3]) ;
        this.TYPE = Integer.parseInt(arr[4]);
        SCHEDULE_DATE = arr[5];
        SCHEDULE_TIME = arr[6];
    }


    public String GET_MEETING_NAME()
    {
        return this.MEETING_NAME;
    }

    public String GET_YOUR_MOBILE_NO()
    {
        return this.YOUR_MOBILE_NO;
    }
    public String GET_YOUR_NAME()
    {
        return this.YOUR_NAME;
    }

    public String GET_SCHEDULE_DATE()
    {
        return this.SCHEDULE_DATE;
    }
    public String GET_SCHEDULE_TIME()
    {
        return this.SCHEDULE_TIME;
    }

    public int GET_SCHEDULE_TYPE()
    {
        return this.TYPE;
    }

    public int ID;
    protected String MEETING_NAME;
    public String CONT_TYPE;

    public String YOUR_MOBILE_NO;
    public String YOUR_NAME;
    public String YOUR_LATLONG;
    public String YOUR_IMAGE;
    public byte[] YOUR_IMG_BYTE;

    public String My_Name = "I am";
    public String MY_LATLONG;
    public String MY_IMAGE;


    public String MY_DIST_To_GO = "";
    public String MY_TIME_TO_GO = "";

    private String SCHEDULE_DATE;
    private String SCHEDULE_TIME;


    private int TYPE;

    public String TARGET_NAME;
    public String TARGET_ADDRESS;
    public String TARGET_LATLONG;
    public int MEETING_STATUS;

    int Dist= 0;
    int Duration=0;

}
