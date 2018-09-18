package sharkfeel.homeautomation;

public class RegionDBListItem {

    //    private Drawable icon;    만약 그림추가할시.. 해당부분 활성화시키면됨
    private String step1;
    private String step2;
    private String step3;
    private String coordinate_x;
    private String coordinate_y;


//    public Drawable getIcon() {
//        return icon;
//    }

//    public void setIcon(Drawable icon) {
//        this.icon = icon;
//    }

    public String getStep1() {
        return step1;
    }

    public void setStep1(String step1) {
        this.step1 = step1;
    }

    public String getStep2() {
        return step2;
    }

    public void setStep2(String step2) {
        this.step2 = step2;
    }

    public String getStep3() {
        return step3;
    }

    public void setStep3(String step3) {
        this.step3 = step3;
    }

    public String getCoordinate_x() {
        return coordinate_x;
    }

    public void setCoordinate_x(String coordinate_x) {
        this.coordinate_x = coordinate_x;
    }

    public String getCoordinate_y() {
        return coordinate_y;
    }

    public void setCoordinate_y(String coordinate_y) {
        this.coordinate_y = coordinate_y;
    }
}