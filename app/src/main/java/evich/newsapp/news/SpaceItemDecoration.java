package evich.newsapp.news;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by W8-64 on 21/06/2016.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final boolean isGridLayoutManager;
    private final int mVerticalSpaceHeight;

    public SpaceItemDecoration(int verticalSpaceHeight, boolean isGridLayoutManager) {
        this.mVerticalSpaceHeight = verticalSpaceHeight;
        this.isGridLayoutManager = isGridLayoutManager;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State
            state) {
        if(isGridLayoutManager) {
            if ((parent.getChildAdapterPosition(view) % 2) == 0) {
                outRect.right = mVerticalSpaceHeight / 2;
            } else {
                outRect.left = mVerticalSpaceHeight / 2;
            }
        }
        outRect.bottom = mVerticalSpaceHeight;
    }
}
