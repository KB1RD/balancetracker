package net.kb1rd.balancetracker;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.CalendarView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Date;
import net.kb1rd.balancetracker.databinding.ActivityEditBinding;

public class EditActivity extends AppCompatActivity {
	
    private ActivityEditBinding binding;
    private BalanceDataStore store;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
		setSupportActionBar(binding.toolbar);

        InputFilter[] filt = new InputFilter[] {new MoneyValueFilter()};
        binding.content.tgtbal.setFilters(filt);
        binding.content.startbal.setFilters(filt);
        
        // Patch https://stackoverflow.com/a/46145179
        CalendarView.OnDateChangeListener l =
                new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(
                            CalendarView view, int year, int month, int day) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, month, day);
                        view.setDate(c.getTimeInMillis());
                    }
                };
        binding.content.startdate.setOnDateChangeListener(l);
        binding.content.enddate.setOnDateChangeListener(l);
    }
        
    public void loadStore() {
        store = new BalanceDataStore();
        store.loadFrom(this.getSharedPreferences("bal_main", Context.MODE_PRIVATE));
    }
    public void saveStore() {
        store.saveTo(this.getSharedPreferences("bal_main", Context.MODE_PRIVATE));
        WidgetMainProvider.triggerUpdate(this, this.getApplication());
    }
        
    @Override
    protected void onResume() {
        super.onResume();
        this.loadStore();
        binding.content.tgtbal.setText(String.valueOf(store.endBal()));
        binding.content.startbal.setText(String.valueOf(store.startBal()));
        binding.content.spent.setText(String.valueOf(store.spentBal()));
        binding.content.startdate.setDate(store.startDate().getTime());
        binding.content.enddate.setDate(store.endDate().getTime());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        store.setStartBal(Float.parseFloat(binding.content.startbal.getText().toString()));
        store.setEndBal(Float.parseFloat(binding.content.tgtbal.getText().toString()));
        store.setSpentBal(Float.parseFloat(binding.content.spent.getText().toString()));
        store.setStartDate(new Date(binding.content.startdate.getDate()));
        store.setEndDate(new Date(binding.content.enddate.getDate()));
        this.saveStore();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}