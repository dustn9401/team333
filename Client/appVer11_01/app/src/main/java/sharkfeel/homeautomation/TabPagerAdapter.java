package sharkfeel.homeautomation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;


public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                MainFrag fragMain = new MainFrag();
                return fragMain;
            case 1:
                FirstFloorFrag fragBedRoom = new FirstFloorFrag();
                return fragBedRoom;
            case 2:
                SecondFloorFrag fragLivingRoom = new SecondFloorFrag();
                return fragLivingRoom;
            case 3:
                YardFrag fragKitchen = new YardFrag();
                return fragKitchen;
            default:
                return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}