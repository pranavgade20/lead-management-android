package com.community.jboss.leadmanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.community.jboss.leadmanagement.data.daos.ContactDao;
import com.community.jboss.leadmanagement.data.entities.Contact;
import com.community.jboss.leadmanagement.utils.DbUtil;

import java.util.ArrayList;
import java.util.List;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<String> contactsArrayList = new ArrayList<>();
    private Context context;

    private Observer<List<Contact>> observer;
    private LiveData<List<Contact>> contactsDataList;

    WidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        ContactDao contactDao = DbUtil.contactDao(context);
        contactsDataList = contactDao.getContacts();

    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return contactsArrayList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews views = new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_1);
        views.setTextViewText(android.R.id.text1, contactsArrayList.get(i));
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void initData() {
        contactsArrayList.clear();

        if (observer!=null){
            try {
                contactsDataList.removeObserver(observer);
                observer = null;
            } catch (Exception e) {
                observer = null;
                Log.e("Error", "in removing observer", e);
            }
        }

        observer = contacts -> {
            if (contacts != null) {
                contactsArrayList.clear();
                for (int i = 0; i < contacts.size(); i++) {
                    contactsArrayList.add(contacts.get(i).getName());
                }
            } else {
                contactsArrayList.add("No Contacts to display");
            }
        };

        contactsDataList.observeForever(observer);
    }
}
