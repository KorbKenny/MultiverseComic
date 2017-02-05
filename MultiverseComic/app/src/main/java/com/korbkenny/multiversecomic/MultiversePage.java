package com.korbkenny.multiversecomic;

/**
 * Created by Scott on 2/4/17.
 */

public class MultiversePage extends GlobalPageObject {
    private MultiversePage mPageRight;
    private MultiversePage mPageLeft;

    public MultiversePage() {}

    public void setPageRight(MultiversePage pageRight) {
        mPageRight = pageRight;
    }

    public void setPageLeft(MultiversePage pageLeft) {
        mPageLeft = pageLeft;
    }

    public MultiversePage getPageRight() {
        return mPageRight;
    }

    public MultiversePage getPageLeft() {
        return mPageLeft;
    }
}
