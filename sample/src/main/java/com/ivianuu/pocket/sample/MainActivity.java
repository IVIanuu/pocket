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
import com.ivianuu.pocket.filesystemstorage.FileSystemStorage;
import com.ivianuu.pocket.gsonserializer.GsonSerializer;
import com.ivianuu.pocket.impl.PocketBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Pocket pocket = PocketBuilder.builder()
                .serializer(GsonSerializer.create())
                .storage(FileSystemStorage.create(this))
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

        final PersonAdapter personAdapter = new PersonAdapter(new PersonAdapter.ClickDelegate() {
            @Override
            public void onDeleteClick(Person person) {
                Disposable deleteDisposable = pocket.delete(person.getName())
                        .subscribe();
                compositeDisposable.add(deleteDisposable);
            }});

        personList.setAdapter(personAdapter);

        Disposable changesDisposable = pocket.keyChanges()
                .flatMapSingle(new Function<String, SingleSource<HashMap<String, Object>>>() {
                    @Override
                    public SingleSource<HashMap<String, Object>> apply(String s) throws Exception {
                        return pocket.getAllValues();
                    }
                })
                .subscribe(new Consumer<HashMap<String, Object>>() {
                    @Override
                    public void accept(HashMap<String, Object> map) throws Exception {
                        List<Person> persons = new ArrayList<>();
                        for (Object value : map.values()) {
                            if (value instanceof Person) {
                                persons.add((Person) value);
                            }
                        }

                        personAdapter.swapPersons(persons);
                    }
                });
        compositeDisposable.add(changesDisposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
