package com.insequence.simpletodo;

/**
 * Created by paulyang on 9/19/16.
 */
public class TodoItem {

    private String key;
    private String text;
    private String name;
    private String photoUrl;
    private String posterUrl;
    private String backdropUrl;
    public String rating;
    public String releaseDate;

    public TodoItem() {
    }

    public TodoItem(String text, String name, String photoUrl, String key, String posterUrl, String backdropUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.key = key;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String text) {
        this.key = text;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public void setBackdropUrl(String BackdropUrl) {
        this.backdropUrl = BackdropUrl;
    }

    // http://stackoverflow.com/questions/8180430/how-to-override-equals-method-in-java
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!TodoItem.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final TodoItem other = (TodoItem) obj;
        if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
            return false;
        }
        return true;
    }

    // should also override hashCode() method?
//    @Override
//    public int hashCode() {
//        int hash = 3;
//        hash = 53 * hash + (this.key != null ? this.key.hashCode() : 0);
//        return hash;
//    }

    // http://stackoverflow.com/questions/3546362/android-tostring
    @Override
    public String toString() {
        return new StringBuilder()
                .append(text + "\n")
                .append(name).toString();
    }
}
