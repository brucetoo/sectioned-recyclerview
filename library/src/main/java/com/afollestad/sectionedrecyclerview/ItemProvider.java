package com.afollestad.sectionedrecyclerview;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ItemProvider {

    /**
     * Init section group size
     * @return section count
     */
    int getSectionCount();

    /**
     * Item count belongs to a single section
     * @param sectionIndex section index
     * @return item count
     */
    int getItemCount(int sectionIndex);

    /**
     * Section can define a sticky row item when be collapsed
     * @param sectionIndex section index
     * @return row count
     */
    int getStickyRowCount(int sectionIndex);

    /**
     * Define section row space to layout items
     * @param sectionIndex section index
     * @return row span
     */
    int getRowSpan(int sectionIndex);

    boolean collapseOnStart(int sectionIndex);

    int getFullSpanSize();

    boolean showHeadersWhenEmptyItems();

    boolean showFooters();
}
