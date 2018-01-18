package com.ivianuu.pocket.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ivianuu.pocket.Pocket
import com.ivianuu.pocket.filesystemstorage.FileSystemStorage
import com.ivianuu.pocket.impl.PocketBuilder
import com.ivianuu.pocket.moshiserializer.MoshiParser
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.io.File

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var pocket: Pocket<Person>
    private lateinit var personAdapter: PersonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pocket = PocketBuilder.newBuilder<Person>()
            .storage(FileSystemStorage.create(File(filesDir.path + "/persons/")))
            .parser(MoshiParser.create())
            .build()

        val personInput = findViewById<EditText>(R.id.person_input)
        val addButton = findViewById<Button>(R.id.add_button)

        addButton.setOnClickListener {
            val name = personInput.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this@MainActivity, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val person = Person(name)

            pocket.put(name, person)
                .observeOn(mainThread())
                .subscribe { personInput.setText("") }
                .addTo(compositeDisposable)
        }

        val deleteAllButton = findViewById<Button>(R.id.delete_all_button)
        deleteAllButton.setOnClickListener {
            pocket.deleteAll()
                .subscribe()
                .addTo(compositeDisposable)
        }

        val personList = findViewById<RecyclerView>(R.id.person_list)
        personList.layoutManager = LinearLayoutManager(this)

        personAdapter = PersonAdapter(object : PersonAdapter.ClickDelegate {
            override fun onDeleteClick(person: Person) {
                pocket.delete(person.name)
                    .subscribe()
                    .addTo(compositeDisposable)
            }
        })

        personList.adapter = personAdapter

        pocket.streamAll()
            .map { it.sortedBy { it.name.toLowerCase().trim() } }
            .observeOn(mainThread())
            .subscribe { personAdapter.swapPersons(it) }
            .addTo(compositeDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}

private fun Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}