package net.kb1rd.balancetracker;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.kb1rd.balancetracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
	
    private ActivityMainBinding binding;
    private BalanceDataStore store;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
		setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(v -> this.showChargePopup());
        binding.content.editbtn.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, EditActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
                
        // This isnt called automatically on a new creation
        this.onNewIntent(getIntent());
    }

    public void showChargePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Charge");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("$12.34");
        InputFilter[] filt = new InputFilter[] {new MoneyValueFilter()};
        input.setFilters(filt);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(
                "Charge",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        charge(Float.parseFloat(input.getText().toString()));
                    }
                });
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }
    
    public void loadStore() {
        store = new BalanceDataStore();
        store.loadFrom(this.getSharedPreferences("bal_main", Context.MODE_PRIVATE));
    }
    public void saveStore() {
        store.saveTo(this.getSharedPreferences("bal_main", Context.MODE_PRIVATE));
        WidgetMainProvider.triggerUpdate(this, this.getApplication());
    }
    
    public void charge(float by) {
        this.loadStore();
        store.charge(by);
        this.saveStore();
        this.refreshUi();
    }
    
    public void refreshUi() {
        StringBuilder sb;
        
        binding.content.availbal.setText(String.format("$%.2f", store.currentAvailBalance()));
        
        sb = new StringBuilder("Start on ");
        sb.append(store.startDateStr());
        sb.append(" with $");
        sb.append(store.startbal);
        sb.append("\nEnd on ");
        sb.append(store.endDateStr());
        sb.append(" with $");
        sb.append(store.endbal);
        binding.content.descstr.setText(sb.toString());
        
        int[] prog = store.barPercents();
        binding.content.progress.setProgress(prog[0]);
        binding.content.progress.setSecondaryProgress(prog[1]);
        
        @ColorInt int pcol = store.isBehind() ? R.color.colorPrimaryM : R.color.colorPrimaryP;

        binding.content.progress.setProgressTintList(
            AppCompatResources.getColorStateList(this, pcol)
        );
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        this.loadStore();
        this.refreshUi();
    }
    
    @Override
    protected void onNewIntent(Intent src_intent) {
        // Set if the widget was clicked
        if (src_intent.getBooleanExtra(WidgetMainProvider.EXTRA_WIDGET, false)) {
            this.showChargePopup();
            WidgetMainProvider.triggerUpdate(this, this.getApplication());
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        WidgetMainProvider.triggerUpdate(this, this.getApplication());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}