package com.artk.gallery.data;

public class Picture {

    private final int id;
    private final String url;
    private final String earthDate;
    private final String rover;
    private final String camera;
    private boolean favorite = false; // this field is not necessary anymore

    public Picture(int id, String url, String earthDate, String rover, String camera) {
        this.id = id;
        this.url = url;
        this.earthDate = earthDate;
        this.rover = rover;
        this.camera = camera;
    }

    public int getId() {
        return id;
    }

    public String getEarthDate() {
        return earthDate;
    }

    public String getCamera() {
        return camera;
    }

    public String getUrl() {
        return url;
    }

    public String getRover() {
        return rover;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Picture picture = (Picture) o;
        return id == picture.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static Picture copyOf(Picture picture){
        int id = picture.getId();
        String url = picture.getUrl();
        String earthDate = picture.getEarthDate();
        String rover = picture.getRover();
        String camera = picture.getCamera();
        Picture copy = new Picture(id, url, earthDate, rover, camera);
        copy.setFavorite(picture.isFavorite());
        return copy;
    }

}
