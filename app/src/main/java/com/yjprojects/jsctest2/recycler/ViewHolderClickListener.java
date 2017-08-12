package com.yjprojects.jsctest2.recycler;

import android.view.MenuItem;
import android.view.View;

/**
 * Created by jnj on 2016-09-04.
 */
public interface ViewHolderClickListener {
    void onViewClicked(View view, int position);

    void onPopupMenuClicked(MenuItem menuItem, int position);
}
