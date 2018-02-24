package com.afollestad.sectionedrecyclerview;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ItemProvider {

    int getSectionCount();

    int getItemCount(int sectionIndex);

    boolean showHeaderWhenEmptyItems();

    boolean showFooters();
}
