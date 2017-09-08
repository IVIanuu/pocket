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

package com.ivianuu.pocket.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Person adapter
 */
final class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {

    private final ClickDelegate clickDelegate;
    private final List<Person> persons = new ArrayList<>();

    PersonAdapter(@NonNull ClickDelegate clickDelegate) {
        this.clickDelegate = clickDelegate;
        this.persons.addAll(persons);
    }

    void swapPersons(@NonNull List<Person> persons) {
        this.persons.clear();
        this.persons.addAll(persons);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PersonViewHolder holder, int position) {
        Person person = persons.get(position);
        holder.bind(person);
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    interface ClickDelegate {
        void onDeleteClick(Person person);
    }

    class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView personName;
        private final Button deleteButton;

        private PersonViewHolder(View itemView) {
            super(itemView);
            this.personName = itemView.findViewById(R.id.person_name);
            this.deleteButton = itemView.findViewById(R.id.delete_button);
            // clicks
            deleteButton.setOnClickListener(this);
        }

        private void bind(Person person) {
            personName.setText(person.getName());
        }

        @Override
        public void onClick(View view) {
            if (view == deleteButton) {
                Person person = persons.get(getAdapterPosition());
                clickDelegate.onDeleteClick(person);
            }
        }
    }
}
