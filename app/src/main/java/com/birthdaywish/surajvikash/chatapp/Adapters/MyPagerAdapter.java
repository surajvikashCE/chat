package com.birthdaywish.surajvikash.chatapp.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.birthdaywish.surajvikash.chatapp.Fragments.ChatsFragment;
import com.birthdaywish.surajvikash.chatapp.Fragments.FriendsFragment;
import com.birthdaywish.surajvikash.chatapp.Fragments.RequestFragment;

/**
 * Created by surajvikash on 23/05/18.
 */

public class MyPagerAdapter extends FragmentPagerAdapter{

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0 :
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            case 1 :
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2 :
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0 :
                return "FRIENDS";
            case 1 :
                return "CHATS";
            case 2 :
                return "REQUESTS";

            default:
                return null;
        }
    }
}
