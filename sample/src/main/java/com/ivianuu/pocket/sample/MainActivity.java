package com.ivianuu.pocket.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.impl.FileSystemStorage;
import com.ivianuu.pocket.impl.GsonSerializer;
import com.ivianuu.pocket.impl.PocketBuilder;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Pocket stringPocket = PocketBuilder.builder()
                .encryption(new Base64Encryption())
                .serializer(GsonSerializer.create())
                .storage(FileSystemStorage.create(this))
                .build();

        stringPocket.updates()
                .subscribe(new Consumer<Pair<String, Object>>() {
                    @Override
                    public void accept(Pair<String, Object> pair) throws Exception {
                        Log.d("testtt", pair.first + " updated to " + pair.second.toString());
                    }
                });

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                count++;
                Pojooo pojooo = new Pojooo("hannes " + count, count, 3.14f * count);
                stringPocket.put("my_key" + count, pojooo)
                        .subscribe();
                handler.postDelayed(this, 100);
            }
        });
    }
}
