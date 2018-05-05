package com.artk.gallery;

public class Picture {

    private final int id;
    private final String url;
    private final String earthDate;
    private final String rover;
    private final String camera;
//    private Bitmap bmp;

    Picture(int id, String url, String earthDate, String rover, String camera) {
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

//    public Bitmap getBmp() {
//        return bmp;
//    }
//
//    void setBmp(Bitmap bmp) {
//        this.bmp = bmp;
//    }

    @Override public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(this.getId()).append("\n");
        sb.append("date taken: ").append(this.getEarthDate()).append("\n");
        sb.append("rover: ").append(this.getRover()).append("\n");
        sb.append("camera: ").append(this.getCamera());

        return sb.toString();

    }

}
