package com.korbkenny.multiversecomic;

/**
 * Created by KorbBookProReturns on 1/14/17.
 */

public class PageObject {
    private String mText, mImage, mUser, mFrom, mFromUser, mLeft, mLeftUser, mNextLeft,
    mRight, mRightUser, mNextRight, mBeingWorkedOn;

    public PageObject() {}

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public String getFrom() {
        return mFrom;
    }

    public void setFrom(String from) {
        mFrom = from;
    }

    public String getFromUser() {
        return mFromUser;
    }

    public void setFromUser(String fromUser) {
        mFromUser = fromUser;
    }

    public String getLeft() {
        return mLeft;
    }

    public void setLeft(String left) {
        mLeft = left;
    }

    public String getLeftUser() {
        return mLeftUser;
    }

    public void setLeftUser(String leftUser) {
        mLeftUser = leftUser;
    }

    public String getNextLeft() {
        return mNextLeft;
    }

    public void setNextLeft(String nextLeft) {
        mNextLeft = nextLeft;
    }

    public String getRight() {
        return mRight;
    }

    public void setRight(String right) {
        mRight = right;
    }

    public String getRightUser() {
        return mRightUser;
    }

    public void setRightUser(String rightUser) {
        mRightUser = rightUser;
    }

    public String getNextRight() {
        return mNextRight;
    }

    public void setNextRight(String nextRight) {
        mNextRight = nextRight;
    }

    public String getBeingWorkedOn() {
        return mBeingWorkedOn;
    }

    public void setBeingWorkedOn(String beingWorkedOn) {
        mBeingWorkedOn = beingWorkedOn;
    }
}
