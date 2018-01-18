/*
 * Copyright 2017 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.pocket.sample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import java.util.*

/**
 * Person adapter
 */
class PersonAdapter(val clickDelegate: ClickDelegate) :
    RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {
    private val persons = ArrayList<Person>()

    fun swapPersons(persons: List<Person>) {
        this.persons.clear()
        this.persons.addAll(persons)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = persons[position]
        holder.bind(person)
    }

    override fun getItemCount(): Int = persons.size

    interface ClickDelegate {
        fun onDeleteClick(person: Person)
    }

    inner class PersonViewHolder (itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val personName = itemView.findViewById<TextView>(R.id.person_name)
        private val deleteButton = itemView.findViewById<Button>(R.id.delete_button).apply {
            setOnClickListener(this@PersonViewHolder)
        }

        override fun onClick(view: View) {
            val person = persons[adapterPosition]
            clickDelegate.onDeleteClick(person)
        }

        fun bind(person: Person) {
            personName.text = person.name
        }
    }
}
