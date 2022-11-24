package com.im.app.coinwatcher

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarketItemDecoration(
    private var context: Context,
    private var leftOffset: Int = 10,
    private var topOffset: Int = 10,
    private var rightOffset: Int = 10,
    private var bottomOffset: Int = 10
): RecyclerView.ItemDecoration() {
    // dp -> pixel 단위로 변경
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = dpToPx(context, bottomOffset)
        outRect.left = dpToPx(context, leftOffset)
        outRect.right = dpToPx(context, rightOffset)
    }
}