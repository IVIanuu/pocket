package com.ivianuu.pocket.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;

import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.filesystemstorage.FileSystemStorage;
import com.ivianuu.pocket.gsonserializer.GsonSerializer;
import com.ivianuu.pocket.impl.PocketBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Pocket stringPocket = PocketBuilder.builder()
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
                List<Pojooo> pojooos = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    Pojooo pojooo = new Pojooo("hannes " + count, count, 3.14f * count);
                    pojooos.add(pojooo);
                }
                stringPocket.put("my_key" + count, pojooos)
                        .subscribe();
                handler.postDelayed(this, 100);
            }
        });
    }
}
