package com.ivianuu.pocket.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ivianuu.pocket.Pocket;
import com.ivianuu.pocket.base64encryption.Base64Encryption;
import com.ivianuu.pocket.impl.PocketBuilder;
import com.ivianuu.pocket.lrucache.LruCache;
import com.ivianuu.pocket.moshiserializer.MoshiSerializer;
import com.ivianuu.pocket.sharedpreferencesstorage.SharedPreferencesStorage;

import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

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
                .serializer(MoshiSerializer.create())
                .storage(SharedPreferencesStorage.create(this))
                .build();

        final EditText personInput = findViewById(R.id.person_input);

        Button addButton = findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = personInput.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Person person = new Person(name);
                Disposable addDisposable = pocket.put(name, person)
                        .observeOn(mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                personInput.setText("");
                            }
                        });
                compositeDisposable.add(addDisposable);
            }
        });

        Button deleteAllButton = findViewById(R.id.delete_all_button);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Disposable deleteAllDisposable = pocket.deleteAll()
                        .subscribe();
                compositeDisposable.add(deleteAllDisposable);
            }
        });

        RecyclerView personList = findViewById(R.id.person_list);
        personList.setLayoutManager(new LinearLayoutManager(this));

        personAdapter = new PersonAdapter(new PersonAdapter.ClickDelegate() {
            @Override
            public void onDeleteClick(Person person) {
                Disposable deleteDisposable = pocket.delete(person.getName())
                        .subscribe();
                compositeDisposable.add(deleteDisposable);
            }});

        personList.setAdapter(personAdapter);

        Disposable changesDisposable = pocket.keyChanges()
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        loadData();
                    }
                });
        compositeDisposable.add(changesDisposable);

        loadData();
    }

    private Disposable loadData;
    private void loadData() {
        if (loadData != null) {
            loadData.dispose();
        }

        loadData = pocket.getAllKeys()
                .toObservable()
                .flatMapIterable(new Function<List<String>, Iterable<String>>() {
                    @Override
                    public Iterable<String> apply(List<String> strings) throws Exception {
                        return strings;
                    }
                })
                .concatMap(new Function<String, ObservableSource<Person>>() {
                    @Override
                    public ObservableSource<Person> apply(String s) throws Exception {
                        return pocket.get(s, Person.class).toObservable();
                    }
                })
                .toList()
                .observeOn(mainThread())
                .subscribe(new Consumer<List<Person>>() {
                    @Override
                    public void accept(List<Person> people) throws Exception {
                        personAdapter.swapPersons(people);
                    }
                });
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
