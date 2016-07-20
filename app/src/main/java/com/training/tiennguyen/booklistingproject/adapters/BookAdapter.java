/*
 * Copyright (c) 2016. Self Training Systems, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by TienNguyen <tien.workinfo@gmail.com - tien.workinfo@icloud.com>, October 2015
 */

package com.training.tiennguyen.booklistingproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.training.tiennguyen.booklistingproject.R;
import com.training.tiennguyen.booklistingproject.models.Book;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * BookAdapter
 *
 * @author TienNguyen
 */
public class BookAdapter extends ArrayAdapter<Book> {
    /**
     * mResouce
     */
    private int mResouce;

    /**
     * Constructor
     *
     * @param context  Context
     * @param resource int
     * @param objects  List<Book>
     */
    public BookAdapter(Context context, int resource, List<Book> objects) {
        super(context, 0, objects);
        this.mResouce = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Caching
        final ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(mResouce, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        // Populating
        Book book = getItem(position);
        if (book != null) {
            holder.link.setText(book.getLink());
            holder.title.setText(book.getTitle());
            holder.description.setText(book.getDescription());
            Picasso.with(getContext())
                    .load(book.getImg())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.book)
                    .into(holder.imageView);
        }

        return convertView;
    }

    protected static class ViewHolder {
        @BindView(R.id.lvBook_image)
        protected ImageView imageView;
        @BindView(R.id.lvBook_link)
        protected TextView link;
        @BindView(R.id.lvBook_title)
        protected TextView title;
        @BindView(R.id.lvBook_description)
        protected TextView description;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
