package com.huangrui.conwaylifegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ConwayLifeGameView extends View {

    private static final String TAG = "ConwayLifeGameView";

    // 列数
    private static final int COLS = 150;
    // 行数
    private int rows;
    // 距离底部
    private static final int MARGIN_BOTTOM = 0;
    // 细胞总数
    private int total;

    // 线条画笔
    private Paint linePaint;
    // 线条宽度
    private static final int EDGE = 2;

    // 细胞画笔
    private Paint cellPaint;
    // 细胞宽度
    private float size;
    // 细胞状态
    private Set<Integer> cellSet = new HashSet<>();

    // 踪迹画笔
    private Paint tracePaint;
    // 踪迹
    private final Set<Integer> traceSet = new HashSet<>();

    // 第几代细胞
    private int generation = 1;

    // 记录最近3代所有细胞状态
    private Map<Integer, Set<Integer>> recordMap;

    public ConwayLifeGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initPaint();
    }

    private void initPaint() {
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.color_line));

        cellPaint = new Paint();
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setColor(ContextCompat.getColor(getContext(), R.color.purple_500));

        tracePaint = new Paint();
        tracePaint.setStyle(Paint.Style.FILL);
        tracePaint.setColor(ContextCompat.getColor(getContext(), R.color.color_trace));
    }

    private void initCell() {
        size = (getWidth() - (COLS + 1) * EDGE) * 1.0f / COLS;
        rows = (int) ((getHeight() - MARGIN_BOTTOM) / (size + EDGE));
        total = COLS * rows;

        recordMap = new LinkedHashMap<Integer, Set<Integer>>(){
            @Override
            protected boolean removeEldestEntry(Entry eldest) {
                return this.size() > 3;
            }
        };

        Set<Integer> cells = new HashSet<>();
        Random random = new Random();
        while (true) {
            cells.add(random.nextInt(total));
            if (cells.size() == total / 5) {
                break;
            }
        }
        for (Integer cell : cells) {
            addCell(cell % COLS, cell / COLS);
        }

        recordMap.put(generation, cellSet);
    }

    private float getXY(int line) {
        return line * (size + EDGE) + EDGE;
    }

    // 添加细胞
    private void addCell(int col, int row) {
        cellSet.add(row * COLS + col);
        // 添加踪迹
        addTrace(col, row);
    }

    // 画出细胞
    private void drawCell(Canvas canvas) {
        for (Integer integer : cellSet) {
            float x = getXY(integer % COLS);
            float y = getXY(integer / COLS);

            canvas.drawRect(x, y, x + size, y + size, cellPaint);
        }
    }

    // 添加踪迹
    private void addTrace(int col, int row) {
        traceSet.add(row * COLS + col);
    }

    // 画出历史踪迹
    private void drawTrace(Canvas canvas) {
        for (Integer integer : traceSet) {
            float x = getXY(integer % COLS);
            float y = getXY(integer / COLS);

            canvas.drawRect(x, y, x + size, y + size, tracePaint);
        }
    }

    // 计算所有下一代状态
    private void totalNext() {
        Set<Integer> tempCellSet = new HashSet<>();
        for (int i = 0; i < total; i++) {
            if (singleNext(i)) {
                tempCellSet.add(i);
            }
        }

        cellSet = new HashSet<>();
        cellSet.addAll(tempCellSet);
        // 记录轨迹
        traceSet.addAll(tempCellSet);

        recordMap.put(generation, cellSet);
    }

    // 单个细胞下一代状态是否存活 true存活 false死亡
    private boolean singleNext(int index) {
        int neighbor = getNeighbor(index);

        if (isAlive(index)) {
            // (2)当前细胞为存活状态时，当周围的邻居细胞低于两个(不包含两个)存活时，该细胞变成死亡状态(模拟细胞数量稀少)。
            // (3)当前细胞为存活状态时，当周围有两个或3个存活细胞时，该细胞保持原样。
            // (4)当前细胞为存活状态时，当周围有3个以上的存活细胞时，该细胞变成死亡状态(模拟细胞数量过多)。
            return neighbor == 2 || neighbor == 3;
        } else {
            // (1)当前细胞为死亡状态时，当周围有3个存活细胞时，则迭代后该细胞变成存活状态(模拟繁殖)；若原先为生，则保持不变。
            return neighbor == 3;
        }
    }

    // 获取邻居数量
    private int getNeighbor(int index) {
        int col = index % COLS;
        int row = index / COLS;

        int neighbor = 0;
        for (int i = col - 1; i <= col + 1; i++) {
            for (int j = row - 1; j <= row + 1; j++) {
                if (i == col && j == row) {
                    continue;
                }

                if (isAlive(getLoop(j, false) * COLS + getLoop(i, true))) {
                    neighbor++;
                }
            }
        }

        return neighbor;
    }

    // 顶部和底部相连 左边和右边相连 当数值为边缘数值时 返回轮回数值
    private int getLoop(int val, boolean isCol) {

        if (isCol) {
            if (val == -1) {
                return COLS - 1;
            }

            if (val == COLS) {
                return 0;
            }
        } else {
            if (val == -1) {
                return rows - 1;
            }

            if (val == rows) {
                return 0;
            }
        }

        return val;
    }

    // 当前状态 true存活 false死亡
    private boolean isAlive(int index) {
        return cellSet.contains(index);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cellSet.size() == 0) {
            initCell();
        }

        float distance;
        // 画横线
        for (int i = 0; i <= rows; i++) {
            distance = i * (size + EDGE);
            canvas.drawRect(0, distance, getWidth(), distance + EDGE, linePaint);
        }

        float height = rows * (size + EDGE);
        // 画竖线
        for (int i = 0; i <= COLS; i++) {
            distance = i * (size + EDGE);
            canvas.drawRect(distance, 0, distance + EDGE, height, linePaint);
        }

        Log.e(TAG, String.format("第%d代细胞", generation));
        drawTrace(canvas);
        drawCell(canvas);

        final Set<Integer> recordSet = recordMap.keySet();
        int genera = recordSet.iterator().next();
        if (equals(recordMap.get(genera), recordMap.get(genera + 1)) || equals(recordMap.get(genera), recordMap.get(genera + 2))) {
            // 如果当代生命与下一代相同 或者当代生命与隔代相同 此时界面类似一潭死水 2秒后重新绘制新的细胞
            postDelayed(() -> {
                generation = 1;
                traceSet.clear();
                cellSet.clear();
                Log.e(TAG, "重生");
                invalidate();
            }, 2000);
        } else {
            totalNext();
            generation++;
            invalidate();
        }
    }

    @Override
    public boolean isHardwareAccelerated() {
        // 开启硬件加速
        return true;
    }

    // 对比两代生命是否相同
    private boolean equals(Set<Integer> set1, Set<Integer> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }

        if (set1.size() != set2.size()) {
            return false;
        }

        return set1.containsAll(set2);
    }
}
