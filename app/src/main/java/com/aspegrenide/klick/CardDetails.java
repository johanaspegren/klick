package com.aspegrenide.klick;

import java.io.Serializable;

public class CardDetails implements Serializable {
// intent.data = Uri.parse("spotify:album:0sNOF9WDwhWunNAHPD3Baj:play")
// https://open.spotify.com/playlist/1JAsQkkhLkHlesRxzHbmpc?si=608af91b39be4177
    // funkar
    // "spotify:track:1uBsu3PbD2909UBIfEMLvK:play"
    // test spellista, funkar
    // "spotify:playlist:1JAsQkkhLkHlesRxzHbmpc:play"

    // Storytel link
    // https://www.storytel.com/se/sv/books/shuggie-bain-558386?appRedirect=true
    // cardId 4ce99017


    public CardDetails() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public CardDetails(String cardId) {
        this.cardId = cardId;
    }

    public CardDetails(String cardId, String name, String uri, String pkg, String cls, String action, String data, String imgUrl) {
        this.cardId = cardId;
        this.name = name;
        this.uri = uri;
        this.pkg = pkg;
        this.cls = cls;
        this.action = action;
        this.data = data;
        this.imgUrl = imgUrl;
    }

    private String cardId;
    private String name;
    private String uri;
    private String pkg;
    private String cls;
    private String action;
    private String data;
    private String imgUrl;

    @Override
    public String toString() {
        return "CardDetails{" +
                "cardId='" + cardId + '\'' +
                ", name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", pkg='" + pkg + '\'' +
                ", cls='" + cls + '\'' +
                ", action='" + action + '\'' +
                ", data='" + data + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
