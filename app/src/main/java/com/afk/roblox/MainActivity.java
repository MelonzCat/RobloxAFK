package com.afk.roblox;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText jx, jy, mn, mx;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        final SharedPreferences p = getSharedPreferences("afk", MODE_PRIVATE);

        ScrollView sv = new ScrollView(this);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        l.setPadding(pad, pad, pad, pad);
        sv.addView(l);

        TextView t = new TextView(this);
        t.setText("1. Tap the button below and turn ON \"Roblox AFK\" in Accessibility.\n"
                + "2. A floating AFK button appears. Open Roblox (landscape) and tap it to start/stop. Drag it to move it.\n\n"
                + "Jump button position (% of screen) and time between actions (seconds):");
        l.addView(t);

        Button acc = new Button(this);
        acc.setText("Open Accessibility Settings");
        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        l.addView(acc);

        jx = field(l, "Jump X %", p.getInt("jx", 88));
        jy = field(l, "Jump Y %", p.getInt("jy", 78));
        mn = field(l, "Min seconds", p.getInt("min", 60));
        mx = field(l, "Max seconds", p.getInt("max", 120));

        Button save = new Button(this);
        save.setText("Save");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a = Math.min(99, Math.max(1, num(jx, 88)));
                int c = Math.min(99, Math.max(1, num(jy, 78)));
                int lo = Math.max(10, num(mn, 60));
                int hi = Math.max(lo, num(mx, 120));
                p.edit().putInt("jx", a).putInt("jy", c).putInt("min", lo).putInt("max", hi).apply();
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
        l.addView(save);

        setContentView(sv);
    }

    private EditText field(LinearLayout parent, String label, int val) {
        TextView tv = new TextView(this);
        tv.setText(label);
        EditText e = new EditText(this);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        e.setText(String.valueOf(val));
        parent.addView(tv);
        parent.addView(e);
        return e;
    }

    private int num(EditText e, int def) {
        try {
            return Integer.parseInt(e.getText().toString().trim());
        } catch (Exception x) {
            return def;
        }
    }
}
