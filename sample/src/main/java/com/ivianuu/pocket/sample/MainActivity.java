package com.ivianuu.pocket.sample;

import android.os.Handler;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ivianuu.pocket.Pocket;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Pocket<Pojooo> stringPocket = new Pocket.Builder<>(this, Pojooo.class).build();

        stringPocket.updates()
                .subscribe(new Consumer<Pair<String, Pojooo>>() {
                    @Override
                    public void accept(Pair<String, Pojooo> pair) throws Exception {
                        Log.d("testtt", pair.first + " updated to " + pair.second.toString());
                    }
                });


        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                count++;
                Pojooo pojooo = new Pojooo("hannes " + count, count, 3.14f * count);
                stringPocket.write("my_key" + count, pojooo)
                        .subscribe();
                handler.postDelayed(this, 10);
            }
        });
    }
}
