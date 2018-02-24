package com.afollestad.sectionedrecyclerview;

import android.support.annotation.RestrictTo;
import android.support.v4.util.ArrayMap;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PositionManager implements SectionedViewHolder.PositionDelegate {

    //key(列表中的位置) value(第几个section)
    private final ArrayMap<Integer, Integer> headerLocationMap = new ArrayMap<>(0);
    //key(列表中的位置) value(第几个section)
    private final ArrayMap<Integer, Integer> footerLocationMap = new ArrayMap<>(0);
    //key(第几个section) value(是否collapse)
    private final ArrayMap<Integer, Boolean> collapsedSectionMap = new ArrayMap<>(0);
    private ItemProvider itemProvider;
    private boolean hasInvalidated;

    public PositionManager(ItemProvider itemProvider) {
        this.itemProvider = itemProvider;
        for (int s = 0; s < itemProvider.getSectionCount(); s++) {
            if (itemProvider.collapseOnStart(s)) {
                if (itemProvider.getItemCount(s) == 0 && !itemProvider.showHeadersWhenEmptyItems()) {
                    //DO NOTHING
                } else {
                    collapsedSectionMap.put(s, true);
                }
            }
        }
    }

    boolean hasInvalidated() {
        return hasInvalidated;
    }

    int invalidate() {
        this.hasInvalidated = true;
        int count = 0;
        headerLocationMap.clear();
        footerLocationMap.clear();
        for (int s = 0; s < itemProvider.getSectionCount(); s++) {
            int itemCount = itemProvider.getItemCount(s);
            //初始化的时候不会走此逻辑,在有section执行collapse后刷新整个列表时会触发此逻辑
            // (不会累加计算该section下的item个数)
            if (collapsedSectionMap.get(s) != null) {
                //当前section位置被collapse操作过
                headerLocationMap.put(count, s);
                count += 1;//只累加header的个数
                //当section所属的Item不为空时
                if (itemCount > 0) {
                    int rowCount = itemProvider.getStickyRowCount(s) * itemProvider.getFullSpanSize() / itemProvider.getRowSpan(s);
                    count += Math.max(0, Math.min(rowCount, itemCount));
                }
                continue;
            }

            //初始化的时候执行此逻辑，统计每个section下所有item的个数
            if (itemProvider.showHeadersWhenEmptyItems() || itemCount > 0) {
                headerLocationMap.put(count, s);
                count += itemCount + 1;//加1的原因是起始位置从0开始算，表示整个列表item个数
                if (itemProvider.showFooters()) {//计算footer的个数
                    footerLocationMap.put(count, s);
                    count += 1;
                }
            }
        }
        return count;
    }

    @Override
    public boolean isHeader(int absolutePosition) {
        return headerLocationMap.get(absolutePosition) != null;
    }

    @Override
    public boolean isFooter(int absolutePosition) {
        return footerLocationMap.get(absolutePosition) != null;
    }

    int sectionIndex(int absolutePosition) {
        Integer result = headerLocationMap.get(absolutePosition);
        if (result == null) {
            return -1;
        }
        return result;
    }

    int footerIndex(int absolutePosition) {
        Integer result = footerLocationMap.get(absolutePosition);
        if (result == null) {
            return -1;
        }
        return result;
    }

    int sectionHeaderIndex(int section) {
        for (Integer key : headerLocationMap.keySet()) {
            if (headerLocationMap.get(key) == section) {
                return key;
            }
        }
        return -1;
    }

    int sectionFooterIndex(int section) {
        for (Integer key : footerLocationMap.keySet()) {
            if (footerLocationMap.get(key) == section) {
                return key;
            }
        }
        return -1;
    }

    /**
     * Converts an absolute position to a relative position and section.
     */
    @Override
    public ItemCoord relativePosition(int absolutePosition) {
        //获取此位置是否存在Header布局
        Integer absHeaderLoc = headerLocationMap.get(absolutePosition);
        if (absHeaderLoc != null) {//如果此位置是Header
            return new ItemCoord(absHeaderLoc, -1);
        }
        Integer lastSectionIndex = -1;
        for (Integer sectionIndex : headerLocationMap.keySet()) {
            if (absolutePosition > sectionIndex) {
                lastSectionIndex = sectionIndex;
            } else {
                break;
            }
        }
        return new ItemCoord(
                headerLocationMap.get(lastSectionIndex), absolutePosition - lastSectionIndex - 1);
    }

    /**
     * Converts a relative position (index inside of a section) to an absolute position (index out of
     * all items and headers).
     */
    int absolutePosition(int sectionIndex, int relativeIndex) {
        if (sectionIndex < 0 || sectionIndex > itemProvider.getSectionCount() - 1) {
            return -1;
        }
        int sectionHeaderIndex = sectionHeaderIndex(sectionIndex);
        if (relativeIndex > itemProvider.getItemCount(sectionIndex) - 1) {
            return -1;
        }
        return sectionHeaderIndex + (relativeIndex + 1);
    }

    /**
     * Converts a relative position (index inside of a section) to an absolute position (index out of
     * all items and headers).
     */
    int absolutePosition(ItemCoord relativePosition) {
        return absolutePosition(relativePosition.section(), relativePosition.relativePos());
    }

    void expandSection(int section) {
        if (section < 0 || section > itemProvider.getSectionCount() - 1) {
            throw new IllegalArgumentException("Section " + section + " is out of bounds.");
        }
        collapsedSectionMap.remove(section);
    }

    void collapseSection(int section) {
        if (section < 0 || section > itemProvider.getSectionCount() - 1) {
            throw new IllegalArgumentException("Section " + section + " is out of bounds.");
        }
        collapsedSectionMap.put(section, true);
    }

    void toggleSectionExpanded(int section) {
        if (collapsedSectionMap.get(section) != null) {
            expandSection(section);
        } else {
            collapseSection(section);
        }
    }

    void expandAllSections() {
        for (int i = 0; i < itemProvider.getSectionCount(); i++) {
            expandSection(i);
        }
    }

    void collapseAllSections() {
        for (int i = 0; i < itemProvider.getSectionCount(); i++) {
            collapseSection(i);
        }
    }

    boolean isSectionExpanded(int section) {
        if (section < 0 || section > itemProvider.getSectionCount() - 1) {
            throw new IllegalArgumentException("Section " + section + " is out of bounds.");
        }
        return collapsedSectionMap.get(section) == null;
    }
}
