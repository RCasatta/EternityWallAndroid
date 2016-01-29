package it.eternitywall.eternitywall.components;


import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * CoordinatorLayout Behavior for a quick return footer
 *
 * When a nested ScrollView is scrolled down, the quick return view will disappear.
 * When the ScrollView is scrolled back up, the quick return view will reappear.
 *
 * @author bherbst
 */
@SuppressWarnings("unused")
public class ProportionalReturnHeaderBehavior extends CoordinatorLayout.Behavior<View> {
    private static final String TAG = ProportionalReturnHeaderBehavior.class.toString();

    private int mDySinceDirectionChange;
    private boolean mIsShowing;
    private boolean mIsHiding;

    public ProportionalReturnHeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        Log.i(TAG,"dx=" + dx + " dy=" + dy);
        Log.i(TAG, "child.getTranslationY()=" + child.getTranslationY());
        final int height = child.getHeight();
        Log.i(TAG,"child.getHeight()=" + height);

        final float childTranslationY = child.getTranslationY() - dy;
        if(childTranslationY>0)
            return;
        if(childTranslationY<-height)
            return;
        child.setTranslationY(childTranslationY);

        final float targetTranslationY = target.getTranslationY() - dy;

        target.setTranslationY(targetTranslationY);


    }


}
