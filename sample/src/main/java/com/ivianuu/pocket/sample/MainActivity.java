package com.ivianuu.pocket.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.base64encryption.Base64Encryption;
import com.ivianuu.pocket.filesystemstorage.FileSystemStorage;
import com.ivianuu.pocket.gsonserializer.GsonSerializer;
import com.ivianuu.pocket.impl.PocketBuilder;
import com.ivianuu.pocket.lrucache.LruCache;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class MainActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Pocket pocket;
    private PersonAdapter personAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pocket = PocketBuilder.builder()
                .cache(LruCache.create(Integer.MAX_VALUE))
                .encryption(Base64Encryption.create())
                .serializer(GsonSerializer.create())
                .storage(FileSystemStorage.create(this))
                .scheduler(Schedulers.io())
                .build();

        pocket.stream("Manolo", Person.class)
                .subscribe(option -> {
                    if (option.present()) {
                        //noinspection ConstantConditions
                        Log.d("testt", "present " + option.getValue().toString());
                    } else {
                        Log.d("testt", "absent");
                    }
                });

        final EditText personInput = findViewById(R.id.person_input);

        Button addButton = findViewById(R.id.add_button);

        addButton.setOnClickListener(__ -> {
            String name = personInput.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Person person = new Person(name);
            Disposable addDisposable = pocket.put(name, person)
                    .observeOn(mainThread())
                    .subscribe(() -> personInput.setText(""));
            compositeDisposable.add(addDisposable);
        });

        Button deleteAllButton = findViewById(R.id.delete_all_button);
        deleteAllButton.setOnClickListener(__ -> {
            Disposable deleteAllDisposable = pocket.deleteAll()
                    .subscribe();
            compositeDisposable.add(deleteAllDisposable);
        });

        RecyclerView personList = findViewById(R.id.person_list);
        personList.setLayoutManager(new LinearLayoutManager(this));

        personAdapter = new PersonAdapter(person -> {
            Disposable deleteDisposable = pocket.delete(person.getName())
                    .subscribe();
            compositeDisposable.add(deleteDisposable);
        });

        personList.setAdapter(personAdapter);

        Disposable changesDisposable = pocket.keyChanges()
                .subscribe(s -> loadData());
        compositeDisposable.add(changesDisposable);

        loadData();
    }

    private Disposable loadData;
    private void loadData() {
        if (loadData != null) {
            loadData.dispose();
        }

        loadData = pocket.getAll(Person.class)
                .map(map -> {
                    List<Person> people = new ArrayList<>();
                    people.addAll(map.values());
                    return people;
                })
                .observeOn(mainThread())
                .subscribe(personAdapter::swapPersons);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (loadData != null) {
            loadData.dispose();
        }
    }
}
