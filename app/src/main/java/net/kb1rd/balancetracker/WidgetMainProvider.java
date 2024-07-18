package net.kb1rd.balancetracker;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;
import com.google.android.material.color.MaterialColors;

public class WidgetMainProvider extends AppWidgetProvider {
    public static final String EXTRA_WIDGET = "net.kb1rd.balancetracker.widget_src";
    private BalanceDataStore store;
    
    public static void triggerUpdate(Context ctx, Application app) {
        // Trigger widget update
        Intent intent = new Intent(ctx, WidgetMainProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(app)
            .getAppWidgetI‌​ds(new ComponentName(app, WidgetMainProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(intent);
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_WIDGET, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        this.loadStore(context);
        this.refreshUi(context, views);
        manager.updateAppWidget(widgetIds, views);
    }
    
    public void loadStore(Context ctx) {
        store = new BalanceDataStore();
        store.loadFrom(ctx.getSharedPreferences("bal_main", Context.MODE_PRIVATE));
    }
    
    public void refreshUi(Context ctx, RemoteViews views) {
        StringBuilder sb;
        String str;
                
        @ColorInt int pcol = store.isBehind() ? R.color.colorPrimaryM : R.color.colorPrimaryP;
        ColorStateList csl = AppCompatResources.getColorStateList(ctx, pcol);
        views.setInt(R.id.widget, "setBackgroundColor", csl.getDefaultColor());
        
        float amt = store.currentAvailBalance();
        float amta = Math.abs(amt);
        int charct = (int)Math.floor(amta);
        int manti = (int)((amta - Math.floor(amta)) * 100);
        if (amt < 0) {
            charct = -charct;
        }
        views.setTextViewText(R.id.priceInt, "$" + String.valueOf(charct));
        views.setTextViewText(R.id.priceDec, String.format(".%02d", manti));
        
        int[] prog = store.barPercents();
        views.setInt(R.id.progress, "setProgress", prog[0]);
        views.setInt(R.id.progress, "setSecondaryProgress", prog[1]);
        
        str = String.format(
            "%.0f%% ($%.2f / $%.2f)",
            store.percentSpent()*100,
            store.balanceSpent(),
            store.endBal()
        );
        views.setTextViewText(R.id.compMoney, str);
        
        str = String.format(
            "%.0f%% (%.0f / %.0f days)",
            store.percentDays()*100,
            store.daysSinceStart(),
            store.length()
        );
        views.setTextViewText(R.id.compTime, str);
                                
        sb = new StringBuilder("");
        float ahead = store.daysAhead();
        if (Math.abs(ahead) < 1) {
            sb.append (">1");
        } else {
            sb.append(Math.round(Math.abs(ahead)));
        }
        sb.append(" day");
        if (Math.round(Math.abs(ahead)) > 1) {
            sb.append("s");
        }
        if (ahead < 0) {
            sb.append(" behind");
        } else {
            sb.append(" ahead");
        }
        views.setTextViewText(R.id.compStr, sb.toString());
    }
}
