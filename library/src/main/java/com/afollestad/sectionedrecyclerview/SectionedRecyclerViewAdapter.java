package com.afollestad.sectionedrecyclerview;

import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.ViewGroup;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class SectionedRecyclerViewAdapter<VH extends SectionedViewHolder>
        extends RecyclerView.Adapter<VH> implements ItemProvider {

    protected static final int VIEW_TYPE_FOOTER = -3;
    protected static final int VIEW_TYPE_HEADER = -2;
    protected static final int VIEW_TYPE_ITEM = -1;
    private static final String TAG = "SectionedRVAdapter";
    private PositionManager positionManager;
    private GridLayoutManager layoutManager;
    private boolean showHeadersWhenEmptyItems;
    private boolean showFooters;

    public SectionedRecyclerViewAdapter() {
        positionManager = new PositionManager(this);
    }

    public void notifySectionChanged(@IntRange(from = 0, to = Integer.MAX_VALUE) int section) {
        if (section < 0 || section > getSectionCount() - 1) {
            throw new IllegalArgumentException(
                    "Section " + section + " is out of range of existing sections.");
        }
        Integer sectionHeaderIndex = positionManager.sectionHeaderIndex(section);
        if (sectionHeaderIndex == -1) {
            throw new IllegalStateException("No header position mapped for section " + section);
        }
        int sectionItemCount = getItemCount(section);
        if (sectionItemCount == 0) {
            Log.d(TAG, "There are no items in section " + section + " to notify.");
            return;
        }
        Log.d(
                TAG, "Invalidating " + sectionItemCount + " items starting at index " + sectionHeaderIndex);
        notifyItemRangeChanged(sectionHeaderIndex, sectionItemCount);
    }

    public void expandSection(int section) {
        positionManager.expandSection(section);
        notifyDataSetChanged();
    }

    public void collapseSection(int section) {
        positionManager.collapseSection(section);
        notifyDataSetChanged();
    }

    public void expandAllSections() {
        if (!positionManager.hasInvalidated()) {
            positionManager.invalidate();
        }
        positionManager.expandAllSections();
        notifyDataSetChanged();
    }

    public void collapseAllSections() {
        if (!positionManager.hasInvalidated()) {
            positionManager.invalidate();
        }
        positionManager.collapseAllSections();
        notifyDataSetChanged();
    }

    public void toggleSectionExpanded(int section) {
        positionManager.toggleSectionExpanded(section);
        notifyDataSetChanged();
    }

    public final boolean isHeader(int position) {
        return positionManager.isHeader(position);
    }

    public final boolean isFooter(int position) {
        return positionManager.isFooter(position);
    }

    public final boolean isSectionExpanded(int section) {
        return positionManager.isSectionExpanded(section);
    }

    public final int getSectionHeaderIndex(int section) {
        return positionManager.sectionHeaderIndex(section);
    }

    public final int getSectionFooterIndex(int section) {
        return positionManager.sectionFooterIndex(section);
    }

    /**
     * Toggle whether or not section headers are shown when a section has no items. Makes a call to
     * notifyDataSetChanged().
     */
    public final void showHeadersWhenEmptyItems(boolean showHeadersWhenEmptyItems) {
        this.showHeadersWhenEmptyItems = showHeadersWhenEmptyItems;
        notifyDataSetChanged();
    }

    /**
     * Toggle whether or not section footers are shown at the bottom of each section. Makes a call to
     * notifyDataSetChanged().
     */
    public final void showFooters(boolean showFooters) {
        this.showFooters = showFooters;
        notifyDataSetChanged();
    }

    public final void setLayoutManager(@Nullable GridLayoutManager lm) {
        layoutManager = lm;
        if (lm == null) {
            return;
        }
        lm.setSpanSizeLookup(
                new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (isHeader(position) || isFooter(position)) {
                            return layoutManager.getSpanCount();
                        }
                        //非footer和header情况
                        ItemCoord sectionAndPos = getRelativePosition(position);
                        return getRowSpan(sectionAndPos.section());
                    }
                });
    }

    /**
     * Converts an absolute position to a relative position and section.
     */
    public ItemCoord getRelativePosition(int absolutePosition) {
        return positionManager.relativePosition(absolutePosition);
    }

    /**
     * Converts a relative position (index inside of a section) to an absolute position (index out of
     * all items and headers).
     */
    public int getAbsolutePosition(int sectionIndex, int relativeIndex) {
        return positionManager.absolutePosition(sectionIndex, relativeIndex);
    }

    /**
     * Converts a relative position (index inside of a section) to an absolute position (index out of
     * all items and headers).
     */
    public int getAbsolutePosition(ItemCoord relativePosition) {
        return positionManager.absolutePosition(relativePosition);
    }

    @Override
    public final int getItemCount() {
        return positionManager.invalidate();
    }

    @Override
    public final boolean showHeadersWhenEmptyItems() {
        return showHeadersWhenEmptyItems;
    }

    @Override
    public boolean showFooters() {
        return showFooters;
    }

    @Override
    public int getFullSpanSize() {
        return layoutManager.getSpanCount();
    }

    @Override
    public long getItemId(int position) {
        //好像没啥用...
        if (isHeader(position)) {
            int pos = positionManager.sectionIndex(position);
            return getHeaderId(pos);
        } else if (isFooter(position)) {
            int pos = positionManager.footerIndex(position);
            return getFooterId(pos);
        } else {
            ItemCoord sectionAndPos = getRelativePosition(position);
            return getItemId(sectionAndPos.section(), sectionAndPos.relativePos());
        }
    }

    public long getHeaderId(int section) {
        return super.getItemId(section);
    }

    public long getFooterId(int section) {
        return super.getItemId(section) + getItemCount(section);
    }

    public long getItemId(int section, int position) {
        return super.getItemId(position);
    }

    @Override
    public final int getItemViewType(int position) {
        if (isHeader(position)) {
            return getHeaderViewType(positionManager.sectionIndex(position));
        } else if (isFooter(position)) {
            return getFooterViewType(positionManager.footerIndex(position));
        } else {
            ItemCoord sectionAndPos = getRelativePosition(position);
            return getItemViewType(
                    sectionAndPos.section(),
                    // offset section view positions
                    sectionAndPos.relativePos(),
                    position - (sectionAndPos.section() + 1));
        }
    }

    public int getHeaderViewType(int section) {
        return VIEW_TYPE_HEADER;
    }

    public int getFooterViewType(int section) {
        return VIEW_TYPE_FOOTER;
    }

    public int getItemViewType(int section, int relativePosition, int absolutePosition) {
        return VIEW_TYPE_ITEM;
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        holder.setPositionDelegate(positionManager);

        StaggeredGridLayoutManager.LayoutParams layoutParams = null;
        if (holder.itemView.getLayoutParams() instanceof GridLayoutManager.LayoutParams)
            layoutParams = new StaggeredGridLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        else if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        }

        if (isHeader(position)) {
            if (layoutParams != null) {
                layoutParams.setFullSpan(true);
            }
            int sectionIndex = positionManager.sectionIndex(position);
            onBindHeaderViewHolder(holder, sectionIndex, isSectionExpanded(sectionIndex));
        } else if (isFooter(position)) {
            if (layoutParams != null) {
                layoutParams.setFullSpan(true);
            }
            int sectionIndex = positionManager.footerIndex(position);
            onBindFooterViewHolder(holder, sectionIndex);
        } else {
            if (layoutParams != null) {
                layoutParams.setFullSpan(false);
            }
            ItemCoord sectionAndPos = getRelativePosition(position);
            int absolutePosition = getAbsolutePosition(sectionAndPos);
            onBindViewHolder(
                    holder,
                    sectionAndPos.section(),
                    // offset section view positions
                    sectionAndPos.relativePos(),
                    absolutePosition);
        }

        if (layoutParams != null) {
            holder.itemView.setLayoutParams(layoutParams);
        }
    }

    public abstract int getSectionCount();

    public abstract int getItemCount(int sectionIndex);

    public abstract int getStickyRowCount(int sectionIndex);

    public abstract int getRowSpan(int sectionIndex);

    public abstract boolean collapseOnStart(int sectionIndex);

    public abstract void onBindHeaderViewHolder(VH holder, int section, boolean expanded);

    public abstract void onBindFooterViewHolder(VH holder, int section);

    public abstract void onBindViewHolder(
            VH holder, int section, int relativePosition, int absolutePosition);
}
