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

    // 行列数
    private final static int LINES = 100;
    // 细胞总数
    private int total;

    // 线条画笔
    private Paint linePaint;
    // 线条宽度
    private final static int EDGE = 2;

    // 细胞画笔
    private Paint cellPaint;
    // 细胞宽度
    private float size;
    // 细胞状态
    private Set<Integer> cellSet;

    // 踪迹画笔
    private Paint tracePaint;
    // 踪迹
    private Set<Integer> traceSet;

    // 第几代细胞
    private int generation;

    // 保存最近3代细胞数据
    private Map<Integer, Set<Integer>> map;

    public ConwayLifeGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initPaint();
        initLife();
    }

    private void initPaint() {
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.grey));

        cellPaint = new Paint();
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setColor(ContextCompat.getColor(getContext(), R.color.purple_700));

        tracePaint = new Paint();
        tracePaint.setStyle(Paint.Style.FILL);
        tracePaint.setColor(ContextCompat.getColor(getContext(), R.color.teal_200));
    }

    private void initLife() {
        total = LINES * LINES;

        initCell();
    }

    private void initCell() {
        cellSet = new HashSet<>();
        traceSet = new HashSet<>();
        generation = 1;

        map = new LinkedHashMap<Integer, Set<Integer>>(){
            @Override
            protected boolean removeEldestEntry(Entry eldest) {
                return this.size() > 3;
            }
        };

        Random random = new Random();
        Set<Integer> cells = new HashSet<>();
        while (true) {
            int index = random.nextInt(total);
            cells.add(index);

            // 随机生成细胞总数的1/5
            if (cells.size() == total / 5) {
                break;
            }
        }
        for (Integer cell : cells) {
            addCell(cell % LINES, cell / LINES);
        }
    }

    private float getXY(int line) {
        return line * (size + EDGE) + EDGE;
    }

    // 添加细胞
    private void addCell(int col, int row) {
        cellSet.add(row * LINES + col);
        // 添加踪迹
        addTrace(col, row);
    }

    // 画出细胞
    private void drawCell(Canvas canvas) {
        for (Integer integer : cellSet) {
            float x = getXY(integer % LINES);
            float y = getXY(integer / LINES);

            canvas.drawRect(x, y, x + size, y + size, cellPaint);
        }
    }

    // 添加踪迹
    private void addTrace(int col, int row) {
        traceSet.add(row * LINES + col);
    }

    // 画出历史踪迹
    private void drawTrace(Canvas canvas) {
        for (Integer integer : traceSet) {
            float x = getXY(integer % LINES);
            float y = getXY(integer / LINES);

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
        int col = index % LINES;
        int row = index / LINES;

        int neighbor = 0;
        for (int i = col - 1; i <= col + 1; i++) {
            for (int j = row - 1; j <= row + 1; j++) {
                if (i == col && j == row) {
                    continue;
                }

                if (isAlive(getLoop(j) * LINES + getLoop(i))) {
                    neighbor++;
                }
            }
        }

        return neighbor;
    }

    // 当数值为边缘数值时 返回轮回数值
    private int getLoop(int val) {
        if (val == -1) {
            return LINES - 1;
        }

        if (val == LINES) {
            return 0;
        }

        return val;
    }

    // 当前状态 true存活 false死亡
    private boolean isAlive(int index) {
        return cellSet.contains(index);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        size = (width - (LINES + 1) * EDGE) * 1.0f / LINES;

        float distance;
        for (int i = 0; i <= LINES; i++) {
            distance = i * (size + EDGE);
            canvas.drawRect(0, distance, width, distance + EDGE, linePaint);
            canvas.drawRect(distance, 0, distance + EDGE, width, linePaint);
        }

        Log.e(TAG, String.format("第%d代细胞", generation));
        map.put(generation, cellSet);
        Set<Integer> generationSet = map.keySet();

        boolean over = false; // 结束繁衍
        for (Integer integer : generationSet) {
            // 第一与第二代 或者第一与第三代相同则停止
            boolean firEqSec = cellEquals(map.get(integer), map.get(integer + 1));
            boolean firEqThr = cellEquals(map.get(integer), map.get(integer + 2));
            if (firEqSec || firEqThr) {
                over = true;
                break;
            }
        }

        if (!over) {
            drawTrace(canvas);
            drawCell(canvas);
            totalNext();
            generation++;
        } else {
            // 重启生命
            Log.e(TAG, "重启生命");
            initCell();
        }

        postDelayed(() -> invalidate(), 100);
    }

    private boolean cellEquals(Set<Integer> set1, Set<Integer> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }

        if (set1.size() != set2.size()) {
            return false;
        }

        return set1.containsAll(set2);
    }
}
