package net.kb1rd.balancetracker;
import android.content.SharedPreferences;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BalanceDataStore {
    final static SimpleDateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    protected float spent;
    protected float startbal;
    protected float endbal;
    protected Date startdate;
    protected Date enddate;
    
    BalanceDataStore() {
        this.spent = 50.0f;
        this.startbal = 0.0f;
        this.endbal = 123.45f;
        this.startdate = parseDate("2024-07-01");
        this.enddate = parseDate("2024-08-01");
    }
    
    void charge(float by) {
        spent += by;
    }
    
    float startBal() { return this.startbal; }
    float endBal() { return this.endbal; }
    float spentBal() { return this.spent; }
    Date startDate() { return this.startdate; }
    Date endDate() { return this.enddate; }
    
    void setStartBal(float v) { this.startbal = roundMoney(v); }
    void setEndBal(float v) { this.endbal = roundMoney(v); }
    void setSpentBal(float v) { this.spent = roundMoney(v); }
    void setStartDate(Date d) { this.startdate = d; }
    void setEndDate(Date d) { this.enddate = d; }
    
    float length() {
        return dayDelta(this.enddate, this.startdate);
    }
    float daysSinceStart() {
        return dayDelta(new Date(), this.startdate);
    }
    float percentDays() {
        return percent(daysSinceStart(), length());
    }
    
    float totalLinearBalance() {
        return this.endbal - this.startbal;
    }
    float balanceSpent() {
        return this.spent;
    }
    float percentSpent() {
        return percent(this.spent, this.endbal);
    }
    
    float dailyBalance() {
        return totalLinearBalance() / length();
    }
    float currentDayBalance() {
        //             y = m               ×x                                  + b
        return roundMoney(dailyBalance() * clamp(daysSinceStart(), length()) + this.startbal);
    }
    float percentDayBalance() {
        return this.currentDayBalance() / this.endbal;
    }
    float currentAvailBalance() {
        return this.currentDayBalance() - this.spent;
    }
    boolean isBehind() {
        return this.currentDayBalance() < this.spent;
    }
    int[] barPercents() {
        int pavail = Math.round(percentDayBalance() * 65535.0f);
        int pspent = Math.round(percentSpent() * 65535.0f);
        return new int[]{
            Math.max(pspent, pavail),
            Math.min(pspent, pavail)
        };
    }
    float daysAhead() {
        return currentAvailBalance() / dailyBalance();
    }
    
    String startDateStr() { return dfmt.format(this.startdate); }
    String endDateStr() { return dfmt.format(this.enddate); }
    
    float loadMoney(SharedPreferences prefs, String key) {
        return ((float)prefs.getInt(key, 0)) / 100.0f;
    }
    Date loadDate(SharedPreferences prefs, String key) {
        return parseDate(prefs.getString(key, ""));
    }
    void loadFrom(SharedPreferences prefs) {
        if (prefs.contains("startbal")) {
            this.startbal = loadMoney(prefs, "startbal");
        }
        if (prefs.contains("endbal")) {
            this.endbal = loadMoney(prefs, "endbal");
        }
        if (prefs.contains("spent")) {
            this.spent = loadMoney(prefs, "spent");
        }
        if (prefs.contains("startdate")) {
            this.startdate = loadDate(prefs, "startdate");
        }
        if (prefs.contains("enddate")) {
            this.enddate = loadDate(prefs, "enddate");
        }
    }
    void saveTo(SharedPreferences prefs) {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("startbal", (int)Math.round(this.startbal * 100.0));
        e.putInt("endbal", (int)Math.round(this.endbal * 100.0));
        e.putInt("spent", (int)Math.round(this.spent * 100.0));
        e.putString("startdate", this.startDateStr());
        e.putString("enddate", this.endDateStr());
        e.apply();
    }
    
    static Date parseDate(String s) {
        try {
            return dfmt.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }
    static float roundMoney(float i) {
        return Math.round(i * 100.0f) / 100.0f;
    }
    static float dayDelta(Date p, Date m) {
        double millis = (double)(p.getTime() - m.getTime());
        double minutes = millis / (60.0 * 1000.0);
        double days = minutes / (60 * 24);
        return (float)days;
    }
    static float clamp(float v, float max) {
        if (v < 0.0) {
            return 0.0f;
        } else if (v > max) {
            return max;
        }
        return v;
    }
    static float clamp(float v) {
        return clamp(v, 1.0f);
    }
    static float percent(float comp, float total) {
        if (total == 0) {
            return comp > 0 ? 1.0f : 0.0f;
        }
        return clamp(comp / total);
    }
}
