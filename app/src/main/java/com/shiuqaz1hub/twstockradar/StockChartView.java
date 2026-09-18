package com.shiuqaz1hub.twstockradar;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockChartView extends View {
    private final List<Float> values = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public StockChartView(android.content.Context context) {
        super(context);
        init();
    }

    public StockChartView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setColor(Color.rgb(37, 99, 235));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        gridPaint.setColor(Color.rgb(226, 232, 240));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
    }

    public void setValues(List<Float> points) {
        values.clear();
        values.addAll(points);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int left = 18;
        int top = 20;
        int right = width - 18;
        int bottom = height - 20;

        for (int i = 0; i <= 4; i++) {
            int y = top + (bottom - top) * i / 4;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        if (values.isEmpty()) {
            return;
        }

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (float v : values) {
            if (v < min) min = v;
            if (v > max) max = v;
        }

        if (max == min) {
            max += 1f;
            min -= 1f;
        }

        Path path = new Path();
        for (int i = 0; i < values.size(); i++) {
            float x = left + (right - left) * i / (values.size() - 1f);
            float normalized = (values.get(i) - min) / (max - min);
            float y = bottom - normalized * (bottom - top);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, linePaint);

        Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.rgb(220, 38, 38));
        pointPaint.setStyle(Paint.Style.FILL);
        int lastIndex = values.size() - 1;
        float lastX = left + (right - left) * lastIndex / (values.size() - 1f);
        float lastValue = values.get(lastIndex);
        float lastNorm = (lastValue - min) / (max - min);
        float lastY = bottom - lastNorm * (bottom - top);
        canvas.drawCircle(lastX, lastY, 5f, pointPaint);
    }

    public void setData(float... series) {
        List<Float> points = new ArrayList<>();
        for (float v : series) points.add(v);
        setValues(points);
    }
}
